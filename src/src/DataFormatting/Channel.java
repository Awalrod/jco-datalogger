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


public class Channel extends NestedHandler
{
	List<Signal> slist;
	String chName;
	private double val;

	public Channel(String name)
	{
		super(name);
		slist = new ArrayList<Signal>();
//		System.out.println("new " + nodeName);
		chName = "default";
	}

	public Channel()
	{
		this("channel");
	}
	
	public double getValue()
	{
		return(val);
	}

	public int getIntValue()
	{
		return((int)(val+0.5));
	}
	
	public boolean nameEquals(String name)
	{
		return( chName.equalsIgnoreCase(name));
	}

	public void evaluate(Instant t)
        {
/*        	Double dVal = new Double(0.0);
                slist.forEach( n -> dVal.sum(dVal.doubleValue(), n.evaluate(t)) );
                val = dVal.doubleValue();
*/
		val = 0.0;
		Iterator<Signal> is = slist.iterator();
		while(is.hasNext())
		{
			val+= is.next().evaluate(t);
		}
//                System.out.println("ch: "+chName+" val:"+val);
        }

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		super.startElement(uri, localName, qName, attributes);

		if(qName.equalsIgnoreCase("signal")) {
			Signal currSignal;
			String type = attributes.getValue("type");

			if(type.equalsIgnoreCase("sin"))
				currSignal = new Sinusoid("signal");
			else if(type.equalsIgnoreCase("random"))
				currSignal = new RandomSignal("signal");
			else if(type.equalsIgnoreCase("randomflat"))
				currSignal = new RandFlatSignal("signal");
			else
				currSignal = new Sinusoid("signal");
			
			child = currSignal;
			slist.add(currSignal);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		super.endElement(uri, localName, qName);
		if(qName.equalsIgnoreCase("name"))
                {
                        chName = new String(nodeString);
                }


	}
}
