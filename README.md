# DynaSOAr

## Features
* Automatically finds nodes on the network using Zeroconf/Bonjour (Multicast DNS)

## TODO
* jmDNS should bind to all the network interfaces rather than the default one only.
* Comm should also listen to all the network interfaces.
* ServiceMonitor should ignore all but json files in serviceConfig directory
* Node communicate using TCP Sockets
* Threads communicate using built-in event queue
* Load calculation and reporting for nodes
* File synchronization between nodes for dynamic service deployment
* Improve exception handling hierarchy

## Developer Reference
* Jetty APIDocs:	http://download.eclipse.org/jetty/8.0.4.v20111024/apidocs/

## Known issues
* Server hangs when immediately shut down after starting
