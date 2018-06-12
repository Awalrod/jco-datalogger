# jco-datalogger
This is java based datalogger that uses the java canopen stack to connect and record data from a canopen bus.

[CAN-Open](http://www.can-cia.org/index.php?id=canopen) is protocol that sits above the 
[CAN-Bus Physical/Data layer](http://www.can-cia.org/index.php?id=systemdesign-can-physicallayer),
but it can also sit above other data layers.  By a using a clever tool such as [socat](http://linux.die.net/man/1/socat),
we can route CAN-Bus data traffic onto a datagram port, allowing devices such as cell phones and tablets direct access to the
CAN-Bus data layer.

This project uses a socket based can bus interface available on Linux type operating systems.  The socket based approach coupled with socat 
allows the datalogger to collect data from anywhere that the canbus socket is visable (ussually the local local ip subnet).  

The Canopen jar is not yet available as open source code, due to my own lazyness.  If you are interesting in working with it, let me know.

This fork is intended to implement livestreaming data

## Whats Different  
  
The data-logger app now starts a WebSocket server and streams data to 1 or
more clients. The address and port
of the server are set in
[loggerconfig.xml](jco-datalogger/code/loggerconfig.xml).
Any other application can open a WebSocket at the correct address and
recieve a stream of JSON data from the logger.
[streamExample.html](jco-datalogger/code/streamExample.html) is an example
written in javascript using the [d3.js](https://d3js.org/) library for graphing.  

The JSON object sent is in the form:  
`[{"x":0,"y":0,"z":0},{"x":-29362,"y":64095,"z":245445},{"x":-155975,"y":-84501,"z":185922},{"x":78515,"y":124510,"z":206242},{"x":0,"y":0,"z":0},{"x":0,"y":0,"z":0},{"x":0,"y":0,"z":0}]`  
The first dataset being the lowest nodeId and the last being the highest.  



## Other Repositories:
The original jco-datalogger can be found 
[here](https://github.com/mpcrowe/jco-datalogger).  

The canopen-raspberrypi project(which this application sits on top of) can
be found [here](https://github.com/mpcrowe/canopen-raspberrypi).  

The WebSocket implementation used can be found
[here](https://github.com/TooTallNate/Java-WebSocket). 
