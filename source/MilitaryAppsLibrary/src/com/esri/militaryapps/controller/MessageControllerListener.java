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
package com.esri.militaryapps.controller;

import com.esri.militaryapps.model.Geomessage;

/**
 * Interface for classes that listen for MessageController events, such as receiving
 * new messages.
 */
public interface MessageControllerListener {
    
    /**
     * Called when a Geomessage is received. One datagram contains zero or more Geomessages.
     * @param geomessage the Geomessage received.
     */
    void geomessageReceived(Geomessage geomessage);
    
    /**
     * Called when a datagram is received. Ideally the datagram contains Geomessages
     * in XML, but the datagram may contain any text, which may or may not be in XML
     * and may or may not be meaningful.
     * @param contents the datagram contents.
     */
    void datagramReceived(String contents);
}
