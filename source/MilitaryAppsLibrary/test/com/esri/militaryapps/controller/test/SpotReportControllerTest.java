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
import com.esri.militaryapps.controller.MapController;
import com.esri.militaryapps.controller.OutboundMessageController;
import com.esri.militaryapps.controller.SpotReportController;
import com.esri.militaryapps.controller.test.OutboundMessageControllerTest;
import com.esri.militaryapps.model.Location;
import com.esri.militaryapps.model.LocationProvider;
import com.esri.militaryapps.model.SpotReport;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Calendar;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.junit.Test;

public class SpotReportControllerTest {
    
    private class Result {
        String message = null;
    }
    
    private static final int PORT = 48989;
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
    
    private static class MapControllerImpl extends MapController {
        
        private double scale = 15000.0;
        private double rotation = 0.0;
        private boolean gridVisible = false;

        public MapControllerImpl() {
            fireMapReady();
        }
        
        @Override
        public void zoom(double factor) {
            scale *= factor;
        }

        @Override
        public void setRotation(double degrees) {
            rotation = degrees;
        }

        @Override
        public double getRotation() {
            return rotation;
        }

        @Override
        protected void _zoomToScale(double scale, double centerPointX, double centerPointY) {
            this.scale = scale;
        }

        @Override
        public int getWidth() {
            return 600;
        }

        @Override
        public int getHeight() {
            return 400;
        }

        @Override
        public void panTo(double centerX, double centerY) {
            
        }

        @Override
        public double[] toMapPoint(int screenX, int screenY) {
            return new double[] {0, 0};
        }

        @Override
        public void setGridVisible(boolean visible) {
            gridVisible = visible;
        }

        @Override
        public boolean isGridVisible() {
            return gridVisible;
        }        
        
        public double getScale() {
            return scale;
        }

        @Override
        protected LocationController createLocationController() {
            try {
                return new LocationController(LocationController.LocationMode.SIMULATOR, false){

                    @Override
                    protected LocationProvider createLocationServiceProvider() {
                        throw new UnsupportedOperationException(
                                "We're not testing LocationDaemon here. This shouldn't be called.");
                    }

                };
            } catch (Exception e) {
                fail("Couldn't create LocationController: " + e.getMessage());
                return null;
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            
        }

        @Override
        public void setAutoPan(boolean autoPan) {
            
        }

        @Override
        public boolean isAutoPan() {
            return false;
        }

        @Override
        public String pointToMgrs(double x, double y, int wkid) {
            return "test MGRS";
        }
        
    }
    
    private static SpotReportController controller;
    static {
        try {
            MapController mapController = new MapControllerImpl();
            OutboundMessageController messageController = new OutboundMessageController(PORT) {
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
            controller = new SpotReportController(mapController, messageController);
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
                    Logger.getLogger(OutboundMessageControllerTest.class.getName()).log(Level.SEVERE, null, t);
                }
            }
            
        }.start();
        
        SpotReport spotReport = new SpotReport(
                SpotReport.Size.TEAM,
                SpotReport.Activity.MOVING,
                -111.5,
                40.9,
                4326,
                SpotReport.Unit.NGO,
                Calendar.getInstance(),
                SpotReport.Equipment.HOWITZER);
        controller.sendSpotReport(spotReport, USERNAME);
        
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