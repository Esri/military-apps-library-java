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
package com.esri.militaryapps.controller;

import com.esri.militaryapps.model.Geomessage;
import com.esri.militaryapps.model.GeomessagesReader;
import com.esri.militaryapps.util.Utilities;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
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

    private final DatagramSocket outboundUdpSocket;
    private final DatagramPacket outboundPacket;
    private final DatagramPacket inboundPacket;
    private final Set<MessageControllerListener> listeners = new HashSet<MessageControllerListener>();
    private final GeomessagesReader reader;
    private final Object inboundLock = new Object();
    
    private Thread inboundThread = null;
    private DatagramSocket inboundUdpSocket = null;
    private int port;
    private String senderUsername;
    private Set<String> ownMessageTypesToIgnore = new HashSet<String>(Arrays.asList(
            "trackrep",
            "position_report"
            ));

    /**
     * Creates a MessageController for the given UDP port.
     * @param messagingPort the UDP port through which messages will be sent and received.
     * Usually you should use a port number between 1024 and 65535.
     * @param senderUsername a unique username for the user who is sending messages;
     *                       this does not modify outbound messages but causes MessageController
     *                       to treat the user's own messages properly when received.
     * @see #setPort(int)
     */
    public MessageController(int messagingPort, String senderUsername) {
        port = messagingPort;
        this.senderUsername = senderUsername;
        
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
        stopReceiving();
        super.finalize();
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
            Set<InetAddress> udpBroadcastAddresses = Utilities.getUdpBroadcastAddresses();
            for (InetAddress addr : udpBroadcastAddresses) {
                outboundPacket.setAddress(addr);
                outboundUdpSocket.send(outboundPacket);
            }
        }
        try {
            List<Geomessage> messages = reader.parseMessages(new String(bytes));
            synchronized (messages) {
                for (int i = 0; i < messages.size(); i++) {
                    Geomessage message = messages.get(i);
                    synchronized (listeners) {
                        for (MessageControllerListener listener : listeners) {
                            synchronized (ownMessageTypesToIgnore) {
                                if (!ownMessageTypesToIgnore.contains(message.getProperty("_type"))) {
                                    listener.geomessageReceived(message);
                                }
                            }
                        }
                    }
                }
            }
        } catch (SAXException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.INFO, "Couldn't parse messages; they might not be Geomessages (and that might be okay)", ex);
        }
    }
    
    /**
     * Tells this controller to bind a socket to the specified port and start
     * receiving messages, notifying this controller's listeners as appropriate.
     */
    public void startReceiving() {
        synchronized (inboundLock) {
            inboundThread = new Thread() {

                @Override
                public void run() {
                    try {
                        inboundUdpSocket = new DatagramSocket(port);
                        while (true) {
                            try {
                                inboundUdpSocket.receive(inboundPacket);
                            } catch (SocketException se) {
                                //This probably means the socket was closed and it's time to stop receiving.
                                break;
                            }
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
                                synchronized (messages) {
                                    for (final Geomessage message : messages) {
                                        synchronized (listeners) {
                                            for (final MessageControllerListener listener : listeners) {
                                                new Thread() {

                                                    @Override
                                                    public void run() {
                                                        if (null == senderUsername ||
                                                                !senderUsername.equals(message.getProperty("uniquedesignation"))) {
                                                            listener.geomessageReceived(message);
                                                        }
                                                    }

                                                }.start();                                    
                                            }
                                        }
                                    }
                                }
                            } catch (SAXException ex) {
                                logger.log(Level.FINE, "Couldn't get Geomessages from string: '" + msgString + "'", ex);
                            } catch (IOException ex) {
                                logger.log(Level.FINE, "Couldn't get Geomessages from string: '" + msgString + "'", ex);
                            }
                        }
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }

                @Override
                public void interrupt() {
                    synchronized (inboundLock) {
                        inboundUdpSocket.close();
                    }
                    super.interrupt();
                }
                
            };
            inboundThread.start();
        }
    }
    
    /**
     * Tells this controller to stop receiving messages, closing the socket in use.
     */
    public void stopReceiving() {
        if (null != inboundThread) {
            inboundThread.interrupt();
        }
    }
    
    /**
     * Returns this controller's UDP port.
     * @return this controller's UDP port.
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Sets this controller's UDP port. If the controller is currently receiving
     * messages and the new port is different, the controller releases the old port
     * and binds to the new port.
     * @param port 
     */
    public void setPort(int port) {
        synchronized (inboundLock) {
            boolean changed = this.port != port;
            this.port = port;
            outboundPacket.setPort(port);
            if (changed && null != inboundThread && inboundThread.isAlive()) {
                new Thread() {

                    @Override
                    public void run() {
                        stopReceiving();
                        startReceiving();
                    }

                }.start();
            }
        }
    }

    /**
     * @return the unique username for the user who is sending messages
     */
    public String getSenderUsername() {
        return senderUsername;
    }

    /**
     * @param senderUsername the unique username for the user who is sending messages.
     */
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    /**
     * @return the types of inbound messages being ignored when sent by the current
     *         user. Default is ["trackrep", "position_report"].
     */
    public Set<String> getOwnMessageTypesToIgnore() {
        return ownMessageTypesToIgnore;
    }

    /**
     * @param ownMessageTypesToIgnore the types of inbound messages to ignore when
     *                                sent by the current user. Default is
     *                                ["trackrep", "position_report"].
     */
    public void setOwnMessageTypesToIgnore(Set<String> ownMessageTypesToIgnore) {
        this.ownMessageTypesToIgnore = ownMessageTypesToIgnore;
    }
    
}
