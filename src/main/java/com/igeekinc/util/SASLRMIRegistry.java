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
 
package com.igeekinc.util;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SASLRMIRegistry implements Runnable
{
        // Constants and variables
        // ------------------------------------------------------------------------

   private static Logger logger = LogManager.getLogger(SASLRMIRegistry.class);

   // the default port for RMI
   private static final int DEFAULT_RMI_PORT = 1099;

        // Constructor(s)
        // ------------------------------------------------------------------------

   /**
    * Constructs an RMI registry listening on the default RMI port -1099- for
    * SASL connections.
    *
    * @exception RemoteException thrown when the registry can not be started,
    * due to unavailabiliy of port 1099.
    */
   private SASLRMIRegistry() throws RemoteException {
      this(DEFAULT_RMI_PORT);
   }

   /**
    * Constructs an RMI registry listening on the designated port for SASL
    * connections.
    *
    * @param port the port to start the registry on.
    * @exception RemoteException thrown when the registry can not be started,
    * due to an invalid or unavailable port.
    */
   private SASLRMIRegistry(int port) throws RemoteException {
      this(port, true);
   }

   /**
    * Constructs an RMI registry.
    *
    * @param port the port to start the registry on.
    * @param secure whether or not the registry is started using the SASL RMI
    * socket factories.
    * @exception RemoteException if the registry can not be started due to an
    * invalid or unavailable port.
    */
   private SASLRMIRegistry(int port, boolean secure) throws RemoteException {
      super();

      logger.info("Listen on port #"+String.valueOf(port)+" for "
         +(secure ? "" : "non-")+"SASL connection(s)...");

      /*if (secure)
         LocateRegistry.createRegistry(port,
                                       new SaslClientSocketFactory(),
                                       new SaslServerSocketFactory());
      else*/
         LocateRegistry.createRegistry(port);
   }

        // Class methods
        // ------------------------------------------------------------------------


        // Instance methods
        // ------------------------------------------------------------------------

   /** Starts the registry. */
   @Override
   public void run() {
      logger.info("RMIRegistry started on "+String.valueOf(new Date()));

      while (true) {
         try {
            Thread.sleep(5000);
         } catch (InterruptedException x) {
            break;
         } catch (Throwable x) {
            logger.fatal("run()", x);
            break;
         }
      }

      logger.info("RMIRegistry stopped on "+String.valueOf(new Date()));
   }
}