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
package com.esri.militaryapps.util.test;

import com.esri.militaryapps.util.Utilities;
import java.awt.Color;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A test for the Utilities class.
 * @see com.esri.militaryapps.util.Utilities
 */
public class UtilitiesTest {
    
    public UtilitiesTest() {
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
     * Test of toCompassHeadingRadians method, of class Utilities.
     */
    @Test
    public void testToCompassHeadingRadians() {
        double trigHeadingRadians = 0.0;
        double expResult = Math.PI / 2.0;
        double result = Utilities.toCompassHeadingRadians(trigHeadingRadians);
        assertEquals(expResult, result, 0.0);

        trigHeadingRadians = Math.PI / 4.0;
        expResult = Math.PI / 4.0;
        result = Utilities.toCompassHeadingRadians(trigHeadingRadians);
        assertEquals(expResult, result, 0.0);
        
        trigHeadingRadians = Math.PI / 2.0;
        expResult = 0.0;
        result = Utilities.toCompassHeadingRadians(trigHeadingRadians);
        assertEquals(expResult, result, 0.0);
        
        trigHeadingRadians = 3 * Math.PI / 4.0;
        expResult = 7 * Math.PI / 4.0;
        result = Utilities.toCompassHeadingRadians(trigHeadingRadians);
        assertEquals(expResult, result, 0.0);
        
        trigHeadingRadians = Math.PI;
        expResult = 3.0 * Math.PI / 2.0;
        result = Utilities.toCompassHeadingRadians(trigHeadingRadians);
        assertEquals(expResult, result, 0.0);
        
        trigHeadingRadians = 5 * Math.PI / 4.0;
        expResult = 5 * Math.PI / 4.0;
        result = Utilities.toCompassHeadingRadians(trigHeadingRadians);
        assertEquals(expResult, result, 0.0);
        
        trigHeadingRadians = 3 * Math.PI / 2.0;
        expResult = Math.PI;
        result = Utilities.toCompassHeadingRadians(trigHeadingRadians);
        assertEquals(expResult, result, 0.0);
        
        trigHeadingRadians = 7 * Math.PI / 4.0;
        expResult = 3 * Math.PI / 4.0;
        result = Utilities.toCompassHeadingRadians(trigHeadingRadians);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of calculateBearingDegrees method, of class Utilities.
     */
    @Test
    public void testCalculateBearingDegrees() {
        //NW to NW
        double expResult = 307.7988889;
        double result = Utilities.calculateBearingDegrees(-74.0, 40.7, -172.5, 41.5);
        assertEquals(expResult, result, 0.001);

        //NW to NE
        expResult = 315.425;
        result = Utilities.calculateBearingDegrees(-74.0, 40.7, 172.5, 41.5);
        assertEquals(expResult, result, 0.001);

        //NW to SW
        expResult = 239.8547222;
        result = Utilities.calculateBearingDegrees(-74.0, 40.7, -172.5, -41.5);
        assertEquals(expResult, result, 0.001);

        //NW to SE
        expResult = 245.8741667;
        result = Utilities.calculateBearingDegrees(-74.0, 40.7, 172.5, -41.5);
        assertEquals(expResult, result, 0.001);

        //NE to NW
        expResult = 44.575;
        result = Utilities.calculateBearingDegrees(74.0, 40.7, -172.5, 41.5);
        assertEquals(expResult, result, 0.001);

        //NE to NE
        expResult = 52.20111111;
        result = Utilities.calculateBearingDegrees(74.0, 40.7, 172.5, 41.5);
        assertEquals(expResult, result, 0.001);

        //NE to SW
        expResult = 114.1258333;
        result = Utilities.calculateBearingDegrees(74.0, 40.7, -172.5, -41.5);
        assertEquals(expResult, result, 0.001);

        //NE to SE
        expResult = 120.1452778;
        result = Utilities.calculateBearingDegrees(74.0, 40.7, 172.5, -41.5);
        assertEquals(expResult, result, 0.001);

        //SW to NW
        expResult = 300.1452778;
        result = Utilities.calculateBearingDegrees(-74.0, -40.7, -172.5, 41.5);
        assertEquals(expResult, result, 0.001);

        //SW to NE
        expResult = 294.1258333;
        result = Utilities.calculateBearingDegrees(-74.0, -40.7, 172.5, 41.5);
        assertEquals(expResult, result, 0.001);

        //SW to SW
        expResult = 232.2011111;
        result = Utilities.calculateBearingDegrees(-74.0, -40.7, -172.5, -41.5);
        assertEquals(expResult, result, 0.001);

        //SW to SE
        expResult = 224.575;
        result = Utilities.calculateBearingDegrees(-74.0, -40.7, 172.5, -41.5);
        assertEquals(expResult, result, 0.001);

        //SE to NW
        expResult = 65.87416667;
        result = Utilities.calculateBearingDegrees(74.0, -40.7, -172.5, 41.5);
        assertEquals(expResult, result, 0.001);

        //SE to NE
        expResult = 59.85472222;
        result = Utilities.calculateBearingDegrees(74.0, -40.7, 172.5, 41.5);
        assertEquals(expResult, result, 0.001);

        //SE to SW
        expResult = 135.425;
        result = Utilities.calculateBearingDegrees(74.0, -40.7, -172.5, -41.5);
        assertEquals(expResult, result, 0.001);

        //SE to SE
        expResult = 127.7988889;
        result = Utilities.calculateBearingDegrees(74.0, -40.7, 172.5, -41.5);
        assertEquals(expResult, result, 0.001);
    }

    /**
     * Test of fixAngleDegrees method, of class Utilities.
     */
    @Test
    public void testFixAngleDegrees() {
        System.out.println("fixAngleDegrees");
        double expResult = 123;
        double result = Utilities.fixAngleDegrees(123, 0, 360);
        assertEquals(expResult, result, 0.0);

        expResult = 0;
        result = Utilities.fixAngleDegrees(0, 0, 360);
        assertEquals(expResult, result, 0.0);

        expResult = 360;
        result = Utilities.fixAngleDegrees(360, 0, 360);
        assertEquals(expResult, result, 0.0);

        expResult = 1;
        result = Utilities.fixAngleDegrees(361, 0, 360);
        assertEquals(expResult, result, 0.0);

        expResult = 360;
        result = Utilities.fixAngleDegrees(720, 0, 360);
        assertEquals(expResult, result, 0.0);

        expResult = 359;
        result = Utilities.fixAngleDegrees(-1, 0, 360);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getAFMGeoEventColorString method, of class Utilities.
     */
    @Test
    public void testGetAFMGeoEventColorString() {
        int colorRgb = Color.BLUE.getRGB();
        String expResult = "3";
        String result = Utilities.getAFMGeoEventColorString(colorRgb);
        assertEquals(expResult, result);
        
        colorRgb = Color.YELLOW.getRGB();
        expResult = "4";
        result = Utilities.getAFMGeoEventColorString(colorRgb);
        assertEquals(expResult, result);
        
        colorRgb = Color.GREEN.getRGB();
        expResult = "2";
        result = Utilities.getAFMGeoEventColorString(colorRgb);
        assertEquals(expResult, result);
        
        colorRgb = Color.RED.getRGB();
        expResult = "1";
        result = Utilities.getAFMGeoEventColorString(colorRgb);
        assertEquals(expResult, result);
        
        colorRgb = 0x002255;
        expResult = "#002255";
        result = Utilities.getAFMGeoEventColorString(colorRgb);
        assertEquals(expResult, result);
    }
}