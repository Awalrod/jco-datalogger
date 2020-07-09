package DataFormatting;

// for parasing an xml config file
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
//import org.xml.sax.helpers.DefaultHandler;
import com.gcdc.sax.NestedHandler;


import java.time.*;
import java.util.*;
import DataRecording.AccelerometerReading;

public class Simulator extends NestedHandler
{
	List<Channel> clist;
	String nodeName;

	public Simulator(String name)
	{
		super(name);
		clist = new ArrayList<Channel>();
//		System.out.println("new "+nodeName);
		nodeName = "9";
	}

	public Simulator()
	{
		this("simulator");
	}
	
	public void evaluate(Instant t)
	{
		clist.forEach(n -> n.evaluate(t));
	}
	
	public AccelerometerReading getAccel()
	{
		int x = 0;
		int y = 0;
		int z = 0;
		Iterator<Channel> iter = clist.iterator();
		while(iter.hasNext())
		{
			Channel ch = iter.next();
			if(ch.nameEquals("x"))
			{
				x = ch.getIntValue();
			}
			else if(ch.nameEquals("y"))
			{
				y = ch.getIntValue();
			}
			else if(ch.nameEquals("z"))
			{
				z = ch.getIntValue();
			}
		}
		AccelerometerReading ar = new AccelerometerReading(x, y, z);
		ar.setID(nodeName);
		return(ar);
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		super.startElement(uri, localName, qName, attributes);		
//		System.out.println(nodeName +" start element: "+ qName);
		
		if(qName.equalsIgnoreCase("channel")) {
			Channel currChannel;
			currChannel = new Channel();
			child = currChannel;
			clist.add(currChannel);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if (qName.equalsIgnoreCase("name"))
		{
			nodeName = new String(nodeString);
		}
//		else
//		{
//		System.out.println(nodeName +" end element: "+ qName);
			super.endElement(uri, localName, qName);
//		}
	}
}
