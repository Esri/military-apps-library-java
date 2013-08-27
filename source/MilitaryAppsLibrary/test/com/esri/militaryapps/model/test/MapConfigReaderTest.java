package com.esri.militaryapps.model.test;

import com.esri.militaryapps.model.BasemapLayerInfo;
import com.esri.militaryapps.model.LayerInfo;
import com.esri.militaryapps.model.LayerType;
import com.esri.militaryapps.model.MapConfig;
import com.esri.militaryapps.model.MapConfigReader;
import com.esri.militaryapps.util.ResourceCopier;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
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
        List<BasemapLayerInfo> basemapLayers = mapConfig.getBasemapLayers();
        final int basemapLayerCount = 4;
        Assert.assertEquals("MapConfig should have " + basemapLayerCount + " basemap layers", basemapLayerCount, basemapLayers.size());
        
        final String layer0Name = "Topo";
        final boolean layer0Visible = false;
        final LayerType layer0Type = LayerType.TILED_CACHE;
        final String layer0DatasetPath = "/data/JbadBasemaps/Topographic.tpk";
        Assert.assertEquals("Layer 0 name should be " + layer0Name, layer0Name, basemapLayers.get(0).getName());
        Assert.assertEquals("Layer 0 visible should be " + layer0Visible, layer0Visible, basemapLayers.get(0).isVisible());
        Assert.assertEquals("Layer 0 type should be " + layer0Type, layer0Type, basemapLayers.get(0).getLayerType());
        Assert.assertEquals("Layer 0 dataset path should be " + layer0DatasetPath, layer0DatasetPath, basemapLayers.get(0).getDatasetPath());
        
        final String layer1Name = "Imagery";
        final boolean layer1Visible = true;
        final LayerType layer1Type = LayerType.TILED_CACHE;
        final String layer1DatasetPath = "C:/Example/basemaps/Imagery";
        Assert.assertEquals("Layer 1 name should be " + layer1Name, layer1Name, basemapLayers.get(1).getName());
        Assert.assertEquals("Layer 1 visible should be " + layer1Visible, layer1Visible, basemapLayers.get(1).isVisible());
        Assert.assertEquals("Layer 1 type should be " + layer1Type, layer1Type, basemapLayers.get(1).getLayerType());
        Assert.assertEquals("Layer 1 dataset path should be " + layer1DatasetPath, layer1DatasetPath, basemapLayers.get(1).getDatasetPath());
                
        final String layer2Name = "National Geographic";
        final boolean layer2Visible = false;
        final LayerType layer2Type = LayerType.TILED_MAP_SERVICE;
        final String layer2DatasetPath = "http://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer";
        Assert.assertEquals("Layer 2 name should be " + layer2Name, layer2Name, basemapLayers.get(2).getName());
        Assert.assertEquals("Layer 2 visible should be " + layer2Visible, layer2Visible, basemapLayers.get(2).isVisible());
        Assert.assertEquals("Layer 2 type should be " + layer2Type, layer2Type, basemapLayers.get(2).getLayerType());
        Assert.assertEquals("Layer 2 dataset path should be " + layer2DatasetPath, layer2DatasetPath, basemapLayers.get(2).getDatasetPath());
        
        final String layer3Name = "USA";
        final boolean layer3Visible = false;
        final LayerType layer3Type = LayerType.DYNAMIC_MAP_SERVICE;
        final String layer3DatasetPath = "http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/Specialty/ESRI_StateCityHighway_USA/MapServer";
        Assert.assertEquals("Layer 3 name should be " + layer3Name, layer3Name, basemapLayers.get(3).getName());
        Assert.assertEquals("Layer 3 visible should be " + layer3Visible, layer3Visible, basemapLayers.get(3).isVisible());
        Assert.assertEquals("Layer 3 type should be " + layer3Type, layer3Type, basemapLayers.get(3).getLayerType());
        Assert.assertEquals("Layer 3 dataset path should be " + layer3DatasetPath, layer3DatasetPath, basemapLayers.get(3).getDatasetPath());
        
        List<LayerInfo> nonBasemapLayers = mapConfig.getNonBasemapLayers();
        final int nonBasemapLayerCount = 2;
        Assert.assertEquals("MapConfig should have " + nonBasemapLayerCount + " non-basemap layers", nonBasemapLayerCount, nonBasemapLayers.size());
        
        final String layer4Name = "Bihsud Bridge and Environs";
        final boolean layer4Visible = true;
        final LayerType layer4Type = LayerType.LOCAL_DYNAMIC_MAP;
        final String layer4DatasetPath = "../../BihsudBridgeVCP.mpk";
        Assert.assertEquals("Layer 4 name should be " + layer4Name, layer4Name, nonBasemapLayers.get(0).getName());
        Assert.assertEquals("Layer 4 visible should be " + layer4Visible, layer4Visible, nonBasemapLayers.get(0).isVisible());
        Assert.assertEquals("Layer 4 type should be " + layer4Type, layer4Type, nonBasemapLayers.get(0).getLayerType());
        Assert.assertEquals("Layer 4 dataset path should be " + layer4DatasetPath, layer4DatasetPath, nonBasemapLayers.get(0).getDatasetPath());
        Assert.assertFalse("Layer 4 should not be a BasemapLayerInfo", nonBasemapLayers.get(0) instanceof BasemapLayerInfo);
        
        final String layer5Name = "COA Sketch";
        final boolean layer5Visible = false;
        final LayerType layer5Type = LayerType.MIL2525C_MESSAGE;
        final String layer5DatasetPath = "../../data/coa.xml";
        Assert.assertEquals("Layer 5 name should be " + layer5Name, layer5Name, nonBasemapLayers.get(1).getName());
        Assert.assertEquals("Layer 5 visible should be " + layer5Visible, layer5Visible, nonBasemapLayers.get(1).isVisible());
        Assert.assertEquals("Layer 5 type should be " + layer5Type, layer5Type, nonBasemapLayers.get(1).getLayerType());
        Assert.assertEquals("Layer 5 dataset path should be " + layer5DatasetPath, layer5DatasetPath, nonBasemapLayers.get(1).getDatasetPath());
        Assert.assertFalse("Layer 5 should not be a BasemapLayerInfo", nonBasemapLayers.get(1) instanceof BasemapLayerInfo);
        
        //The easiest way to test the thumbnail is like this:
        final String thumbnailFilename = "ThumbnailTest.png";
        File thumbnailFile = ResourceCopier.copyResourceToTemp(thumbnailFilename, getClass().getResourceAsStream("/" + thumbnailFilename));
        thumbnailFile.deleteOnExit();
        BasemapLayerInfo basemapLayerInfo = new BasemapLayerInfo(thumbnailFile.getAbsolutePath());
        Assert.assertNotNull("Thumbnail should not be null", basemapLayerInfo.getThumbnailUrl());
    }
    
    @After
    public void tearDown() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
}