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
package com.esri.militaryapps.model.test;

import com.esri.militaryapps.model.Location;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A test for the Location class.
 * @see com.esri.militaryapps.model.Location
 */
public class LocationTest {
    
    public LocationTest() {
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
     * Test Location class's speed conversions.
     */
    @Test
    public void testSpeedConversions() {
        System.out.println("getSpeed");
        Location instance = new Location();
        instance.setSpeedMph(55);
        assertEquals(24.587, instance.getSpeed(), 0.001);
        assertEquals(55, instance.getSpeedMph(), 0);
        instance.setSpeed(55);
        assertEquals(55, instance.getSpeed(), 0);
        assertEquals(123.031, instance.getSpeedMph(), 0.001);
    }
    
}