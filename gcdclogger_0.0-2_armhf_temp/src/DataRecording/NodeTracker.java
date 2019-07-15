package DataRecording;
/**
 * Created by gcdc on 6/7/17.
 * Stores all data collected from a node and dumps it on command
 */
import GlobalVars.GlobalVars;
import com.gcdc.canopen.*;

import java.util.ArrayList;
import java.util.Iterator;

public class NodeTracker
{
	private CanOpen co;
	private int destIndex;
	private int nodeId;
	private ArrayList<SubEntry> subs;
	private int data[];

	//Assumes that the CanOpen has been initialized and the Object dictionary has been created
	/**
	*
	* @param coInstance An already configured instance of CanOpen
	* @param cobid rpdo CobId of slave node
	* @param emNormIndex normalized value of the RxPdo index range 0x00-0xff,
	* @param destIndex Index to place data in Object Dictionary
	* @param numSamples Number of samples/subindexes to create
	* @param numBits Length of subindexes
	* @param subindexes List of subindexes to monitor
	*/
	public NodeTracker(CanOpen coInstance, String name, int cobid, int emNormIndex, int destIndex, int numSamples, int numBits, int ... subindexes)
	{
		this.nodeId = emNormIndex;
		this.destIndex = destIndex;
//		this.subindexes = subindexes;
		co = coInstance;
		co.getObjDict().insertRxpdo(name, cobid, emNormIndex, destIndex, numSamples, numBits);
		try
		{
			co.addRxPdoIndex(emNormIndex);
		}
		catch (Exception e)
		{
			System.out.println("Could not add RxPdo Index");
			e.printStackTrace();
		}

		subs = new ArrayList<SubEntry>(numSamples);
		for(int i = 0; i < subindexes.length;i++)
		{
			try
			{
				subs.add(co.getObjDict().getSubEntry(destIndex,subindexes[i]));
			}
			catch (COException e)
			{
				System.out.println("Could not read subentry 0x"+Integer.toHexString(destIndex <<8|subindexes[i]));
				e.printStackTrace();
			}
		}

		data = new int[subs.size()];
	}


	public AccelerometerReading getLatestReading()
	{
		Iterator<SubEntry> ise = subs.iterator();
		int j = 0;
		try
		{
			while( ise.hasNext())
			{
				data[j++] = ise.next().getInt();
			}
		}
		catch(COException e)
		{
			System.out.println(";Could not read subentry 0x"+Integer.toHexString(destIndex <<8));
			e.printStackTrace();
		}
	AccelerometerReading ar = new AccelerometerReading(data);
	String sid = Integer.toHexString(nodeId);
	ar.setID(sid);
	if(ar.getX() != 0 && ar.getY() != 0 && ar.getZ() != 0)
		return( ar);
	else
		return(null);
	}


	public String getName()
	{
		String name;
		try
		{
			name  = co.getObjDict().getEntry(destIndex).getName();
		}
		catch(COException e)
		{
			name = "Unknown";
		}
		return(name);
	}

}
