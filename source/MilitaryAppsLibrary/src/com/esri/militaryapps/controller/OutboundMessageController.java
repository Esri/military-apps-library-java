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

import com.esri.militaryapps.util.Utilities;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;

/**
 * A controller that sends messages to listening clients. This implementation sends
 * UDP broadcasts.
 */
public abstract class OutboundMessageController {

    private final DatagramSocket udpSocket;
    private final DatagramPacket packet;

    /**
     * Creates an OutboundMessageController for the given UDP port.
     * @param messagingPort the UDP port through which messages will be sent.
     * Usually you should use a port number between 1024 and 65535.
     * @see #setPort(int)
     */
    protected OutboundMessageController(int messagingPort) {
        DatagramSocket theSocket = null;
        DatagramPacket thePacket = null;
        try {
            theSocket = new DatagramSocket();
            thePacket = new DatagramPacket(new byte[0], 0, InetAddress.getByName("255.255.255.255"), messagingPort);
        } catch (IOException ex) {
            Logger.getLogger(OutboundMessageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        udpSocket = theSocket;
        packet = thePacket;
    }

    @Override
    protected void finalize() throws Throwable {
        if (null != udpSocket) {
            udpSocket.close();
        }
        super.finalize();
    }
    
    /**
     * Sets the UDP port used to send messages.
     * @param messagingPort the UDP port through which messages will be sent.
     * Usually you should use a port number between 1024 and 65535. Numbers less
     * than 0 or greater than 65535 are not port numbers. Port number 0 is reserved.
     * Port numbers 1 to 1023 cannot be used on some operating systems without root
     * privileges.
     */
    public void setPort(int messagingPort) {
        if (null != packet) {
            packet.setPort(messagingPort);
        }
    }
    
    /**
     * Sends a UDP broadcast.
     * @param doc the DOM document to be converted to a string and broadcast.
     */
    public void sendMessage(Document doc) throws TransformerException, IOException {
        sendMessage(Utilities.documentToString(doc).getBytes());
    }
    
    /**
     * Sends a UDP broadcast.
     * @param bytes the message.
     * @throws IOException if the message cannot be sent.
     */
    public void sendMessage(byte[] bytes) throws IOException {
        synchronized (packet) {
            packet.setData(bytes);
            packet.setLength(bytes.length);
            udpSocket.send(packet);
        }
    }
    
    public abstract String getTypePropertyName();
    public abstract String getIdPropertyName();
    public abstract String getWkidPropertyName();
    public abstract String getControlPointsPropertyName();
    public abstract String getActionPropertyName();
    public abstract String getSymbolIdCodePropertyName();
    
}
