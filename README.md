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
[loggerconfig.xml](https://github.com/Awalrod/jco-datalogger/blob/master/code/loggerconfig.xml).
Any other application can open a WebSocket at the correct address and
recieve a stream of JSON data from the logger.
[streamExample.html](https://github.com/Awalrod/jco-datalogger/blob/master/code/examples/streamExample.html) is an example
written in javascript using the [d3.js](https://d3js.org/) library for
graphing. [index.html](https://github.com/Awalrod/jco-datalogger/blob/master/code/examples/index.html) is a more
robust dashboard-style monitor for the logger.

The JSON object sent is in the form:  
`[{"x":0,"y":0,"z":0},{"x":-29362,"y":64095,"z":245445},{"x":-155975,"y":-84501,"z":185922},{"x":78515,"y":124510,"z":206242},{"x":0,"y":0,"z":0},{"x":0,"y":0,"z":0},{"x":0,"y":0,"z":0}]`  
The first dataset being the lowest nodeId and the last being the highest. To
configure how many nodes are on the network, edit 
[loggerconfig.xml](jco-datalogger/code/loggerconfig.xml). Extra nodes that
aren't physically connected will show up as `{"x":0,"y":0,"z":0}` in the
data stream.

## Startup Options
Options:

+ `-a`      Sets the IF socat address.  
        default: None(REQUIRED)  
        alias: --address  
        example: java -jar can-0v1.jar -a '10.10.3.1'  

+ `-b`      Sets the base name for the data files. The resulting
        filename will be [base-name][nnn].csv. [nnn] is the nth
        data file created in this specific session. Note that   
        can-0v1 will overwrite files with the same name.  
        default: DATA_  
        alias: --base-name  
        example: java -jar can-0v1.jar  -a [address] -b Experiment_  

+ `-d`     Enables debugging statements. These include various  
        status updates and output to the data files  
        default: off  
        alias: --debug  
        example: java -jar can-0v1.jar  -a [address] -d  

+ `-dir`    Sets the directory in which the data files will be placed.   
        IMPORTANT: This is also the directory that the cgi-bin  
                interface will clear when commanded to. If you set a   
                directory besides /var/www/html/data be sure you are careful 
                when clearing it. Make sure there are no .csv files in there 
                that you want to keep.  
        default: ./
        alias: --directory
        example: java -jar can-0v1.jar  -a [address] -dir /var/www/html/data

+ `-f`      Sets the maximum number of data files to create. After
        this number is reached, can-0v1 will exit quietly.
        default: off
        alias: --file-limit
        example: java -jar can-0v1.jar -a [address] -f 5

+ `-help`   Brings up this help screen
        example: java -jar can-0v1 -help

+ `-i`      Enables infinite data file. The logger will
        write to a single data file until it is stopped.
        The logger can be stopped through the -t option or
        the cgi-bin interface
        default: off
        alias: --infinite
        example: java -jar can-0v1.jar -a [address] -i

+ `-l`      Sets the maximum number of samples per data file
        Cannot be set when -i is used.
        default: 1000
        alias: --length
        example: java -jar can-0v1.jar  -a [address] -l 2500

+ `-t`      Sets a period of time in milliseconds for the logger to run
        After the specified time can-0v1 will exit quietly.
        default: off
        alias: -timed, --runtime
        example: java -jar can-0v1.jar  -a [address] -t 5000

+ `-s`      If set then the logger will start recording data immediately.
        If this flag is not set the logger will wait for a 
        command from the cgi-bin interface to begin recording
        default: off
        alias: --start, --start-immediately

+ `help`    Brings up the args4j generated helpscreen
        example: java -jar can-0v1 help



## Runtime Options 
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
+ `fileRequest`: Returns a list of .csv files in the data directory with info about each file. A typical Json message would look like: `{"fileList": [["DATA_007.csv","2018-06-26T14:55:37Z","500","46230"],["DATA_000.csv","2018-06-26T14:53:06Z","700","64631"]]}`. Each array contains `["file name", "timestamp(generated by the pi)", "Number of samples", "number of bytes"]`  
+ `sampleRate=$sampleRate`: Sets the sample rate of the busmaster. The max sample rate is 200hz.



The Busmaster has a local socket at 7332 (not a websocket server) and accepts xml statemens as commands to change the object
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
+ `stream=true/false`: Turns streaming on or off
+ `nodeid=$nodeid`: Changes nodeData that gets streamed. `$nodeId` is the index of the node in the list of nodes provided in `loggerconfig.xml`, NOT the actual node ID.  
+ `nodeid=all` will send all the node data.  


## Other Repositories:
The original jco-datalogger can be found 
[here](https://github.com/mpcrowe/jco-datalogger).  

The canopen-raspberrypi project(which this application sits on top of) can
be found [here](https://github.com/mpcrowe/canopen-raspberrypi).  

The WebSocket implementation used can be found
[here](https://github.com/TooTallNate/Java-WebSocket). 
