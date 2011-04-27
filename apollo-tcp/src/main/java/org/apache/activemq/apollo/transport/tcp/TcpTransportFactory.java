/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.apollo.transport.tcp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.activemq.apollo.transport.Transport;
import org.apache.activemq.apollo.transport.TransportFactory;
//import org.apache.activemq.transport.TransportLoggerFactory;
import org.apache.activemq.apollo.transport.TransportServer;
import org.apache.activemq.apollo.util.IntrospectionSupport;
import org.apache.activemq.apollo.util.URISupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.activemq.apollo.transport.TransportFactorySupport.configure;
import static org.apache.activemq.apollo.transport.TransportFactorySupport.verify;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 * @author David Martin Clavo david(dot)martin(dot)clavo(at)gmail.com (logging improvement modifications)
 */
public class TcpTransportFactory implements TransportFactory.Provider {
    private static final Logger LOG = LoggerFactory.getLogger(TcpTransportFactory.class);

    public TransportServer bind(String location) throws Exception {

        URI uri = new URI(location);
        TcpTransportServer server = createTcpTransportServer(uri);
        if (server == null) return null;

        Map<String, String> options = new HashMap<String, String>(URISupport.parseParamters(uri));
        IntrospectionSupport.setProperties(server, options);
        server.setTransportOption(options);
        return server;
    }


    public Transport connect(String location) throws Exception {
        URI uri = new URI(location);
        TcpTransport transport = createTransport(uri);
        if (transport == null) return null;

        Map<String, String> options = new HashMap<String, String>(URISupport.parseParamters(uri));
        URI localLocation = getLocalLocation(uri);

        transport.connecting(uri, localLocation);

        Map<String, Object> socketOptions = IntrospectionSupport.extractProperties(options, "socket.");
        transport.setSocketOptions(socketOptions);

        configure(transport, options);
        return verify(transport, options);
    }

    /**
     * Allows subclasses of TcpTransportFactory to create custom instances of
     * TcpTransportServer.
     */
    protected TcpTransportServer createTcpTransportServer(final URI location) throws IOException, URISyntaxException, Exception {
        if( !location.getScheme().equals("tcp") ) {
            return null;
        }
        return new TcpTransportServer(location);
    }

    /**
     * Allows subclasses of TcpTransportFactory to create custom instances of
     * TcpTransport.
     */
    protected TcpTransport createTransport(URI uri) throws NoSuchAlgorithmException, Exception {
        if( !uri.getScheme().equals("tcp") ) {
            return null;
        }
        TcpTransport transport = new TcpTransport();
        return transport;
    }

    protected URI getLocalLocation(URI location) {
        URI localLocation = null;
        String path = location.getPath();
        // see if the path is a local URI location
        if (path != null && path.length() > 0) {
            int localPortIndex = path.indexOf(':');
            try {
                Integer.parseInt(path.substring(localPortIndex + 1, path.length()));
                String localString = location.getScheme() + ":/" + path;
                localLocation = new URI(localString);
            } catch (Exception e) {
                LOG.warn("path isn't a valid local location for TcpTransport to use", e);
            }
        }
        return localLocation;
    }

    protected String getOption(Map options, String key, String def) {
        String rc = (String) options.remove(key);
        if( rc == null ) {
            rc = def;
        }
        return rc;
    }

}
