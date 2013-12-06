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
import com.esri.militaryapps.model.GeomessagesReader;
import com.esri.militaryapps.util.Utilities;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A controller that sends messages to listening clients and receives inbound messages.
 * This implementation sends and receives UDP broadcasts.
 */
public class MessageController {
    
    private static final int MAX_MESSAGE_LENGTH = 6000;
    private static final Logger logger = Logger.getLogger(MessageController.class.getName());
    private static final HashMap<Integer, MessageController> portToController
            = new HashMap<Integer, MessageController>();

    private final DatagramSocket outboundUdpSocket;
    private final DatagramPacket outboundPacket;
    private final DatagramPacket inboundPacket;  
    private final Set<MessageControllerListener> listeners = new HashSet<MessageControllerListener>();
    private final GeomessagesReader reader;
    private final int port;
    
    private Thread inboundThread;
    private Boolean receiving = false;
    private final Object receivingLock = new Object();

    /**
     * Creates a MessageController for the given UDP port.
     * @param messagingPort the UDP port through which messages will be sent and received.
     * Usually you should use a port number between 1024 and 65535.
     * @see #setPort(int)
     */
    private MessageController(int messagingPort) {
        port = messagingPort;
        DatagramSocket theSocket = null;
        DatagramPacket thePacket = null;
        try {
            theSocket = new DatagramSocket();
            thePacket = new DatagramPacket(new byte[0], 0, InetAddress.getByName("255.255.255.255"), messagingPort);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        outboundUdpSocket = theSocket;
        outboundPacket = thePacket;
        
        theSocket = null;
        byte[] byteArray = new byte[MAX_MESSAGE_LENGTH];
        inboundPacket = new DatagramPacket(byteArray, MAX_MESSAGE_LENGTH);
        
        GeomessagesReader theReader = null;
        try {
            theReader = new GeomessagesReader();
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        reader = theReader;
    }

    @Override
    protected void finalize() throws Throwable {
        if (null != outboundUdpSocket) {
            outboundUdpSocket.close();
        }
        super.finalize();
    }
    
    public static MessageController getInstance(int port) {
        synchronized (portToController) {
            MessageController controller = portToController.get(port);
            if (null == controller) {
                controller = new MessageController(port);
                portToController.put(port, controller);
            }
            return controller;
        }
    }
    
    /**
     * Adds a listener to this controller.
     * @param listener the listener to add. If this controller already has this listener,
     *                 this method has no effect.
     * @return true if this controller did not already have this listener.
     */
    public boolean addListener(MessageControllerListener listener) {
        synchronized (listeners) {
            return listeners.add(listener);
        }
    }
    
    /**
     * Removes a listener from this controller.
     * @param listener the listener to remove. If this controller did not have this
     *                 listener, this method has no effect.
     * @return true if this controller had this listener.
     */
    public boolean removeListener(MessageControllerListener listener) {
        synchronized (listeners) {
            return listeners.remove(listener);
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
        synchronized (outboundPacket) {
            outboundPacket.setData(bytes);
            outboundPacket.setLength(bytes.length);
            outboundUdpSocket.send(outboundPacket);
        }
    }
    
    public void startReceiving() {
        synchronized (receivingLock) {
            if (receiving) {
                return;
            } else {
                receiving = true;
            }
        }
        inboundThread = new Thread() {
            
            private DatagramSocket inboundUdpSocket = null;

            @Override
            public void run() {
                try {
                    inboundUdpSocket = new DatagramSocket(port);
                    while (true) {
                        inboundUdpSocket.receive(inboundPacket);
                        final String msgString = new String(inboundPacket.getData(), inboundPacket.getOffset(), inboundPacket.getLength());
                        synchronized (listeners) {
                            for (final MessageControllerListener listener : listeners) {
                                new Thread() {

                                    @Override
                                    public void run() {
                                        listener.datagramReceived(msgString);
                                    }

                                }.start();
                            }
                        }
                        try {
                            List<Geomessage> messages = reader.parseMessages(msgString);
                            for (final Geomessage message : messages) {
                                synchronized (listeners) {
                                    for (final MessageControllerListener listener : listeners) {
                                        new Thread() {

                                            @Override
                                            public void run() {
                                                listener.geomessageReceived(message);
                                            }

                                        }.start();                                    
                                    }
                                }
                            }
                        } catch (SAXException ex) {
                            logger.log(Level.FINE, "Couldn't get Geomessages from string: '" + msgString + "'", ex);
                        } catch (IOException ex) {
                            logger.log(Level.FINE, "Couldn't get Geomessages from string: '" + msgString + "'", ex);
                        }
                    }
                } catch (SocketException ex) {
                    logger.log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        };
        inboundThread.start();
    }
    
}