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

import com.esri.militaryapps.controller.MapController;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A test for the MapController class.
 * @see com.esri.militaryapps.controller.MapController
 */
public class MapControllerTest {
    
    public MapControllerTest() {
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
     * Test of zoomIn and zoomOut methods, of class MapController.
     */
    @Test
    public void testZoomInAndOut() {
        System.out.println("zoomOut");
        MapControllerImpl instance = new MapControllerImpl();
        instance.zoomToScale(15000, 0, 0);
        assertEquals(15000, instance.getScale(), 0.0);
        instance.zoomOut();
        assertEquals(30000, instance.getScale(), 0.0);
        instance.zoomOut();
        assertEquals(60000, instance.getScale(), 0.0);
        instance.zoomIn();
        assertEquals(30000, instance.getScale(), 0.0);
        for (int i = 0; i < 5; i++) {
            instance.zoomIn();
        }
        assertEquals(937.5, instance.getScale(), 0.0);
        for (int i = 0; i < 6; i++) {
            instance.zoomOut();
        }
        assertEquals(60000, instance.getScale(), 0.0);
    }

    /**
     * Test of rotate method, of class MapController.
     */
    @Test
    public void testRotate() {
        MapController instance = new MapControllerImpl();
        instance.setRotation(0.0);
        assertEquals(0, instance.getRotation(), 0.0);
        instance.rotate(179);
        assertEquals(179, instance.getRotation(), 0.0);
        instance.rotate(2);
        assertEquals(-179, instance.getRotation(), 0.0);
        instance.rotate(179);
        assertEquals(0, instance.getRotation(), 0.0);
        instance.rotate(-1);
        assertEquals(-1, instance.getRotation(), 0.0);
        instance.rotate(-180);
        assertEquals(179, instance.getRotation(), 0.0);
    }

    /**
     * Test of zoomToScale method, of class MapController.
     */
    @Test
    public void testZoomToScale() {
        System.out.println("zoomToScale");
        MapControllerImpl instance = new MapControllerImpl();
        instance.zoom(15000);
        instance.zoomToScale(30000, 0, 0);
        assertEquals(30000, instance.getScale(), 0.0);
    }

    /**
     * Test of pan method, of class MapController.
     */
    @Test
    public void testPan() {
        System.out.println("pan");
        MapController.PanDirection direction = null;
        MapController instance = new MapControllerImpl();
        instance.pan(direction);
    }

    public class MapControllerImpl extends MapController {
        
        private double scale = 15000.0;
        private double rotation = 0.0;
        private boolean gridVisible = false;

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
        public boolean isReady() {
            return true;
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
        
    }
    
}