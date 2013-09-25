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
package com.esri.militaryapps.model.test;

import com.esri.militaryapps.model.BasemapLayerInfo;
import com.esri.militaryapps.model.LayerInfo;
import com.esri.militaryapps.model.LayerType;
import com.esri.militaryapps.model.MapConfig;
import com.esri.militaryapps.model.MapConfigReader;
import com.esri.militaryapps.util.ResourceCopier;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * A test for the MapConfigReader class.
 * @see com.esri.militaryapps.model.MapConfigReader
 */
public class MapConfigReaderTest {
    
    private static final Logger logger = Logger.getLogger(MapConfigReaderTest.class.getName());
    
    public MapConfigReaderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @Test
    public void testReadMapConfig() throws IOException, ParserConfigurationException, SAXException, URISyntaxException {
        URL resource = getClass().getResource("/mapconfig_MapConfigReaderTest.xml");
        File mapConfigFile = new File(resource.toURI());
        MapConfig mapConfig = MapConfigReader.readMapConfig(mapConfigFile);
        testReadMapConfig(mapConfig, true);
    }
    
    private void testReadMapConfig(MapConfig mapConfig, boolean serializeAndRetest) throws IOException, ParserConfigurationException, SAXException, URISyntaxException {
        BasemapLayerInfo[] basemapLayers = mapConfig.getBasemapLayers();
        final int basemapLayerCount = 4;
        Assert.assertEquals("MapConfig should have " + basemapLayerCount + " basemap layers", basemapLayerCount, basemapLayers.length);
        
        final String layer0Name = "Topo";
        final boolean layer0Visible = false;
        final LayerType layer0Type = LayerType.TILED_CACHE;
        final String layer0DatasetPath = "/data/JbadBasemaps/Topographic.tpk";
        Assert.assertEquals("Layer 0 name should be " + layer0Name, layer0Name, basemapLayers[0].getName());
        Assert.assertEquals("Layer 0 visible should be " + layer0Visible, layer0Visible, basemapLayers[0].isVisible());
        Assert.assertEquals("Layer 0 type should be " + layer0Type, layer0Type, basemapLayers[0].getLayerType());
        Assert.assertEquals("Layer 0 dataset path should be " + layer0DatasetPath, layer0DatasetPath, basemapLayers[0].getDatasetPath());
        
        final String layer1Name = "Imagery";
        final boolean layer1Visible = true;
        final LayerType layer1Type = LayerType.TILED_CACHE;
        final String layer1DatasetPath = "C:/Example/basemaps/Imagery";
        Assert.assertEquals("Layer 1 name should be " + layer1Name, layer1Name, basemapLayers[1].getName());
        Assert.assertEquals("Layer 1 visible should be " + layer1Visible, layer1Visible, basemapLayers[1].isVisible());
        Assert.assertEquals("Layer 1 type should be " + layer1Type, layer1Type, basemapLayers[1].getLayerType());
        Assert.assertEquals("Layer 1 dataset path should be " + layer1DatasetPath, layer1DatasetPath, basemapLayers[1].getDatasetPath());
                
        final String layer2Name = "National Geographic";
        final boolean layer2Visible = false;
        final LayerType layer2Type = LayerType.TILED_MAP_SERVICE;
        final String layer2DatasetPath = "http://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer";
        Assert.assertEquals("Layer 2 name should be " + layer2Name, layer2Name, basemapLayers[2].getName());
        Assert.assertEquals("Layer 2 visible should be " + layer2Visible, layer2Visible, basemapLayers[2].isVisible());
        Assert.assertEquals("Layer 2 type should be " + layer2Type, layer2Type, basemapLayers[2].getLayerType());
        Assert.assertEquals("Layer 2 dataset path should be " + layer2DatasetPath, layer2DatasetPath, basemapLayers[2].getDatasetPath());
        
        final String layer3Name = "USA";
        final boolean layer3Visible = false;
        final LayerType layer3Type = LayerType.DYNAMIC_MAP_SERVICE;
        final String layer3DatasetPath = "http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/Specialty/ESRI_StateCityHighway_USA/MapServer";
        Assert.assertEquals("Layer 3 name should be " + layer3Name, layer3Name, basemapLayers[3].getName());
        Assert.assertEquals("Layer 3 visible should be " + layer3Visible, layer3Visible, basemapLayers[3].isVisible());
        Assert.assertEquals("Layer 3 type should be " + layer3Type, layer3Type, basemapLayers[3].getLayerType());
        Assert.assertEquals("Layer 3 dataset path should be " + layer3DatasetPath, layer3DatasetPath, basemapLayers[3].getDatasetPath());
        
        LayerInfo[] nonBasemapLayers = mapConfig.getNonBasemapLayers();
        final int nonBasemapLayerCount = 2;
        Assert.assertEquals("MapConfig should have " + nonBasemapLayerCount + " non-basemap layers", nonBasemapLayerCount, nonBasemapLayers.length);
        
        final String layer4Name = "Bihsud Bridge and Environs";
        final boolean layer4Visible = true;
        final LayerType layer4Type = LayerType.LOCAL_DYNAMIC_MAP;
        final String layer4DatasetPath = "../../BihsudBridgeVCP.mpk";
        Assert.assertEquals("Layer 4 name should be " + layer4Name, layer4Name, nonBasemapLayers[0].getName());
        Assert.assertEquals("Layer 4 visible should be " + layer4Visible, layer4Visible, nonBasemapLayers[0].isVisible());
        Assert.assertEquals("Layer 4 type should be " + layer4Type, layer4Type, nonBasemapLayers[0].getLayerType());
        Assert.assertEquals("Layer 4 dataset path should be " + layer4DatasetPath, layer4DatasetPath, nonBasemapLayers[0].getDatasetPath());
        Assert.assertFalse("Layer 4 should not be a BasemapLayerInfo", nonBasemapLayers[0] instanceof BasemapLayerInfo);
        
        final String layer5Name = "COA Sketch";
        final boolean layer5Visible = false;
        final LayerType layer5Type = LayerType.MIL2525C_MESSAGE;
        final String layer5DatasetPath = "../../data/coa.xml";
        Assert.assertEquals("Layer 5 name should be " + layer5Name, layer5Name, nonBasemapLayers[1].getName());
        Assert.assertEquals("Layer 5 visible should be " + layer5Visible, layer5Visible, nonBasemapLayers[1].isVisible());
        Assert.assertEquals("Layer 5 type should be " + layer5Type, layer5Type, nonBasemapLayers[1].getLayerType());
        Assert.assertEquals("Layer 5 dataset path should be " + layer5DatasetPath, layer5DatasetPath, nonBasemapLayers[1].getDatasetPath());
        Assert.assertFalse("Layer 5 should not be a BasemapLayerInfo", nonBasemapLayers[1] instanceof BasemapLayerInfo);
        
        //The easiest way to test the thumbnail is like this:
        final String thumbnailFilename = "ThumbnailTest.png";
        File thumbnailFile = ResourceCopier.copyResourceToTemp(thumbnailFilename, getClass().getResourceAsStream("/" + thumbnailFilename));
        thumbnailFile.deleteOnExit();
        BasemapLayerInfo basemapLayerInfo = new BasemapLayerInfo(thumbnailFile.getAbsolutePath());
        Assert.assertNotNull("Thumbnail should not be null", basemapLayerInfo.getThumbnailUrl());
        
        if (serializeAndRetest) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(mapConfig);
            String serializedString = baos.toString();
            Assert.assertNotNull("Serialization output shouldn't be null", serializedString);
            ByteArrayInputStream bais = new ByteArrayInputStream(serializedString.getBytes());
            ObjectInputStream ois = new ObjectInputStream(bais);
            try {
                Object deserializedObj = ois.readObject();
                Assert.assertSame("Deserialized object should be of class " + MapConfig.class.getSimpleName(), MapConfig.class, deserializedObj.getClass());
                mapConfig = (MapConfig) deserializedObj;
                testReadMapConfig(mapConfig, false);
            } catch (ClassNotFoundException ex) {
                Assert.fail("Could not deserialize string " + serializedString);
            }
        }
    }
    
    @After
    public void tearDown() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
}