/*******************************************************************************
 * Copyright 2013 Esri
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
import com.esri.militaryapps.controller.PositionReportController;
import com.esri.militaryapps.model.LocationProvider;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class PositionReportControllerTest {
    
    private class Result {
        String message = null;
    }
    
    private static final int PORT = 48965;
    private static final String USERNAME = "Honey Badgers 42G";
    private static final String VEHICLE_TYPE = "HMMWV";
    private static final String UID = UUID.randomUUID().toString();
    private static final String SIC = "SFGPEVCAH------";
    private static final String PROPNAME_TYPE = "type";
    private static final String PROPNAME_ID = "id";
    private static final String PROPNAME_WKID = "wkid";
    private static final String PROPNAME_CONTROL_POINTS = "control_points";
    private static final String PROPNAME_ACTION = "action";
    private static final String PROPNAME_SIC = "sic";
    
    private static PositionReportController controller;
    static {
        try {
            LocationController locController = new LocationController(LocationMode.SIMULATOR, false) {

                @Override
                protected LocationProvider createLocationServiceProvider() {
                    return null;
                }
            };
            locController.start();
            MessageController messageController = new MessageController(PORT) {
                @Override
                public String getTypePropertyName() {
                    return PROPNAME_TYPE;
                }

                @Override
                public String getIdPropertyName() {
                    return PROPNAME_ID;
                }

                @Override
                public String getWkidPropertyName() {
                    return PROPNAME_WKID;
                }

                @Override
                public String getControlPointsPropertyName() {
                    return PROPNAME_CONTROL_POINTS;
                }

                @Override
                public String getActionPropertyName() {
                    return PROPNAME_ACTION;
                }

                @Override
                public String getSymbolIdCodePropertyName() {
                    return PROPNAME_SIC;
                }
            };
            controller = new PositionReportController(locController, messageController, USERNAME, VEHICLE_TYPE, UID, SIC);
        } catch (Throwable t) {
            fail("Couldn't set up test: " + t.getMessage());
        }
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
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
        System.out.println("enabled");
        assertFalse(controller.isEnabled());
        controller.setEnabled(true);
        assertTrue(controller.isEnabled());
        controller.setEnabled(false);
        assertFalse(controller.isEnabled());
    }

    @Test
    public void testPeriod() {
        System.out.println("period");
        assertEquals(PositionReportController.DEFAULT_PERIOD, controller.getPeriod());
        controller.setPeriod(3456);
        assertEquals(3456, controller.getPeriod());
        controller.setPeriod(-42);
        assertEquals(PositionReportController.DEFAULT_PERIOD, controller.getPeriod());
        controller.setPeriod(PositionReportController.DEFAULT_PERIOD);
        assertEquals(PositionReportController.DEFAULT_PERIOD, controller.getPeriod());
    }
    
    @Test
    public void testUsername() {
        System.out.println("username");
        assertEquals(USERNAME, controller.getUsername());
        controller.setUsername("Woody");
        assertEquals("Woody", controller.getUsername());
        controller.setUsername(USERNAME);
        assertEquals(USERNAME, controller.getUsername());
    }
    
    @Test
    public void testVehicleType() {
        System.out.println("vehicleType");
        assertEquals(VEHICLE_TYPE, controller.getVehicleType());
        controller.setVehicleType("SR-71");
        assertEquals("SR-71", controller.getVehicleType());
        controller.setVehicleType(VEHICLE_TYPE);
        assertEquals(VEHICLE_TYPE, controller.getVehicleType());
    }
    
    @Test
    public void testUniqueId() {
        System.out.println("uniqueId");
        assertEquals(UID, controller.getUniqueId());
        controller.setUniqueId("Percy");
        assertEquals("Percy", controller.getUniqueId());
        controller.setUniqueId(UID);
        assertEquals(UID, controller.getUniqueId());
    }
    
    @Test
    public void testMessage() throws Exception {
        final Result result = new Result();
        new Thread() {

            @Override
            public void run() {
                byte[] message = new byte[1500];
                DatagramPacket packet = new DatagramPacket(message, message.length);
                DatagramSocket socket;
                try {
                    socket = new DatagramSocket(PORT);
                    socket.receive(packet);
                    String msgString = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    synchronized (result) {
                        result.message = msgString;
                    }
                } catch (Throwable t) {
                    Logger.getLogger(MessageControllerTest.class.getName()).log(Level.SEVERE, null, t);
                }
            }
            
        }.start();
        
        controller.getMessageController().setPort(PORT);
        controller.setEnabled(true);
        Thread.sleep(2000);
        synchronized (result) {
            /**
             * Here we just make sure the message is not null and that it contains
             * the username. Someone could add code to do a more rigorous test, realizing
             * that elements won't be in any particular order in the XML message.
             */
            assertNotNull(result.message);
            assertTrue(0 < result.message.indexOf(">" + USERNAME + "<"));
        }
    }
    
}