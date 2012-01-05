package org.dynasoar.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

public class NetworkUtil {
	private static Logger logger = Logger.getLogger(NetworkUtil.class);

	public static InetAddress getLocalNetworkAddress() throws SocketException {
		Enumeration e = NetworkInterface.getNetworkInterfaces();

		while (e.hasMoreElements()) {
			NetworkInterface ni = (NetworkInterface) e.nextElement();
			logger.debug("Net interface: " + ni.getName());

			if (ni.getName().equalsIgnoreCase("en0")
					|| ni.getName().equalsIgnoreCase("eth0")) {
				Enumeration e2 = ni.getInetAddresses();

				while (e2.hasMoreElements()) {
					InetAddress ip = (InetAddress) e2.nextElement();
					logger.debug("IP address: " + ip.toString());
					if (ip instanceof Inet4Address) {
						return ip;
					}
				}
			}
		}
		return null;
	}
}
