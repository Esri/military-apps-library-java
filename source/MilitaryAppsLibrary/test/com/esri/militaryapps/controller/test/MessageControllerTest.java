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

import com.esri.militaryapps.controller.MessageController;
import com.esri.militaryapps.controller.MessageControllerListener;
import com.esri.militaryapps.model.Geomessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

public class MessageControllerTest {
    
    private class Result {
        String message = null;
        HashMap<String, Geomessage> geomessages = new HashMap<String, Geomessage>();
    }

    private static final int TEST_PORT = 59849;
    private static final int TEST_PORT_2 = 16346;
    
    @Before
    public void setUp() throws ParserConfigurationException, SAXException {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testSendUDPMessage() throws Exception {
        System.out.println("sendUDPMessage");

        MessageController controller = new MessageController(TEST_PORT);
        controller.startReceiving();
        
        final Result result = new Result();
        MessageControllerListener listener = new MessageControllerListener() {

            @Override
            public void geomessageReceived(Geomessage geomessage) {
                fail("That text had no Geomessages!");
            }

            @Override
            public void datagramReceived(String contents) {
                result.message = contents;
            }
            
        };
        
        controller.addListener(listener);
        
        String expected = "Test message " + System.currentTimeMillis();
        byte[] bytes = expected.getBytes();
        Thread.sleep(100);
        controller.sendMessage(bytes);
        Thread.sleep(100);
        controller.removeListener(listener);
        controller.stopReceiving();
        assertEquals(expected, result.message);
    }
    
    @Test
    public void testSendGeomessage() throws IOException, InterruptedException {
        System.out.println("sendGeomessage");
        MessageController controller = new MessageController(TEST_PORT);
        doGeomessageTesting(controller);
        
        controller.setPort(TEST_PORT_2);
        Thread.sleep(2000);
        doGeomessageTesting(controller);
    }
    
    private void doGeomessageTesting(MessageController controller) throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        InputStream in = getClass().getResource("/geomessages.xml").openStream();
        int next = -1;
        while (-1 != (next = in.read())) {
            sb.append((char) next);
        }
        in.close();
        
        final Result result = new Result();
        controller.startReceiving();
        MessageControllerListener listener;
        listener = new MessageControllerListener() {
            @Override
            public void geomessageReceived(Geomessage geomessage) {
                result.geomessages.put(geomessage.getId(), geomessage);
            }

            @Override
            public void datagramReceived(String contents) {
                result.message = contents;
            }
            
        };
        controller.addListener(listener);
        
        String expected = sb.toString();
        byte[] bytes = expected.getBytes();
        Thread.sleep(100);
        controller.sendMessage(bytes);
        Thread.sleep(100);
        controller.removeListener(listener);
        controller.stopReceiving();
        assertEquals(expected, result.message);
        assertEquals(2, result.geomessages.size());
        assertEquals("3A1-001", result.geomessages.get("{3a752ef3-b085-41e8-993a-3ec39098fde2}").getProperty("uniquedesignation"));
        assertEquals("3A2-002", result.geomessages.get("{48f54ca2-ae19-4de0-9fda-f8dd9b17adac}").getProperty("uniquedesignation"));
    }
    
}