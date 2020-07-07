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

public class Simulator extends NestedHandler
{
	List<Channel> clist;
	Channel currChannel;

	public Simulator(String name)
	{
		super(name);
		clist = new ArrayList<Channel>();
//		System.out.println("new "+nodeName);
	}

	public Simulator()
	{
		this("simulator");
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		super.startElement(uri, localName, qName, attributes);		
//		System.out.println(nodeName +" start element: "+ qName);
		
		if(qName.equalsIgnoreCase("channel")) {
			currChannel = new Channel();
			child = currChannel;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
//		if (qName.equalsIgnoreCase("channel"))
//		{
//			clist.add(currChannel);
//			child = null;
//		}
//		else
//		{
//		System.out.println(nodeName +" end element: "+ qName);
			super.endElement(uri, localName, qName);
			if(child == null)
				clist.add(currChannel);
//		}
	}
}
