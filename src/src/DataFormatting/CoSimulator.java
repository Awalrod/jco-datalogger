package DataFormatting;

import DataFormatting.DataFormatter;
import DataRecording.AccelerometerReading;
import DataRecording.NodeTracker;
import GlobalVars.GlobalVars;

import com.gcdc.can.CanMessage;
import com.gcdc.can.Driver;
import com.gcdc.can.DriverManager;
import com.gcdc.canopen.*;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.StringTokenizer;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.ReadableInstant;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;



/**
 * Created by gcdc on 7/3/2020.
 */
public class CoSimulator implements CanOpenListener
{
	private CanOpen canOpen;
	private DataFormatter dfmt;
	long elapsedTimeNs = 0;
	DateTime startInstant;
	Simulator simulator;

	public CoSimulator(CanOpen canOpen, Simulator simulator)
	{
		this.canOpen = canOpen;
		this.simulator = simulator;
	}
	
	//Adds Self to the Canopen instance's list of sync listeners
	public void startSyncListener()
	{
		canOpen.addSyncListener(this);
		startInstant = DateTime.now();
	}

	//Removes self from the Canopen instance's list of sync listeners
	public void stopSyncListener()
	{
		canOpen.removeSyncListener(this);
	}

	/**
	* Acts on recieved sync messages.
	* @param canMessage - The Sync message, supplied by CanOpen. 
	*/
	public void onMessage(CanMessage canMessage)
	{
//			long nanoStart = System.nanoTime();
//		System.out.println("SYNC message received in simulator");
		simulator.evaluate(canMessage.getInstant());
	}

	public void onObjDictChange(SubEntry se)
	{
	}

	public AccelerometerReading getAccel()
	{
		return(simulator.getAccel());
	}

	/**
	* Process CAN-Open state change events here
	*/
	public void onEvent(CanOpen canOpen)
	{
//		System.out.println("NmtListner.onEvent() State change "+canOpen.getStateString());
		if(canOpen.isResetNodeState())
		{
			stopSyncListener();
		}
		else if(canOpen.isOperationalState())
		{
			startSyncListener();
		}
	}
	
	public AccelerometerReading getLatestReading()
	{
		AccelerometerReading retval = new AccelerometerReading(0,0,0);
		retval.setID("sim");
		return(retval);
	}
	
}
