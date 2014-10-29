package brooklyn.rest.client;

import brooklyn.BrooklynTestUtils;
import brooklyn.rest.api.ApplicationApi;
import brooklyn.rest.api.SensorApi;
import brooklyn.rest.client.BrooklynApi;
import brooklyn.rest.domain.ApplicationSummary;
import brooklyn.rest.domain.SensorSummary;
import core.BrooklynConnector;
import core.Manager;
import metrics.MetricCatalog;
import model.Application;
import model.ApplicationModule;
import model.Module;
import model.exceptions.MetricNotFoundException;
import model.exceptions.MonitorConnectorException;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author MBarrientos
 */
@Test(groups = { "brooklyn" })
public class BrooklynApiTest {

    public static final String BROOKLYN_ENDPOINT = "http://devtest.scenic.uma.es:8081";

    private BrooklynApi api;
    private ApplicationApi applicationApi;
    private SensorApi sensorApi;
    private List<SensorSummary> sensorSummaryList;
    private String appId;
    private String appName;
    private String moduleId;

    private Application application;
    private Module webServerModule;

    @BeforeClass
    public void setup(){
        System.out.println("BrooklynApiTest.setup()");

        api = BrooklynTestUtils.getApi();
        applicationApi = BrooklynTestUtils.getApplicationApi();
        appId = BrooklynTestUtils.getAppId();
        appName = BrooklynTestUtils.getAppName();

        moduleId = applicationApi.getDescendants(appId,".*jboss.*").iterator().next().getId();

        sensorApi = api.getSensorApi();
        sensorSummaryList = sensorApi.list(appId, moduleId);

        application = new Application(appId,appName);
        webServerModule = new ApplicationModule(moduleId,application,application);
    }

    @Test
    public void brooklynApiTest(){
        // Requires application to be completely deployed and running.
        if(BrooklynTestUtils.isAppRunning(appId)) {
            Object res = sensorApi.get(appId, moduleId, "java.metrics.physicalmemory.free", false);
            Assert.assertNotNull(res);
        }
    }

    @Test
    public void wrongSensorNameTest(){
        // Requires application to be completely deployed and running.
        Object res = sensorApi.get(appId, moduleId, "this.is.not.a.sensor", false);
        Assert.assertNull(res);
    }

    @Test
    public void registerAgent() throws MonitorConnectorException {
        Manager manager = Manager.getInstance();
        manager.registerApplication(application);
        manager.addMonitoringAgent(application, new BrooklynConnector(webServerModule, BROOKLYN_ENDPOINT));
    }

    @Test( expectedExceptions = MetricNotFoundException.class)
    public void wrongSensorTest() throws MetricNotFoundException, MonitorConnectorException {
        Manager manager = Manager.getInstance();
        manager.addMonitoringAgent(webServerModule, new BrooklynConnector(webServerModule, BROOKLYN_ENDPOINT));
        manager.getMetricValue(webServerModule, "this.is.not.a.metric.id");
    }

    @Test
    public void sensorTest() throws MetricNotFoundException, MonitorConnectorException {
        Manager manager = Manager.getInstance();
        manager.addMonitoringAgent(webServerModule, new BrooklynConnector(webServerModule, BROOKLYN_ENDPOINT));
        manager.getMetricValue(webServerModule, MetricCatalog.PredefinedMetrics.WEBAPP_REQUESTS_TOTAL.getValue().getId());
    }
}
