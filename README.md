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
Currently the controller for the data recording portion of the application
sits on a websocket server at port 7331. Here are valid commands:  
+ `recording=start`: Starts recording .csv files.  
+ `recording=stop`: Stops recording .csv files.
<!---+ `status\n`: Returns a brief status message that includes:  
	Recording/Not Recording  
	Samples per file  
	Base Filename  
	Start time (if recording)  
	Runtime (if recording)
--->  
+ `numSamples=$sampleSize`: Sets the number of samples(lines after header info) in each .csv file, where `$sampleSize` is the desired amount.
+ `fileName=$fileName`: Sets the base file name for the .csv files. The logger will append three digits (`000`,`001`,etc) at the end of each file as new files are created.  
+ `clearData`: Deletes any .csv files in the set data directory. Be careful when using this. The data directory is set with the `-dir` option at startup. 
+ `fileRequest`: Returns a list of .cvs files in the set data directory with info about each file



The Busmaster sits at 7332 and accepts xml statemens as commands to change the object
dictionary. Example of changing the sample rate:  
`<?xml version="1.0" encodeing="UTF-8'?>  
<config>  
	<slave_nodes>  
		<node id="local">  
			<nodeid>local</nodeid>  
			<obj_dict>  
				<index>0x1006</index>  
				<subindex>0</subindex>  
				<type>int32</type>  
				<val>$val</val>  
			</obj_dict>  
		</node>  
	</slave_nodes>  
</config>`  
Where `$val` is the desired time interval between samples in microseconds.  

The WebSocket server sits at port 7333 and accepts commands to control the data stream.
+ `stream?=true/false`: Turns streaming on or off
+ `nodeid?=$nodeid`: Changes nodeData that gets streamed. `$nodeId` is the index of the node in the list of nodes provided in `loggerconfig.xml`, NOT the actual node ID.  
  `nodeid?=all` will send all the node data.  


## Other Repositories:
The original jco-datalogger can be found 
[here](https://github.com/mpcrowe/jco-datalogger).  

The canopen-raspberrypi project(which this application sits on top of) can
be found [here](https://github.com/mpcrowe/canopen-raspberrypi).  

The WebSocket implementation used can be found
[here](https://github.com/TooTallNate/Java-WebSocket). 
