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
package eu.seaclouds.platform.dashboard;

import eu.seaclouds.deployer.SeaCloudsProperties;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;

public class SeaCloudsPropertiesTest {

    @Test
    public void testFilePath() throws Exception {
        File f = new File("seaclouds.properties");
        assertTrue(f.exists());
    }

    @Test
    public void testGetProperty() throws Exception {
        assertNotNull(SeaCloudsProperties.get(SeaCloudsProperties.DEPLOYER_ENDPOINT));
    }
}