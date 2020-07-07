package DataFormatting;

// for parasing an xml config file
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.time.*;
import java.util.*;


public class Channel extends NestedHandler
{
	List<Signal> slist;
	Signal currSignal;

	public Channel(String name)
	{
		super(name);
		slist = new ArrayList<Signal>();
//		System.out.println("new " + nodeName);
	}
	
	public Channel()
	{
		this("channel");
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		super.startElement(uri, localName, qName, attributes);		
		
		if(qName.equalsIgnoreCase("signal")) {
			String type = attributes.getValue("type");
//	System.out.println("signal type: "+type);
			currSignal = new Sinusoid("signal");
			child = currSignal;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
//		if (qName.equalsIgnoreCase("channel"))
//		{
//			slist.add(currSignal);
//			child = null;
//		}
//		else
//		{
//			super.endElement(uri, localName, qName);
//		}
//		nodeString = new String();
//		System.out.println(nodeName +" end element: "+ qName);
		super.endElement(uri, localName, qName);
		if(child == null)
			slist.add(currSignal);

	}
}
