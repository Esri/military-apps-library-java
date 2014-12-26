/*******************************************************************************
 * Copyright 2013-2014 Esri
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.militaryapps.controller.test;

import com.esri.militaryapps.controller.LocationController;
import com.esri.militaryapps.controller.LocationController.LocationMode;
import com.esri.militaryapps.controller.MessageController;
import com.esri.militaryapps.controller.MessageControllerListener;
import com.esri.militaryapps.controller.PositionReportController;
import com.esri.militaryapps.model.Geomessage;
import com.esri.militaryapps.model.LocationProvider;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

public class PositionReportControllerTest {
    
    private class Result {
        String message = null;
        Geomessage geomessage = null;
    }
    
    private static final int PORT = 48965;
    private static final String USERNAME = "Honey Badgers 42G";
    private static final String VEHICLE_TYPE = "HMMWV";
    private static final String UID = UUID.randomUUID().toString();
    private static final String SIC = "SFGPEVCAH------";
    
    private static PositionReportController controller;
    private static LocationController locController;
    static {
        try {
            locController = new LocationController(LocationMode.SIMULATOR, false) {

                @Override
                protected LocationProvider createLocationServiceProvider() {
                    return null;
                }
            };
            locController.start();
            MessageController messageController = new MessageController(PORT, "3A1-001");
            messageController.startReceiving();
            controller = new PositionReportController(locController, messageController, USERNAME, VEHICLE_TYPE, UID, SIC);
        } catch (Throwable t) {
            Assert.fail("Couldn't set up test: " + t.getMessage());
        }
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
        locController.pause();
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
        controller.setEnabled(false);
    }

    @Test
    public void testEnabled() {
        System.out.println("testEnabled");
        Assert.assertFalse(controller.isEnabled());
        controller.setEnabled(true);
        Assert.assertTrue(controller.isEnabled());
        controller.setEnabled(false);
        Assert.assertFalse(controller.isEnabled());
    }

    @Test
    public void testPeriod() {
        System.out.println("testPeriod");
        Assert.assertEquals(PositionReportController.DEFAULT_PERIOD, controller.getPeriod());
        controller.setPeriod(3456);
        Assert.assertEquals(3456, controller.getPeriod());
        controller.setPeriod(-42);
        Assert.assertEquals(PositionReportController.DEFAULT_PERIOD, controller.getPeriod());
        controller.setPeriod(PositionReportController.DEFAULT_PERIOD);
        Assert.assertEquals(PositionReportController.DEFAULT_PERIOD, controller.getPeriod());
    }
    
    @Test
    public void testUsername() {
        System.out.println("testUsername");
        Assert.assertEquals(USERNAME, controller.getUsername());
        controller.setUsername("Woody");
        Assert.assertEquals("Woody", controller.getUsername());
        controller.setUsername(USERNAME);
        Assert.assertEquals(USERNAME, controller.getUsername());
    }
    
    @Test
    public void testVehicleType() {
        System.out.println("testVehicleType");
        Assert.assertEquals(VEHICLE_TYPE, controller.getVehicleType());
        controller.setVehicleType("SR-71");
        Assert.assertEquals("SR-71", controller.getVehicleType());
        controller.setVehicleType(VEHICLE_TYPE);
        Assert.assertEquals(VEHICLE_TYPE, controller.getVehicleType());
    }
    
    @Test
    public void testUniqueId() {
        System.out.println("testUniqueId");
        Assert.assertEquals(UID, controller.getUniqueId());
        controller.setUniqueId("Percy");
        Assert.assertEquals("Percy", controller.getUniqueId());
        controller.setUniqueId(UID);
        Assert.assertEquals(UID, controller.getUniqueId());
    }
    
    @Test
    public void testMessage() throws Exception {
        System.out.println("testMessage");
        final Result result = new Result();
        MessageControllerListener listener = new MessageControllerListener() {

            @Override
            public void geomessageReceived(Geomessage geomessage) {
                result.geomessage = geomessage;
            }

            @Override
            public void datagramReceived(String contents) {
                result.message = contents;
            }
        };
        controller.getMessageController().addListener(listener);
        
        controller.setEnabled(true);
        Thread.sleep(2000);
        synchronized (result) {
            /**
             * Here we just make sure the message is not null and that it contains
             * the username. Someone could add code to do a more rigorous test, realizing
             * that elements won't be in any particular order in the XML message.
             */
            Assert.assertNotNull(result.message);
            Assert.assertTrue(0 < result.message.indexOf(">" + USERNAME + "<"));
        }
        controller.getMessageController().removeListener(listener);
    }
    
}