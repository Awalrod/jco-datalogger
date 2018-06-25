package DataFormatting;

import DataRecording.AccelerometerReading;
import DataRecording.SubIndexValue;
import DataRecording.NodeTracker;
import GlobalVars.GlobalVars;

import java.util.ArrayList;
import java.util.Date;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.text.SimpleDateFormat;
import java.io.File;

import com.google.gson.*;
/**
 * Created by gcdc on 6/7/17.
 */
public class DataFormatter
{
	private final String REVISION_KEY = "Canopen-Revision";
	private final String DATE_KEY = "Build-Date";
	private final String VERSION_KEY = "Version";
	public DataFormatter()
	{
		setTitle("blank");
		setSampleRate("unknown");
	}
	String title;
	String sampleRate;
	
	/**
	*
	* @param readings A list of readings from the accelerometers
	* @return Example output:  Elapsed Time: 208441739
	X: 2731, Y: 3343, Z: 2192
	Index: 0x6210
	Elapsed Time: 208477156
	X: 2502, Y: 1504, Z: 2305
	Index: 0x6211
	Elapsed Time: 208501947
	X: 2787, Y: 1825, Z: 1820
	Index: 0x6212
	Elapsed Time: 208527208
	X: 2455, Y: 3095, Z: 3201
	Index: 0x6213
	*/
	public String producePrettyOutputString(AccelerometerReading... readings)
	{
		String retVal = new String();
		for(AccelerometerReading reading : readings)
		{
			retVal = retVal.concat(reading.toString());
		}
		return retVal;
	}


	public String getManifestAttribute(String key, Attributes attrs)
	{
		if(attrs == null)
		{
			return "";
		}
		String val = attrs.getValue(key);
		if(val == null)
			val = "";
		return val;
	}
	
	public void setTitle(String title)
	{
		this.title = new String(";Title "+ title+"\n");
	}

	public void setSampleRate(String rate)
	{
		sampleRate = new String(";SampleRate, "+ rate+"\n");
	}


	/**
	*
	* @param indexes A list of indexes that we recieve readings from
	* @return A header comment
	*/
	public String produceHeader(ArrayList<NodeTracker> nodes)
	{
		JarFile jar;
		Manifest manifest;
		Attributes attrs = null;
		try
		{
			jar = new JarFile(new File(DataFormatter.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
			manifest = jar.getManifest();
			attrs = manifest.getMainAttributes();
		}
		catch(Exception e)
		{
			System.out.println(";Manifest could not be read");
		}

		String header = new String(title);


		String headerVersion = ";Version";
		headerVersion = headerVersion.concat(","+getManifestAttribute(VERSION_KEY,attrs));
		headerVersion = headerVersion.concat(", Build date:");
		headerVersion = headerVersion.concat(","+getManifestAttribute(DATE_KEY,attrs));
		header = header.concat(headerVersion + "\n");

//		String headerCanopen = ";Canopen";
//		headerCanopen = headerCanopen.concat(", Revision:");
//		headerCanopen = headerCanopen.concat(","+ getManifestAttribute(REVISION_KEY,attrs));
//		header = header.concat(headerCanopen + "\n");

		String headerStartTime = ";StartTime";
		headerStartTime = headerStartTime.concat(","+new SimpleDateFormat("yyyy-MM-d").format(new Date()));
		headerStartTime = headerStartTime.concat(","+new SimpleDateFormat("HH:mm:ss.SS").format(new Date()));
		header = header.concat(headerStartTime+"\n");

		header = header.concat(sampleRate);


		String headerLabels = ";Time, "; //new String(";").concat(new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss:SS z").format(new Date()));
		for(int i = 0; i < nodes.size(); i++ )
		{
			String name = nodes.get(i).getName();
			headerLabels = headerLabels.concat(name+"X,");
			headerLabels = headerLabels.concat(name+"Y,");
			headerLabels = headerLabels.concat(name+"Z, ");
		}

		header = header.concat(headerLabels); //no newline intentionally
		return( header);
	}


	public String produceFileName()
	{
		String filename = new SimpleDateFormat("EEEE d MMM yyyy HH:mm:ss:SS z").format(new Date());
		return(filename);
	}


	public static String produceOutputLine(long elapsedTime, AccelerometerReading[] readings)
	{
		StringBuilder output = new StringBuilder(0x100);
//		output.append(String.format("%3f", ((double)elapsedTime/(double)1000000000)));
		output.append( ((double)elapsedTime)/(double)1000000000 );
		for(AccelerometerReading reading : readings)
		{
			output.append(", ");
			output.append(reading.toStringBuilder());
		}
		return(output.toString());
	}


	public static String produceHexOutputLine(long elapsedTime, AccelerometerReading[] readings)
	{
		String output = new String("");
		//NOTE:
		//  The elapsed time value will be different for each reading
		//  even though they were all gathered at the same Sync message.
		//  The timestamp of the first reading will be used on the line
		//  because that is assumed to be the most accurate. Luckily
		//  the margin of error is only a few ten thousandths of a second
		//  so it shouldn't cause any problems.
		long nanoElapsed = elapsedTime;
		double secondElapsed = (double)nanoElapsed/(double)1000000000; //10^9

		output = output.concat(String.format("%3f",secondElapsed));
		for(AccelerometerReading reading : readings)
		{
			output = output.concat(", 0x"+Integer.toHexString(reading.getX()));
			output = output.concat(", 0x"+Integer.toHexString(reading.getY()));
			output = output.concat(", 0x"+Integer.toHexString(reading.getZ()));
		}
		return output;
	}
	
	public static String produceJsonString(AccelerometerReading[] readings){
		String jsonString;
		Gson gson = new Gson();
		jsonString = gson.toJson(readings);
		return jsonString;	
	}
	public static String produceJsonString(AccelerometerReading reading){
		String jsonString;
		Gson gson = new Gson();
		jsonString = gson.toJson(reading);
		return jsonString;
	}
	public static String produceJsonString(String[][] stringList){
		String jsonString;
		Gson gson = new Gson();
		jsonString = gson.toJson(stringList);
		return jsonString;
	}
	

}
