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
package com.esri.militaryapps.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copies resources, including files, to the temporary directory. For example, copies
 * tool and command icons from the compiled JAR file to the temporary directory.
 */
public class ResourceCopier {

    /**
     * Copies the resource specified by the InputStream to a temporary file.
     * @param filename the original resource's filename, in order to get the proper
     *                 filename extension; the rest of the name is ignored.
     * @param resource the resource to copy.
     * @return a temporary file.
     * @throws IOException 
     */
    public static File copyResourceToTemp(String filename, InputStream resource) throws IOException {
        String extension = null;
        String name = filename;
        int lastInd = name.lastIndexOf(".");
        if (0 <= lastInd) {
            extension = name.substring(lastInd);
        } else if (name.length() < 3) {
            extension = name;
        } else {
            extension = name.substring(name.length() - 3);
        }
        File tempFile = File.createTempFile("tmp", extension);
        tempFile.deleteOnExit();
        copy(resource, tempFile);
        return tempFile;
    }
    
    private static void copy(InputStream in, File dst) throws IOException {
        OutputStream out = new FileOutputStream(dst);
    
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
    
}
