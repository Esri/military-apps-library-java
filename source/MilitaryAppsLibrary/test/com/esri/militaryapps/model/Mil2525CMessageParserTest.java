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
package com.esri.militaryapps.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

public class Mil2525CMessageParserTest {
    
    public Mil2525CMessageParserTest() {
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

    /**
     * Test of parseMessages method, of class Mil2525CMessageParser.
     */
    @Test
    public void testParseMessages_String() throws Exception {
        System.out.println("parseMessages");
        String type0 = "position_report";
        String id0 = "12345678";
        String type1 = "spotrep";
        String id1 = "12345679";
        String xmlMessages = "<geomessages><geomessage v=\"1.1\"><_type>" + type0 + "</_type><id>" + id0 + "</id></geomessage><geomessage v=\"1.1\"><id>" + id1 + "</id><_type>" + type1 + "</_type></geomessage></geomessages>";
        Mil2525CMessageParserImpl instance = new Mil2525CMessageParserImpl();
        instance.parseMessages(xmlMessages);
        assertEquals(2, instance.messages.size());
        HashMap message0 = instance.messages.get(0);
        assertEquals(type0, message0.get("_type"));
        assertEquals(id0, message0.get("id"));
        HashMap message1 = instance.messages.get(1);
        assertEquals(type1, message1.get("_type"));
        assertEquals(id1, message1.get("id"));
        instance.clearMessages();
        assertEquals(0, instance.messages.size());
    }

    public class Mil2525CMessageParserImpl extends Mil2525CMessageParser {
        
        public static final String ID_FIELD_NAME = "id";
        public HashMap<String, Object> message = null;
        public List<HashMap> messages = new ArrayList<HashMap>();

        public Mil2525CMessageParserImpl() throws ParserConfigurationException, SAXException {
            super();
        }

        public void newMessage() {
            message = new HashMap<String, Object>();
            messages.add(message);
        }

        public void setMessageId(String id) {
            message.put(ID_FIELD_NAME, id);
        }

        public void setMessageProperty(String key, Object value) {
            message.put(key, value);
        }

        public String getMessageIdPropertyName() {
            return ID_FIELD_NAME;
        }

        public void clearMessages() {
            messages.clear();
        }
    }
}