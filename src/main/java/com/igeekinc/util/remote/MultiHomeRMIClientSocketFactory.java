/*
 * Copyright 2002-2014 iGeek, Inc.
 * All Rights Reserved
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.igeekinc.util.remote;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.rmi.server.RMIClientSocketFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MultiHomeRMIClientSocketFactory implements RMIClientSocketFactory,
		Serializable
{
	private static final long	serialVersionUID	= 2369257468705719200L;
	protected transient String resolvedHost = null;
	public MultiHomeRMIClientSocketFactory()
	{
		
	}
	
	public Socket createSocket(String hostListString, int port) throws IOException 
	{
        // We've determined that we can connect to this host but we didn't use
        // the right factory so we have to reconnect with the factory.
		return getSocket(hostListString, port);
    }

	private Socket getSocket(String hostListString, int port) throws IOException
	{
		boolean resolvedHostNotNull;
		synchronized(this)
		{
			resolvedHostNotNull = (resolvedHost != null);
		}
		Socket returnSocket;
		if (resolvedHostNotNull)
		{
			returnSocket = getSocket(resolvedHost, port, true);
			if (returnSocket != null)
				return returnSocket;
			// Resolved host went bad??
			resolvedHost = null;
		}
		synchronized(this)
		{
			returnSocket = getSocket(hostListString, port, true);
			if (returnSocket != null)
			{
				resolvedHost = returnSocket.getInetAddress().getHostAddress();
			}
		}
		return returnSocket;
	}
	
	public String getHostname(String hostListString, int port) throws IOException
	{
		if (resolvedHost != null)
			return resolvedHost;
		return getHostnameFromList(hostListString, port);
	}
	/**
	 * 
	 * 
	 * Thanks to NAT and other nastiness we will often have multi-homed hosts with some of the IP addresses blocked.  Takes a ! separated
     * list of hosts and determines which we can connect to.  Returns the a socket to the first host we can open a port to.
	 * @param hostListString
	 * @param port
	 * @param alwaysOpen
	 * @return
	 * @throws IOException
	 */
	public static Socket getSocket(String hostListString, int port, boolean alwaysOpen) throws IOException
	{
    	String[] hosts;
    	hosts = hostListString.split("!");
        if (hosts.length < 2 && ! alwaysOpen)
            return null;

        List<IOException> exceptions = new ArrayList<IOException>();
        Selector selector = Selector.open();
        for (String host : hosts) 
        {
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);
            SocketAddress addr = new InetSocketAddress(host, port);
            channel.connect(addr);
        }
        SocketChannel connectedChannel = null;
        int maxWaits = 10;
        while (true) 
        {
            if (selector.keys().isEmpty()) 
            {
                throw new IOException("Connection failed for " + hostListString +
                        ": " + exceptions);
            }
            selector.select(1000L);  // you can add a timeout parameter in millseconds
            Set<SelectionKey> keys = selector.selectedKeys();
            if (!keys.isEmpty()) 
            {
            	for (SelectionKey key : keys) 
            	{
            		SocketChannel channel = (SocketChannel) key.channel();
            		key.cancel();
            		if (connectedChannel == null)
            		{
            			try {
            				channel.configureBlocking(true);
            				channel.finishConnect();
            				connectedChannel = channel;
            			} catch (IOException e) {
            				exceptions.add(e);
            			}
            		}
            	}
            }
            else
            {
            	maxWaits --;
            	if (maxWaits <= 0)
            	{
            		throw new IOException("Selection keys unexpectedly empty for " +
            				hostListString + "[exceptions: " + exceptions + "]");
            	}
            }
            if (connectedChannel != null)
            	break;
        }
        
        assert connectedChannel != null;
        
        // Close the channels that didn't connect
        for (SelectionKey key : selector.keys()) 
        {
            Channel channel = key.channel();
            if (channel != connectedChannel)
            {
            	key.cancel();
                channel.close();
            }
        }
        
        selector.close();
        final Socket socket = connectedChannel.socket();
        return socket;
	}
	
    /**
     * Thanks to NAT and other nastiness we will often have multi-homed hosts with some of the IP addresses blocked.  Takes a ! separated
     * list of hosts and determines which we can connect to.  Returns the first host we can open a port to.  Use this for RMIFactories that
     * need to open the socket themselves (e.g. SSL connections).  This will add an extra socket connection at the beginning.  Oh well.
     * @param hostListString
     * @param port
     * @return
     * @throws IOException
     */
    public static String getHostnameFromList(String hostListString, int port) throws IOException
    {
    	Socket checkSocket = getSocket(hostListString, port, false);
    	String host;
    	if (checkSocket != null)
    	{
    		// We've determined that we can connect to this host but we didn't use
    		// the right factory so we have to reconnect with the factory.
    		host = checkSocket.getInetAddress().getHostAddress();
    		checkSocket.close();
    	}
    	else
    	{
    		// If it returns null that's because there's no use in futzing about to find the socket
    		host = hostListString;
    	}
        return host;
    }

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((resolvedHost == null) ? 0 : resolvedHost.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MultiHomeRMIClientSocketFactory other = (MultiHomeRMIClientSocketFactory) obj;
		if (resolvedHost == null)
		{
			if (other.resolvedHost != null)
				return false;
		} else if (!resolvedHost.equals(other.resolvedHost))
			return false;
		return true;
	}
}
