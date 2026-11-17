# Geriausias projektas

mes laimesime

## Prerequisites:
Download  IntelliJ:\
https://www.jetbrains.com/idea/download/

## How to run:
- Turn on IntelliJ, go to `File > Open` and click to open the pom.xml file in either server or client directory (they have to be opened separately). If you get a prompt saying "JDK is needed" then click install JDK and select at least version 17. After it is installed you SHOULD (hopefully) be able to run `GameServer.java` for a server instance or `GameClient.java` for client instances.\
- To run multiple client instances, with the client project opened go to `Run > Edit Configurations`, create a new application configuration, choose main class to be `GameClient of game.client`, hit `Modify options` right above it and select `Allow multiple instances`.