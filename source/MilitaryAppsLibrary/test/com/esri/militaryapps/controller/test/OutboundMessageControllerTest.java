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

import com.esri.militaryapps.controller.OutboundMessageController;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class OutboundMessageControllerTest {
    
    private class Result {
        String message = null;
    }

    private static final int TEST_PORT = 59849;
    
    private OutboundMessageController controller = null;
    
    @Before
    public void setUp() {
        controller = new OutboundMessageController(TEST_PORT) {
            @Override
            public String getTypePropertyName() {
                return null;
            }
            
            @Override
            public String getIdPropertyName() {
                return null;
            }
            
            @Override
            public String getWkidPropertyName() {
                return null;
            }
            
            @Override
            public String getControlPointsPropertyName() {
                return null;
            }
            
            @Override
            public String getActionPropertyName() {
                return null;
            }
        };
    }
    
    @After
    public void tearDown() {
    }
    
    /**
     * Test of sendMessage method, of class OutboundMessageController.
     */
    @Test
    public void testSendUDPMessage() throws Exception {
        System.out.println("sendUDPMessage");

        final Result result = new Result();
        new Thread() {

            @Override
            public void run() {
                byte[] message = new byte[1500];
                DatagramPacket packet = new DatagramPacket(message, message.length);
                DatagramSocket socket;
                try {
                    socket = new DatagramSocket(TEST_PORT);
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
        
        String expected = "Test message " + System.currentTimeMillis();
        byte[] bytes = expected.getBytes();
        Thread.sleep(100);
        controller.sendMessage(bytes);
        Thread.sleep(100);
        assertEquals(expected, result.message);
    }
    
}