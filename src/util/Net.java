/*
 *
 */

package util;

/**
 *
 * @author Martin Pröhl alias MythGraphics
 * @version 1.1.0
 *
 */

import java.net.*;
import java.util.Collections;
import java.util.Enumeration;

public class Net {

    private Net() {}

    public static String getFileName(URL url) {
        String s = url.toString();
        int i = s.lastIndexOf('/');
        return s.substring(i+1);
    }

    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netInterface : Collections.list(interfaces)) {
                // Ignoriere inaktive Schnittstellen, Loopbacks und virtuelle Docker/VM-Adapter
                if ( !netInterface.isUp() || netInterface.isLoopback() || netInterface.isVirtual() ) {
                    continue;
                }

                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                for ( InetAddress addr : Collections.list( addresses )) {
                    // nur eine echte IPv4-Adresse
                    if ( addr instanceof Inet4Address && !addr.isLoopbackAddress() && !addr.isLinkLocalAddress() ) {
                        String hostAddress = addr.getHostAddress();

                        // Filter gängiger Netze
                        if ( hostAddress.startsWith( "192.168." ) ||
                             hostAddress.startsWith( "10." ) ||
                             hostAddress.startsWith( "172." )) {
                            return hostAddress;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        // Fallback, falls gar nichts gefunden wurde
        return "127.0.0.1";
    }

}
