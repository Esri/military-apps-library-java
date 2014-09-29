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
import com.esri.militaryapps.controller.LocationListener;
import com.esri.militaryapps.model.Location;
import com.esri.militaryapps.model.LocationProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.xml.sax.SAXException;

/**
 * A test for the LocationController class.
 * @see com.esri.militaryapps.controller.LocationController
 */
public class LocationControllerTest {
    
    public LocationControllerTest() {
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
     * Test of addListener method, of class LocationController.
     */
    @Test
    public void testAddListener() throws ParserConfigurationException, SAXException, IOException {
        System.out.println("addListener");
        final ArrayList<Location> locations = new ArrayList<Location>();
        LocationListener listener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                if (null != location) {
                    synchronized (locations) {
                        locations.add(location);
                    }
                }
            }

            @Override
            public void onStateChanged(LocationProvider.LocationProviderState state) {
                
            }
            
        };
        LocationController instance = new LocationControllerImpl();
        instance.addListener(listener);
        instance.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(LocationControllerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        synchronized (locations) {
            Assert.assertTrue("LocationController should have provided a location", 0 < locations.size());
            Assert.assertEquals(70.4085697998, locations.get(0).getLongitude(), 0.000001);
            Assert.assertEquals(34.4188940003, locations.get(0).getLatitude(), 0.000001);
        }
        instance.pause();
    }

    public class LocationControllerImpl extends LocationController {

        public LocationControllerImpl() throws ParserConfigurationException, SAXException, IOException {
            super(LocationMode.SIMULATOR, false);
        }

        @Override
        public LocationProvider createLocationServiceProvider() {
            return null;
        }
    }
}