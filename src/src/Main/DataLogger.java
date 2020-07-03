package Main;

import DataFormatting.DataFormatter;
import DataRecording.AccelerometerReading;
import DataRecording.NodeTracker;
import GlobalVars.GlobalVars;
import Server.StreamServer;
import Server.ControllerServer;

import com.gcdc.can.CanMessage;
import com.gcdc.can.Driver;
import com.gcdc.can.DriverManager;
import com.gcdc.canopen.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.PrintWriter;

import java.nio.file.Paths;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.StandardCopyOption;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.StringTokenizer;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.NetworkInterface;
import java.net.SocketException;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.ReadableInstant;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;

// for parasing an xml config file
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.io.File;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.time.*;


/**
 * Created by gcdc on 6/7/17.
 */
public class DataLogger
{
	private CanOpen canOpen;
	public COListener coListener;
	private CanOpenThread coThread;

	private ArrayList<NodeTracker> nodes;

	private DataFormatter dfmt;
	public FileHandler fileHandler;
	private Timer shutdownTimer;
	private String[] args;
	public boolean recordingStatus;
	private ReadableInstant startInstant;

	private ArrayList<StreamServer> streamServers = new ArrayList<StreamServer>();
	private ConstantListener constantListener;
	private Controller controller;
	public ControllerServer controllerServer;

	//Weird args4j stuff here
	@Option(name="-l",aliases={"--length"},usage="Maximum number of sample lines per data file",forbids={"-i"})
	public Integer fileLength = 1000;

	@Option(name="-i",aliases={"--infinite"}, usage = "Enable infinite data files", forbids={"-l"})
	private Boolean infiniteDataFile = false;

	@Option(name="-d",aliases={"--debug"}, usage = "Enable debugging statements")
	private Boolean debug = false;

	@Option(name="-n",aliases={"--stdout"}, usage = "direct output to standard output instead of a file")
	private Boolean toStdout = false;

	@Option(name="-a",aliases={"--address"}, usage = "Set IF socat address",required=true)
	private String  address;

	@Option(name="-b",aliases={"--base-name"}, usage = "Set base file name")
	public String baseName = "DATA_";

	@Option(name="-dir",aliases={"--directory"}, usage = "Directory to place data files")
	private String directory = ".";

	@Option(name="-t",aliases={"--timed","--runtime"}, usage = "Specify period of time in milliseconds for java app to run")
	private Long runTime = null;

	@Option(name="-s",aliases={"--start","--start-immediately"}, usage = "Will start recording data immediately instead of waiting for signal from socket")
	private Boolean startImmediately = false;

	@Option(name="-f",aliases={"--file-limit"},usage = "Set the maximum number of data files to be created")
	private Integer fileLimit = null;

	@Option(name="-x",aliases={"--xml-config"},usage = "Provide a filename to parse an xmlfile of Object Dictionary entries ")
	private String xmlFileName = null;

	@Option(name="-help",help=true,usage="Bring up the extended help screen")
	private Boolean help = false;
	
	
	//Wrapper for other objects to use.
	//Hopefully these will become unneccessary
	public void startSyncListener(){
		coListener.startSyncListener();
	}
	public void stopSyncListener(){
		coListener.stopSyncListener();
	}
	public String detailedFileList(){
		return fileHandler.detailedFileList();
	}
	public void clearData(){
		fileHandler.clearDirectory();
	}
	public String createZip(){
		return fileHandler.createZip();
	}
	
	
	private class COListener implements CanOpenListener
	{
		long elapsedTimeNs = 0;
		//Adds Self to the Canopen instance's list of sync listeners
		public void startSyncListener()
		{
			if(!recordingStatus)
			{
				canOpen.addSyncListener(this);
				recordingStatus = true;
				startInstant = DateTime.now();
				if(runTime != null)
				{
					shutdownTimer.schedule(new ShutdownTimer(), runTime.longValue());
				}
			}
		}

		//Removes self from the Canopen instance's list of sync listeners
		public void stopSyncListener()
		{
			canOpen.removeSyncListener(this);
			try
			{
				if(!toStdout)
					fileHandler.close(elapsedTimeNs);
			}
			catch(Exception e)
			{
				System.out.println("remove synclistener " +e);
			}
			GlobalVars.START_TIME = null;
			recordingStatus = false;
		}

		/**
		* Acts on recieved sync messages.
		* Creates new file if needed
		* Gets the latest sample from the NodeListeners
		* Closes file and clears the per-file start time if file size is reached
		* @param canMessage - The Sync message, supplied by CanOpen. 
		*/
		public void onMessage(CanMessage canMessage)
		{
//			long nanoStart = System.nanoTime();
			debugPrint("SYNC message received");

			if(GlobalVars.START_TIME == null)
			{
				GlobalVars.START_TIME = System.nanoTime();
				String header = dfmt.produceHeader(nodes);
				if(toStdout)
				{
					System.out.println(header);
				}
				else
				{
					fileHandler.createFile();
					fileHandler.printLine(header);
				}
			}
			else
			{
				elapsedTimeNs = System.nanoTime()-GlobalVars.START_TIME;

				AccelerometerReading readings[] = new AccelerometerReading[nodes.size()];
				for(int i=0; i<nodes.size(); i++ )
				{
					AccelerometerReading ar = nodes.get(i).getLatestReading();
					if(ar != null)
						readings[i] = ar;
//nodes.get(i).getLatestReading();
				}
//				long nanoFmtStart = System.nanoTime();
				String formattedLine = dfmt.produceOutputLine(elapsedTimeNs, readings);
//				long nanoDone = System.nanoTime();

				if(toStdout)
				{
					System.out.println(formattedLine);
//					System.out.println("stime: "+(nanoFmtStart-nanoStart)/1000+"usec");
//					System.out.println("ftime: "+(nanoDone - nanoFmtStart)/1000+"usec");
//					System.out.println("ptime: "+(nanoDone-nanoStart)/1000+"usec");
//					System.out.println(dfmt.produceHexOutputLine(readings));
				}
				else
				{
					//debugPrint(dfmt.producePrettyOutputString(readings));
					if((fileHandler.currentSampleSize <  fileLength) || infiniteDataFile)
					{
						fileHandler.printSample(formattedLine);
						debugPrint(formattedLine);
					}
					else
					{
						try
						{
							fileHandler.close(elapsedTimeNs);
						}
						catch(Exception e)
						{
							System.out.println(e);
							stopSyncListener();
						}
						GlobalVars.START_TIME = null;
					}
				}
			}

		}

		public void onObjDictChange(SubEntry se) {}

		/**
		* Process CAN-Open state change events here
		*/
		public void onEvent(CanOpen canOpen)
		{
//			System.out.println("NmtListner.onEvent() State change "+canOpen.getStateString());
			if(canOpen.isResetNodeState())
			{
				stopSyncListener();
			}
			else if(canOpen.isOperationalState())
			{
//				System.out.println("need to potentially start recording on this event");
				if(startImmediately)
				{
					startSyncListener(); }
			}
		}
	}
	
	
	//This is for the stream
	private class ConstantListener implements CanOpenListener
	{
	
		public void onMessage(CanMessage canMessage){
			AccelerometerReading readings[] = new AccelerometerReading[nodes.size()];
			int j=0;
			for(int i=0; i<nodes.size(); i++ )
			{
				//readings[i] = nodes.get(i).getLatestReading();
				AccelerometerReading ar = nodes.get(i).getLatestReading();
                                if(ar != null)
                                	readings[j++] = ar;
			}
			//debugPrint(dfmt.produceJsonString(readings));
			Iterator<StreamServer> ss = streamServers.iterator();
			while(ss.hasNext())
			{
				ss.next().pushToStream(readings, canMessage.getInstant());
			}
		}
		
		public void onObjDictChange(SubEntry se){}
		public void onEvent(CanOpen canOpen){
			/*if(canOpen.isResetNodeState())
			{
				stopSyncListener();
			}else if(canOpen.isOperationalState())
			{
				startSyncListener();
			}*/
		}
		
		public void startSyncListener()
		{
			canOpen.addSyncListener(this);
			debugPrint("Starting Sync Listener for Stream");
			
		}
		public void stopSyncListener()
		{
			canOpen.removeSyncListener(this);
		}
	}
	

	private class FileHandler
	{
		private PrintWriter output;
		private int currentSampleSize;
		private String currentFileName;
		private int currentFileNumber = 0;

		//Clears the Data Directory of .csv files
		//This should really only be used through the cgi-bin script
		//If the java app is manually ran instead of executed through 'onBootSetup.sh'
		// it may result in unexpection deletions
		//It is recommended to not clear any directories until the user is positive
		// they do not need the data
		/*
		* @returns - A String to be sent back to the cgi-bin script, either success or error
		*/
		public String clearDirectory()
		{
			Path dir = Paths.get(directory);
			try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir))
			{
				for (Path file: stream)
				{
					if(file.toString().endsWith(".csv"))
					{
						debugPrint("FileHandler.clearDirectory(): Deleting file "+file.toString());
						Files.deleteIfExists(file);
					}
					else
					{
						debugPrint("FileHandler.clearDirectory(): Skipping file "+file.toString());
					}
				}
			}
			catch(DirectoryNotEmptyException dnee)
			{
				return "Error clearing data";
			}
			catch(IOException ioe)
			{
				return "Error clearing data";
			}
			currentFileNumber = 0;
			return "Success";
		}
		
		private Path[] getFiles(){
			ArrayList<Path> files = new ArrayList<Path>();
			Path dir = Paths.get(directory);
			try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)){
				for(Path file : stream){
					if(file.toString().endsWith(".csv")){
						files.add(file);
					}		
				}		
			}catch(Exception e){
				System.out.println("error retrieving filelist");
				e.printStackTrace();
			}
			return files.toArray(new Path[files.size()]);
		}
		/*
		 * Retrieve a list of .csv files in the data directory 
		 */
		public String[] fileList(){
			ArrayList<String> dataFiles = new ArrayList<String>();
			try{
				Path[] files = getFiles();
				for(Path file : files){
					dataFiles.add(file.toString());
				}		
			}catch(Exception e){
				System.out.println("error retrieving filelist");
				e.printStackTrace();
			}		
			return dataFiles.toArray(new String [0]);					
		}
		
		/*
		 * Retrieve a detailed file list including time, sample size, and file size. In a  JSON string
		 */
		 public String detailedFileList(){
			ArrayList<String[]> dataFiles = new ArrayList<String[]>();
			Path[] files = getFiles();
			try{
				for(Path file : files){
					String[] jFile = new String[4]; //[name,time,sample size,size bytes]
					jFile[0] = file.getFileName().toString();
					jFile[1] = Files.getLastModifiedTime(file).toString();
					jFile[2] = Integer.toString(countLines(file.toString())-6);
					jFile[3] = Long.toString(Files.size(file));
					if(Files.size(file)!=0){//file is still being written if size==0
						dataFiles.add(jFile);
					}
				}
			}catch(IOException ioe){
				System.out.println("Error creating detailed File List");
				ioe.printStackTrace();
			}
			/*try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)){
				for(Path file : stream){
					if(file.toString().endsWith(".csv")){
						String[] jFile = new String[4]; //[name,time,sample size,size bytes]
						jFile[0] = file.getFileName().toString();
						jFile[1] = Files.getLastModifiedTime(file).toString();
						jFile[2] = Integer.toString(countLines(file.toString())-6);
						jFile[3] = Long.toString(Files.size(file));
						if(Files.size(file)!=0){//file is still being written if size==0
							dataFiles.add(jFile);
						}
					}		
				}		
			}catch(Exception e){
				System.out.println("error retrieving filelist");
				e.printStackTrace();
			}*/		
			String d[][] =  dataFiles.toArray(new String[0][0]);
			return dfmt.produceJsonString(d);							 	
		 }
		
		//Stole this from the internet
		//Supposed to be zippy
		private int countLines(String filename)throws IOException{
			InputStream is = new BufferedInputStream(new FileInputStream(filename));
			try {
				byte[] c = new byte[1024];

				int readChars = is.read(c);
				if (readChars == -1) {
					// bail out if nothing to read
					return 0;
				}

				// make it easy for the optimizer to tune this loop
				int count = 0;
				while (readChars == 1024) {
					for (int i=0; i<1024;) {
						if (c[i++] == '\n') {
							++count;
						}
					}
					readChars = is.read(c);
				}

				// count remaining characters
				while (readChars != -1) {
					for (int i=0; i<readChars; ++i) {
						if (c[i] == '\n') {
							++count;
						}
					}
					readChars = is.read(c);
				}

				return count == 0 ? 1 : count;
			} finally {
				is.close();
			}		
		}
		
		public String createZip(){
			String archivePath = directory+"/archive";
			File archiveDir = new File(archivePath);
			archiveDir.mkdir();
			DateTimeFormatter dateFormat = new DateTimeFormatterBuilder()
				.appendYearOfEra(4,4)
				.appendLiteral("-")
				.appendMonthOfYear(2)
				.appendLiteral("-")
				.appendDayOfMonth(2)
				.appendLiteral("_")
				.appendClockhourOfDay(2)
				.appendLiteral("-")
				.appendMinuteOfHour(2)
				.appendLiteral("-")
				.appendSecondOfMinute(2)
				.toFormatter();
			String filename = dateFormat.print(DateTime.now())+".zip";
			Map<String, String> env = new HashMap<>();
			env.put("create","true");
			URI uri=null;
			try{
				uri = new URI("jar:file://"+archivePath+"/"+filename);
			}catch(URISyntaxException urise){
				System.out.println("uri messed up");
				urise.printStackTrace();
			}
			try(FileSystem zipfs = FileSystems.newFileSystem(uri,env)){
				Path[] files = getFiles();
				for (Path file: files){
					Path PathInZip = zipfs.getPath("/"+file.getFileName().toString());
					Files.copy(file,PathInZip,StandardCopyOption.REPLACE_EXISTING);
				}
			}catch(IOException ioe){
				System.out.println("Couldn't create zip file");
				ioe.printStackTrace();
			}catch(Exception e){
				System.out.println("unknown "+ e);
				e.printStackTrace();
			}
			return filename;
			
		}
		
		//Creates a new data file and resets or increments relevant variables
		public void createFile()
		{
			if((fileLimit != null)&&(currentFileNumber>=fileLimit))
			{
				gracefulShutdown();
			}
			else
			{
				if(output != null)
					close(0);
				try
				{
					currentFileName = baseName+String.format("%03d", currentFileNumber)+".csv";
					output = new PrintWriter(new BufferedWriter(new FileWriter(directory+"/"+currentFileName,false)));
				}
				catch(Exception e)
				{
					System.out.println("createFile() couldn't create file");
				}
				currentSampleSize = 0;
				currentFileNumber++;
				controllerServer.pushFiles();
			}
		}

		//Closes file and makes comment at end of file
		public void close(long elapsedTime)
		{
			if(output!=null)
			{
				makeEOFComment(elapsedTime);
				output.close();
			}
			output = null;
		}

		/**
		 * Prints a line of text to the current file
		 * @param line - Line to be printed
		 */
		void printLine(String line)
		{
		    if(output == null)
				createFile();
		    output.println(line);
//		    output.flush();
		}

		/**
		* Wrapper for println that increments the currentSampleSize
		* @param sample - Line to be printed;
		*/
		void printSample(String sample)
		{
			printLine(sample);
			currentSampleSize++;
		}


		//Generates the last line of the data file
		//Simply contains info on the number of samples in the file
		// and the average sample rate across the file
		//Does not rely on currentSampleSize to be correct
		//This could probably be changed to show more faith in the instance
		// variables but  its works fine like it is.
		public void makeEOFComment(long elapsedTimeNs)
		{
			double dt = (double)elapsedTimeNs;
			int sampleNumber = currentSampleSize;
			double deltaTimeSeconds = dt/1e9;
			double sampleRate = ((double)sampleNumber/deltaTimeSeconds);
			printLine(String.format(";%d samples at %.3fHz", sampleNumber, sampleRate));
		}
	}


	private class CoXmlHandler extends DefaultHandler
	{
		CanOpenThread cot;
		boolean bDriver = false;
		boolean bType = false;
		boolean bAddress = false;
		boolean bPort = false;
		boolean bCanAddr = false;
		boolean bChannels = false;
		boolean bNode = false;
		boolean bOdIndex = false;
		String type;
		String ipAddress;
		String port;
		String canAddr;
		String odIndex;
		boolean bCobid = false;
		String cobid;
		boolean bName = false;
		String sName;
		boolean bNumSamples = false;
		String numSamples;
		boolean bBitsSample = false;
		String bitsSample;
		private int rxPdoCtlMapIndex = 0x01;
		boolean bStream = false;
		boolean bStreamAddress = false;
		boolean bStreamPort = false;
		String streamAddress;
		String streamPort;
		boolean bController = false;
		boolean bControllerAddress = false;
		boolean bControllerPort = false;
		String controllerAddress;
		String controllerPort;
		boolean bBusMasterPort;
		String busMasterPort;

		public InetAddress getIPv4FromIFaceName(String name) throws SocketException{
        		Enumeration<NetworkInterface> interfaces;
        		interfaces = NetworkInterface.getNetworkInterfaces();
        		while(interfaces.hasMoreElements()){
                		NetworkInterface ni = interfaces.nextElement();
                		if (!(ni.getName().contains(name)))
                        		continue;
        			Enumeration<InetAddress> ips = ni.getInetAddresses();
                		while (ips.hasMoreElements()){
                        		InetAddress ina = ips.nextElement();
                        		if(ina.toString().contains("."))
                                		return ina;
                		}
        		}
        		return null;

    		}

    		public InetAddress getIPv4FromIFaceNameList(ArrayList<String> nameList) throws SocketException{
        		Iterator<String> name = nameList.iterator();
        		while(name.hasNext()){
                		InetAddress ina = getIPv4FromIFaceName(name.next());
                		if(ina != null){
                        		return ina;
				}
        		}
        		return null;
    		}


		CoXmlHandler(CanOpenThread cot)
		{
			this.cot = cot;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if(qName.equalsIgnoreCase("can_driver")) {
				bDriver = true;
			}
			else if(qName.equalsIgnoreCase("canopen_address")) {
				bCanAddr = true;
			}
			else if(qName.equalsIgnoreCase("channels")) {
				bChannels = true;
			}
			else if(qName.equalsIgnoreCase("stream")){
				bStream = true;
			}
			else if(qName.equalsIgnoreCase("controller")){
				bController=true;
			}
			else if(bDriver)
			{
				if(qName.equalsIgnoreCase("type")) {
					bType = true;
				}
				else if(qName.equalsIgnoreCase("address")) {
					bAddress = true;
				}
				else if(qName.equalsIgnoreCase("port")) {
					bPort = true;
				}
			}
			else if(bChannels)
			{
				if(bNode)
				{
					if(qName.equalsIgnoreCase("od_index")) {
						bOdIndex = true;
					}
					else if(qName.equalsIgnoreCase("name")) {
						bName = true;
					}
					else if(qName.equalsIgnoreCase("cobid")) {
						bCobid = true;
					}
					else if(qName.equalsIgnoreCase("num_samples")) {
						bNumSamples = true;
					}
					else if(qName.equalsIgnoreCase("bits_sample")) {
						bBitsSample = true;
					}
				}
				else if(qName.equalsIgnoreCase("node")) {
					bNode = true;
				}
			}
			else if(bStream)
			{
				if(qName.equalsIgnoreCase("address")){
					bStreamAddress=true;
				}
				else if(qName.equalsIgnoreCase("port")){
					bStreamPort=true;
				}
			}
			else if(bController){
				if(qName.equalsIgnoreCase("address")){
					bControllerAddress = true;
				}else if(qName.equalsIgnoreCase("port")){
					bControllerPort = true;
				}else if(qName.equalsIgnoreCase("busmasterport")){
					bBusMasterPort=true;
				}
			}

		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			if (qName.equalsIgnoreCase("can_driver"))
			{
				bDriver = false;
				debugPrint("type: ("+type+")  addr: ("+ipAddress+") port: ("+port+")");
				int p = Integer.decode(port);
				cot.dm = new DriverManager(type, ipAddress, p, false);
				cot.drvr = cot.dm.getDriver();
				debugPrint("CANbus driver configured");

			}
			else if(qName.equalsIgnoreCase("canopen_address")) {
				bCanAddr = false;
				int iAddr = Integer.decode(canAddr);
				debugPrint("canopen addr: ("+canAddr+")"+"  val:"+iAddr);
				cot.od = DefaultOD.create(iAddr);
				//                              nodeId,  type, heartbeatMs,vendorId,productId, revisionNum, serialNum
//				cot.od = DefaultOD.createStandardDict(iAddr, 0x0, 5000, 0x0000029C, 0x19, 0x11, 0x1234 );

				canOpen = new CanOpen(cot.drvr, cot.od, iAddr, GlobalVars.DEBUG);
			}
			else if(qName.equalsIgnoreCase("channels")) {
				bChannels = false;
			}
			else if(qName.equalsIgnoreCase("stream"))
			{
				bStream = false;
				try{
					StringTokenizer st = new StringTokenizer(streamAddress, ",");
					while (st.hasMoreTokens())
					{
						String t2 = st.nextToken();
//						System.out.println("Stream t2: "+t2);
						InetAddress i1 = getIPv4FromIFaceName(t2);
						if(i1 ==null)
							continue;

						System.out.println("Stream Using "+i1.getHostAddress());
						try
						{
							StreamServer streamServer = new StreamServer(i1,Integer.decode(streamPort));
							streamServer.start();
							streamServers.add(streamServer);
						}
						catch(IllegalStateException e2)
						{
							System.out.println("Error starting stream server");
							e2.printStackTrace();
						}
					}
				}catch(Exception e){
					System.out.println("Error");
					e.printStackTrace();
				}
			}
			else if(qName.equalsIgnoreCase("controller")){
				bController = false;
				try{
					StringTokenizer st = new StringTokenizer(controllerAddress, ",");
					while (st.hasMoreTokens())
                                        {
						String t2 = st.nextToken();
                                                System.out.println("Controller t2: "+t2);
                                                InetAddress i1 = getIPv4FromIFaceName(t2);
                                                if(i1 ==null)
                                                        continue;
						System.out.println("Controller Using "+i1.getHostAddress());
						try
						{
							controller.setBusMasterPort(Integer.decode(busMasterPort));
							controllerServer = new ControllerServer(i1,Integer.decode(controllerPort),controller);
							controllerServer.start();
							controller.setSampleRate(50); //default. possible make this configurable in the future
						}
						catch(IllegalStateException e3)
						{
							System.out.println("Error starting Controller server");
							e3.printStackTrace();
						}
					}
				}catch(Exception e){
					System.out.println("Error");
					e.printStackTrace();
				}
			}
			else if(bDriver)
			{
				if(qName.equalsIgnoreCase("type")) {
					bType = false;
				}
				else if(qName.equalsIgnoreCase("address")) {
					bAddress = false;
				}
				else if(qName.equalsIgnoreCase("port")) {
					bPort = false;
				}
			}
			else if(bChannels)
			{
				if(bNode)
				{

					if(qName.equalsIgnoreCase("od_index")) {
						bOdIndex = false;
					}
					else if(qName.equalsIgnoreCase("cobid")) {
						bCobid = false;
					}
					else if(qName.equalsIgnoreCase("name")) {
						bName = false;
					}
					else if(qName.equalsIgnoreCase("num_samples")) {
						bNumSamples = false;
					}
					else if(qName.equalsIgnoreCase("bits_sample")) {
						bBitsSample = false;
					}
					else if(qName.equalsIgnoreCase("node")) {
						bNode = false;
						int cobId = Integer.decode(cobid);
						int bits = Integer.decode(bitsSample);
						int iOdIndex = Integer.decode(odIndex);

						debugPrint("node parameters odIndex:("+odIndex+ ") cobid:("+cobid+ ") numSamples:("+ numSamples +") bits per sample:("+ bitsSample+")");

						NodeTracker possibleNode = new NodeTracker(canOpen, sName, cobId, cobId - 0x180, iOdIndex, 0x3, bits, 0,1,2);
						//for(int i = 0; i < 10; i++) possibleNode.getLatestReading();
						//if (possibleNode.getLatestReading().getX() != 0){
							//nodes.add( new NodeTracker(canOpen, sName, cobId, rxPdoCtlMapIndex++, iOdIndex, 0x3, bits, 0,1,2));
							nodes.add(possibleNode);
							//nodes.add( new NodeTracker(canOpen, sName, cobId, iOdIndex, iOdIndex, 0x3, bits, 0,1,2));
						//}
					}
				}
				else if(qName.equalsIgnoreCase("channels")) {
					bChannels = false;
				}
			}
			else if(bStream)
			{
				if(qName.equalsIgnoreCase("address")){
					bStreamAddress = false;
				}
				else if(qName.equalsIgnoreCase("port")){
					bStreamPort = false;
				}	
			}
			else if(bController){
				if(qName.equalsIgnoreCase("address")){
					bControllerAddress = false;
				}else if(qName.equalsIgnoreCase("port")){
					bControllerPort = false;
				}else if(qName.equalsIgnoreCase("busmasterport")){
					bBusMasterPort = false;
				}
			}
		}

		@Override
		public void characters(char ch[], int start, int length) throws SAXException
		{
			String temp = new String(ch, start, length).trim();
			if(temp.length() == 0)
				return;

			if(bType)
				type = temp;
			else if(bAddress)
				ipAddress = temp;
			else if(bPort)
				port = temp;
			else if(bCanAddr)
				canAddr = temp;
			else if(bOdIndex)
				odIndex = temp;
			else if(bCobid)
				cobid = temp;
			else if(bName)
				sName = temp;
			else if(bNumSamples)
				numSamples = temp;
			else if(bBitsSample)
				bitsSample = temp;
			else if(bStreamAddress)
				streamAddress = temp;
			else if(bStreamPort)
				streamPort = temp;
			else if(bControllerAddress)
				controllerAddress = temp;
			else if(bControllerPort)
				controllerPort = temp;	
			else if(bBusMasterPort)
				busMasterPort = temp;
		}
	}	// end private class CoXmlHandler


	//Does most of the CanOpen setup stuff
	private class CanOpenThread extends Thread
	{
		private DriverManager dm;
		private Driver drvr;
		private ObjectDictionary od;

		CanOpenThread(String fname) throws Exception
		{
			try
			{
				debugPrint("CANbus driver starting");
				coListener = new COListener();
				nodes = new ArrayList<>();//NodeTracker[4];

				SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
				SAXParser saxParser = saxParserFactory.newSAXParser();
				CoXmlHandler handler = new CoXmlHandler(this);
				File fXmlFile = new File(fname);
				saxParser.parse(fXmlFile, handler);

				canOpen.addEventListener(coListener);
				constantListener = new ConstantListener();

				debugPrint("CanOpen configured");
			}
			catch(ParserConfigurationException pce)
			{
				pce.printStackTrace();
				throw(new Exception("outta here"));
			}
			catch(SAXException se)
			{
				se.printStackTrace();
				throw(new Exception("outta here"));
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
				throw(new Exception("outta here"));
			}
		}


		//Sets up CanOpen stuff
		//Creates the NodeTrackers and SyncListener
		@Override
		public void run()
		{
			boolean restart = false;
			try
			{
				do
				{
					debugPrint("CanOpen Starting");
					canOpen.startTasks();
					if(startImmediately)
					{
						coListener.startSyncListener();
					}
					constantListener.startSyncListener();
					canOpen.join(1000);
					debugPrint("CanOpenThread.run(): canOpen.start() is finished");
				}
				while(restart);
			}
			catch(InterruptedException ie)
			{
System.out.println("CanOpenThread shutdown REALLY");
				dm.unloadDriver();
//				dm.unloadDriver();
				drvr = null;
				dm = null;
				System.gc();
				canOpen = null;
				od = null;
				GlobalVars.START_TIME = null;
				nodes.clear();
				fileHandler = null;
				System.gc();
System.out.println("CanOpenThread shutdown complete");
			}
			catch(Exception coe)
			{
				coe.printStackTrace();
			}
		}
		public void shutdown()
		{
			System.out.println("CanOpenThread shutdown REALLY");
			dm.unloadDriver();
			drvr = null;
			dm = null;
			System.gc();
			canOpen = null;
			od = null;
			GlobalVars.START_TIME = null;
			nodes.clear();
			fileHandler = null;
			System.gc();
		}
	} // end private class def

	private class ShutdownTimer extends TimerTask
	{
		@Override
		public void run()
		{
			gracefulShutdown();
		}
	}


	//constructor
	//Sets up most of the Threads and important Variables
	//Also handles any special cases for the command line arguments
	/*
	* @param args - List of commandLine Arguments
	*/
	public DataLogger(String[] args)
	{
		this.args = args;

		ParserProperties pp = ParserProperties.defaults().withUsageWidth(100);
		CmdLineParser parser = new CmdLineParser(this, pp);
//		parser.setUsageWidth(100);//???
		try
		{
			parser.parseArgument(args);
		}
		catch(CmdLineException cle)
		{
			parser.printUsage(System.err);
			debugPrint("DataLogger(): " + cle.getMessage());
			System.exit(-1);
		}
		if(help)
		{
			try
			{
				BufferedReader helpReader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("helpscreen.txt")));
				String helpLine;
				helpLine = helpReader.readLine();
				while(helpLine!=null)
				{
					System.out.println(helpLine);
					helpLine = helpReader.readLine();
				}
				helpReader.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
		}

		{

			shutdownTimer = new Timer("Shutdown Timer", true);//true means it is a daemon

			GlobalVars.DEBUG = debug;

			dfmt = new DataFormatter();
			dfmt.setTitle("http://www.gcdataconcepts.com, Datalogger");
			dfmt.setSampleRate("fixeme, Hz");
			fileHandler = new FileHandler();
			
			recordingStatus = false;
			
			controller = new Controller(this);
			//controller.setSampleRate(50);
			
			try
			{
				if( xmlFileName != null)
					coThread = new CanOpenThread(xmlFileName);
				else
				{
					System.out.println("data logger requires a config file.");
					System.exit(-2);
				}
			}
			catch( Exception e)
			{
				System.out.println("Can't use default object dictionary entries anymore");
				System.exit(-2);
			}

			coThread.start();
			
			try
			{
				coThread.join(1000);
			}
			catch(InterruptedException ie)
			{
				System.out.println("coThread.join exception");
				ie.printStackTrace();
			}
		}
	}


	/**
	* Utility to print debug info
	* only prints when DEBUG is turned on
	*
	* param s - String to print
	*/
	static void debugPrint(String s)
	{
		if(GlobalVars.DEBUG)
			System.out.println(s);
	}


	//Shuts down everything
	public void gracefulShutdown()
	{
		System.out.println("gracefulShutdown");
		try{
			Iterator<StreamServer> ss = streamServers.iterator();
			while(ss.hasNext())
			{
				ss.next().shutdown();
			}

			controllerServer.shutdown();
		}catch(Exception e){
			System.out.println("Error during shutdown");
			e.printStackTrace();
		}
		System.out.println("stream servers stopped");
		
		coListener.stopSyncListener();
	}


	//Main method
	//Creates DataLogger Instance
	/*
	* @param args - List of comand-line arguments
	*/
	public static void main(String args[])
	{
		Instant i1 = Instant.now();
		long secs = i1.getEpochSecond();
		long nano = i1.getNano();
		double dtime = secs+ (double)nano/1e9;
		System.out.println("startTime: "+i1);
		System.out.println("dtime    : "+dtime);

		Instant i2 = Instant.now();
		 secs = i2.getEpochSecond();
		nano = i2.getNano();
		System.out.println("startTime: "+i2);
		dtime = secs+ (double)nano/1e9;
		System.out.println("dtime    : "+dtime);

		final DataLogger dl = new DataLogger(args);
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				dl.gracefulShutdown();
			}
		});
	}
}
