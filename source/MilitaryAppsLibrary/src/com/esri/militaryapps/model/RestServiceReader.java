package com.esri.militaryapps.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class for reading a RESTful service, determining what type of service it is,
 * and creating LayerInfo objects if applicable.
 */
public class RestServiceReader {
    
    /**
     * Reads a REST service endpoint, determines what type of service it is if possible,
     * and creates and returns an array of corresponding LayerInfo objects if applicable.
     * @param url the REST service endpoint URL.
     * @param useAsBasemap
     * @return an array of LayerInfo objects. The length of the array will be one
     *         unless it's a multi-layer service such as a feature service.
     */
    public static LayerInfo[] readService(URL url, boolean useAsBasemap) throws Exception {
        final String urlString = url.toString();
        StringBuilder urlSb = new StringBuilder(urlString);
        if (0 > urlString.indexOf('?')) {
            urlSb.append("?");
        } else {
            urlSb.append("&");
        }
        urlSb.append("f=json");
        url = new URL(urlSb.toString());
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder contentSb = new StringBuilder();
        String line;
        while (null != (line = reader.readLine())) {
            contentSb.append(line);
        }
        
        JSONObject jsonObject = new JSONObject(contentSb.toString());
        LayerInfo layerInfo = useAsBasemap ? new BasemapLayerInfo((String) null) : new LayerInfo();
        layerInfo.setDatasetPath(urlString);
        layerInfo.setVisible(true);
        ArrayList<LayerInfo> layerInfos = new ArrayList<LayerInfo>();
        if (jsonObject.has("singleFusedMapCache")) {
            //It's a map service
            if ("true".equals(jsonObject.getString("singleFusedMapCache"))) {
                layerInfo.setLayerType(LayerType.TILED_MAP_SERVICE);
            } else {
                layerInfo.setLayerType(LayerType.DYNAMIC_MAP_SERVICE);
            }
        } else if (jsonObject.has("layers")) {
            //It's a feature service; get all the layers
            layerInfo = null;
            JSONArray layersArray = jsonObject.getJSONArray("layers");
            for (int i = 0; i < layersArray.length(); i++) {
                try {
                    JSONObject layerJson = layersArray.getJSONObject(i);
                    LayerInfo thisLayerInfo = useAsBasemap ? new BasemapLayerInfo((String) null) : new LayerInfo();
                    StringBuilder datasetPath = new StringBuilder(urlString);
                    if (!urlString.endsWith("/")) {
                        datasetPath.append("/");
                    }
                    datasetPath.append(layerJson.getInt("id"));
                    thisLayerInfo.setDatasetPath(datasetPath.toString());
                    thisLayerInfo.setName(layerJson.getString("name"));
                    thisLayerInfo.setVisible(true);
                    thisLayerInfo.setLayerType(LayerType.FEATURE_SERVICE);
                    layerInfos.add(thisLayerInfo);
                } catch (JSONException ex) {
                    Logger.getLogger(RestServiceReader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (jsonObject.has("drawingInfo")) {
            //It's a single feature service layer
            layerInfo.setDatasetPath(urlString);
            layerInfo.setName(jsonObject.getString("name"));
            layerInfo.setVisible(true);
            layerInfo.setLayerType(LayerType.FEATURE_SERVICE);
        } else if (jsonObject.has("pixelSizeX")) {
            //It's an image service
            layerInfo.setLayerType(LayerType.IMAGE_SERVICE);
        } else {
            throw new Exception("Unsupported service type: " + urlString);
        }
        if (null != layerInfo) {
            try {
                if (jsonObject.has("documentInfo") && jsonObject.getJSONObject("documentInfo").has("Title")) {
                    layerInfo.setName(jsonObject.getJSONObject("documentInfo").getString("Title"));
                }
            } catch (JSONException ex) {
                Logger.getLogger(RestServiceReader.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (null == layerInfo.getName() && jsonObject.has("mapName")) {
                try {
                    layerInfo.setName(jsonObject.getString("mapName"));
                } catch (JSONException ex) {
                    Logger.getLogger(RestServiceReader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (null == layerInfo.getName() && jsonObject.has("name")) {
                try {
                    layerInfo.setName(jsonObject.getString("name"));
                } catch (JSONException ex) {
                    Logger.getLogger(RestServiceReader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            layerInfos.add(layerInfo);
        }
        return layerInfos.toArray(new LayerInfo[layerInfos.size()]);
    }
    
}
