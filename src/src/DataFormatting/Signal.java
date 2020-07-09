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


abstract class Signal extends NestedHandler
{

	abstract double evaluate(Instant t);
	double freq;
	double amplitude;
	double t0;
	double offset;
	double jitter;
	double phase;

	public Signal(String name)
	{
		super(name);
		freq = 0.0;
		amplitude = 0.0;
		offset = 0.0;
		jitter = 0.0;
//             System.out.println(nodeName);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
//             System.out.println(nodeName +" end element: "+ qName +"  ("+nodeString+") numattr:"+currentAttributes.getLength() );
             	
		if( qName.equalsIgnoreCase("freq"))
		{
			freq = Double.parseDouble(nodeString);
		}
		else if(qName.equalsIgnoreCase("amplitude"))
		{
			amplitude = Double.parseDouble(nodeString);
			String sUnits = currentAttributes.getValue("units");
			if(sUnits !=null )
			{	// a units modifier was found in the attributes section of this element
				if(sUnits.equalsIgnoreCase("rms"))
				{
					amplitude = Math.sqrt(2.0) * amplitude;

				}
				if(sUnits.equalsIgnoreCase("db"))
				{
					double db = amplitude;
					db = db/20.0;
					amplitude =  Math.sqrt(2.0) * Math.pow(10.0,db);
				}
			}
		}
		else if(qName.equalsIgnoreCase("t0"))
		{
			t0 = Double.parseDouble(nodeString);
		}
		else if(qName.equalsIgnoreCase("offset"))
		{
			offset = Double.parseDouble(nodeString);
		}
		else if(qName.equalsIgnoreCase("jitter"))
		{
			jitter = Double.parseDouble(nodeString);
		}
		else if(qName.equalsIgnoreCase("phase"))
		{
			phase = Double.parseDouble(nodeString);
			String sUnits = currentAttributes.getValue("units");
			if(sUnits !=null )
			{	// a units modifier was found in the attributes section of this element
				if(sUnits.equalsIgnoreCase("deg"))
				{
					phase = phase* Math.PI/180.0;

				}
				else if(sUnits.equalsIgnoreCase("grad"))
				{
					phase = phase* Math.PI/200.0;
				}
			}
			
		}
		else
		{
			super.endElement(uri, localName, qName);
		}
		nodeString = new String();
	}
}
