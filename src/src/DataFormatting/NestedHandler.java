package DataFormatting;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Created by gcdc on 7/3/2020.
 */
public class NestedHandler extends DefaultHandler
{
	protected String nodeName;
	protected NestedHandler parent;
	protected NestedHandler child;
	private boolean isActive;
	protected String nodeString;

	public NestedHandler(NestedHandler parent, String name)
	{
		this.parent = parent;
		this.nodeName = name;
		isActive = true;
		nodeString = new String();
	}
	public NestedHandler(String name )
	{
		this(null, name);
	    }

	public boolean isNodeActive()
	{
		return(isActive);
	}


	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if(child == null)
			return;
		child.startElement(uri, localName, qName, attributes);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if(child == null)
		{
			//            System.out.println(nodeName +" endElement: "+ qName);
			if(qName.equals(nodeName))
			{
				//                System.out.println("deactiviating");
				isActive = false;
			}
			return;
		}
		//        System.out.println(nodeName +" endElement for child: "+ qName);

		child.endElement(uri, localName, qName);
		if(!child.isNodeActive())
		{
			//            System.out.println(child.nodeName +" removed on: "+ qName);
			child = null;
		}
	}

	@Override
	public void notationDecl(String name,String publicId,String systemId)throws SAXException
	{
		if(child == null)
			return;
		child.notationDecl( name, publicId, systemId);
	}

	@Override
	public void characters(char[] ch, int start,int length)throws SAXException
	{
		if(child == null)
		{
			nodeString += new String(ch, start, length).trim();
			return;
		}
		child.characters( ch, start, length);
	}

}
