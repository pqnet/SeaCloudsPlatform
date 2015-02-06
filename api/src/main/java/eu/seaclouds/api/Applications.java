/**
 * Copyright 2014 SeaClouds
 * Contact: dev@seaclouds-project.eu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package eu.seaclouds.api;

import brooklyn.rest.client.BrooklynApi;
import brooklyn.rest.domain.ApplicationSummary;
import brooklyn.rest.domain.EntitySummary;
import brooklyn.rest.domain.LocationSummary;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.seaclouds.core.SeaCloudsProperties;
import org.jboss.resteasy.client.ClientResponseFailure;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/applications")
public class Applications {

    final static BrooklynApi BROOKLKYN_API = new BrooklynApi(SeaCloudsProperties.get(SeaCloudsProperties.DEPLOYER_ENDPOINT));

    @GET
    @Produces("application/json")
    public Response listApplications() {
        List<ApplicationSummary> applicationSummaries = BROOKLKYN_API.getApplicationApi().list();
        Response response;

        Collections.sort(applicationSummaries, new Comparator<ApplicationSummary>() {
            @Override
            public int compare(ApplicationSummary s1, ApplicationSummary s2) {
                return s1.getId().compareTo(s2.getId());
            }
        });


        if (applicationSummaries == null) {
            return Response.status(Response
                    .Status.INTERNAL_SERVER_ERROR)
                    .entity("Connection error: couldn't reach SeaClouds endpoint")
                    .build();
        } else {

            JsonArray jsonResult = new JsonArray();

            for (ApplicationSummary application : applicationSummaries) {

                JsonObject jsonApplication = new JsonObject();
                jsonResult.add(jsonApplication);

                jsonApplication.addProperty("id", application.getId());
                jsonApplication.addProperty("status", application.getStatus().toString());

                JsonObject jsonSpec = new JsonObject();
                jsonApplication.add("spec", jsonSpec);

                jsonSpec.addProperty("name", application.getSpec().getName());
                jsonSpec.addProperty("type", application.getSpec().getName());

                JsonArray jsonDescendantsEntities = new JsonArray();
                jsonApplication.add("descendants", jsonDescendantsEntities);

                List<EntitySummary> descendants;
                try {
                    descendants = BROOKLKYN_API.getEntityApi().list(application.getId());

                    if (descendants != null) {
                        for (EntitySummary childEntity : descendants) {
                            JsonObject jsonDescendantEntity = new JsonObject();
                            jsonDescendantsEntities.add(jsonDescendantEntity);

                            jsonDescendantEntity.addProperty("id", childEntity.getId());
                            jsonDescendantEntity.addProperty("name", childEntity.getName());
                            jsonDescendantEntity.addProperty("type", childEntity.getType());

                            JsonArray jsonArrayLocations = new JsonArray();
                            jsonDescendantEntity.add("locations", jsonArrayLocations);

                            List<LocationSummary> locations = BROOKLKYN_API.getEntityApi().getLocations(application.getId(), childEntity.getId());
                            if (locations != null) {

                                for (LocationSummary locationSummary : locations) {
                                    LocationSummary locationDetails = BROOKLKYN_API.getLocationApi().get(locationSummary.getId(), null);

                                    if (locationDetails != null) {
                                        JsonObject jsonLocation = new JsonObject();
                                        jsonArrayLocations.add(jsonLocation);
                                        jsonLocation.addProperty("id", locationDetails.getId());
                                        jsonLocation.addProperty("name", locationDetails.getName());
                                        jsonLocation.addProperty("type", locationDetails.getType());
                                        jsonLocation.addProperty("spec", locationDetails.getSpec());
                                    }
                                }
                            }
                        }
                    }
                } catch (ClientResponseFailure e) {
                    // The application removed after calling getApplicationApi().list()
                    jsonResult.remove(jsonApplication);
                }
            }

            return Response.ok().entity(new Gson().toJson(jsonResult)).build();
        }
    }

    @POST
    @Produces("application/json")
    public Response addApplication(@QueryParam("yaml") String yaml) {
        if (yaml != null) {
            Response res = BROOKLKYN_API.getApplicationApi().createFromYaml(yaml);

            if (res.getStatus() >= 400) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Connection error: couldn't reach Deployer engine endpoint")
                        .build();
            } else {
                return Response.ok().entity(new Gson().toJson(res.getStatus())).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing argument: yaml")
                    .build();
        }
    }

    @DELETE
    @Produces("application/json")
    public Response removeApplication(@QueryParam("application") String application){
        if (application != null) {

            Response res = BROOKLKYN_API.getApplicationApi().delete(application);
            if (res.getStatus() >=  400) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Connection error: couldn't reach Deployer engine endpoint")
                        .build();
            } else {
                return Response.ok().entity(new Gson().toJson(res.getStatus())).build();
            }

        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing argument: yaml")
                    .build();
        }
    }

}