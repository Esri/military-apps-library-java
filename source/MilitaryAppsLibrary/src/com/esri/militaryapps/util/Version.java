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
package com.esri.militaryapps.util;

import java.util.ResourceBundle;

/**
 * A utility class through which applications can determine which version/build
 * of the Military Apps Library is being used.
 */
public class Version {
    
    private static final ResourceBundle versionProperties;
    static {
        ResourceBundle theVersionProperties = null;
        try {
            theVersionProperties = ResourceBundle.getBundle("version");
        } catch (Throwable t) {
            
        }
        versionProperties = theVersionProperties;
    }
    
    /**
     * Returns the build ID for this build of the application, or an empty string.
     * @return the build ID for this build of the application, or an empty string.
     */
    public static String getBuildId() {
        String build = null == versionProperties ? null : versionProperties.getString("BUILD");
        return (null == build) ? "" : build;
    }
    
    /**
     * A main method that simply prints the version number to the console and exits.
     */
    public static void main(String[] args) {
        System.out.println("Military Apps Library build " + getBuildId());
    }
    
}
