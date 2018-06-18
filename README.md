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
The first dataset being the lowest nodeId and the last being the highest. To
configure how many nodes are on the network, edit 
[loggerconfig.xml](jco-datalogger/code/loggerconfig.xml). Extra nodes that
aren't physically connected will show up as `{"x":0,"y":0,"z":0}` in the
data stream.


## API  
Currently the controller for the data recorder portion of the application
sits on port 7331. Here are valid commands:  
+ `recording=start\n`: Starts recording .csv files.  
+ `recording=stop\n`: Stops recording .csv files.
+ `status\n`: Returns a brief status message that includes:  
	Recording/Not Recording  
	Samples per file  
	Base Filename  
	Start time (if recording)  
	Runtime (if recording)  
+ `numSamples=$sampleSize\n`: Sets the number of samples(lines after header info) in each .csv file, where `$sampleSize` is the desired amount.
+ `fileName=$fileName\n`: Sets the base file name for the .csv files. The logger will append three digits (`000`,`001`,etc) at the end of each file as new files are created.  
+ `clearData=\n`: Deletes any .csv files in the set data directory. Be careful when using this. The data directory is set with the `-dir` option at startup. 
## Other Repositories:
The original jco-datalogger can be found 
[here](https://github.com/mpcrowe/jco-datalogger).  

The canopen-raspberrypi project(which this application sits on top of) can
be found [here](https://github.com/mpcrowe/canopen-raspberrypi).  

The WebSocket implementation used can be found
[here](https://github.com/TooTallNate/Java-WebSocket). 
