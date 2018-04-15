# Homing Beacon Map
Homing Beacon Map provide a solution for helping to locate people who needs to be rescued more efficiently and accurately.

# Overview
Homing Beacon Map actually is a composite of three parts: **Beacon**, **Relay**, and **Locating Map**.

![Overview Image](https://github.com/vritvij/HB5/HBM.png)

* **Beacon** is carried by end user. When the user believe he's in danger he can turn on "SOS" button on the beacon, then beacon will send out signal including GPS and unique ID.

* **Relay** is owned by first responder or other rescue organizations. It can be attached to drone or other devices that sent to assigned area, and detect the signal from beacons. Once signal is received, it can send the information to base server.

* **Locating Map** is a front-end application provided for first responder or other rescue organizations. It acquire location data from base server and draw a locating map helping rescuer to find victim and dispatch rescue power more efficiently.

# Coding
## Beacon
hb5-beacon is beacon project. In this hackathon we implemented an Android App using Eddystone open beacon format to work as a beacon.

## Relay
HB5relay is relay project. We implemented an Android App also using Eddystone open beacon format to work as a receiver and transmitter.

## Base Server
node-server is server project. We implemented a node server in AWS to work as a server getting data from Relay and sending data to Map application.

## Map
MapFronted is Locating Map project. We implemented a real-time map application using Google Maps and Visualization API along with barebones HTML, Less/Css and Javascript.

# Running Instruction
Consists of 4 components

Map
 - Open MapFrontend/index.html in any browser (already configured to load data from the node server hosted on Amazon EC2)

node-server - This is an REST API server hosted on Amazon EC2 at http://18.217.115.154:3000
 - In the case you need to run this locally, you can use the ''' node index.js ''' and start mongodb using ''' mongod --port 23456 '''. Also you may need to change the address the WebFrontend and relay are pointed at.

Relay
 - Compile and run the Android app in the HB5relay folder to start the relay (Need to enable bluetooth and internet access)

Beacon
 - Compile and run the Android app in the hb5-beacon folder.
 - Make sure your Bluetooth service is activated and Location permissions are granted (manually through settings)
 - Press the SOS button to start broadcasting your coordinates.
 
You will see that the Frontend will be updated to reflect the Beacon's location