/*******************************************************************************
 * Copyright 2015 Esri
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

import com.esri.militaryapps.controller.ChemLightController;
import com.esri.militaryapps.controller.MessageController;
import com.esri.militaryapps.model.Geomessage;
import com.esri.militaryapps.model.GeomessagesReader;
import com.esri.militaryapps.util.Utilities;
import java.awt.Color;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Assert;
import org.junit.Test;

public class ChemLightControllerTest {
    
    private class Result {
        String message = null;
    }
    
    private static final int PORT = 48989;
    private static final String USERNAME = "ChemLightControllerTest 42";
    private static final double X = 12;
    private static final double Y = 34;
    private static final Color COLOR = Color.RED;
    
    private static ChemLightController controller;
    static {
        try {
            MessageController messageController = new MessageController(PORT, "3A1-001");
            controller = new ChemLightController(messageController, USERNAME);
        } catch (Throwable t) {
            Assert.fail("Couldn't set up test: " + t.getMessage());
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
                    Logger.getLogger(MessageControllerTest.class.getName()).log(Level.SEVERE, null, t);
                }
            }
            
        }.start();
        
        controller.sendChemLight(12, 34, COLOR.getRGB());
        
        Thread.sleep(2000);
        synchronized (result) {
            Assert.assertNotNull(result.message);
            GeomessagesReader reader = new GeomessagesReader();
            List<Geomessage> messages = reader.parseMessages(result.message);
            Assert.assertEquals(1, messages.size());
            Geomessage message = messages.get(0);
            Assert.assertEquals(ChemLightController.REPORT_TYPE, message.getProperty(Geomessage.TYPE_FIELD_NAME));
            Assert.assertEquals(USERNAME, message.getProperty("uniquedesignation"));
            Assert.assertEquals(X + "," + Y, message.getProperty(Geomessage.CONTROL_POINTS_FIELD_NAME));
            Assert.assertEquals(Utilities.getAFMGeoEventColorString(COLOR.getRGB()), message.getProperty("color"));
        }
    }
        
}