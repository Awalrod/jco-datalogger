package Server;

import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.exceptions.*;

import java.util.ArrayList;
import java.util.Iterator;

import com.gcdc.canopen.CanOpenListener;
import com.gcdc.can.CanMessage;
import com.gcdc.canopen.SubEntry;

import DataRecording.AccelerometerReading;
import DataFormatting.DataFormatter;

import java.time.Instant;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class StreamServer extends WebSocketServer {
	
	//WebSocket currentConnection;
//	ArrayList<WebSocket> streams = new ArrayList<WebSocket>();
	ArrayList<Streamer> streams = new ArrayList<Streamer>();
	int port;
	InetSocketAddress isa;

	public StreamServer( InetSocketAddress address ) {
		super( address );
		setReuseAddr(true);
		isa = address;
//		System.out.println("interface "+isa.getHostString()+":"+isa.getPort());
	}
	
	public StreamServer( int port ) throws UnknownHostException {
		this( new InetSocketAddress( port ) );
	}

	public StreamServer(InetAddress host, int port) throws UnknownHostException{
                this( new InetSocketAddress(host,port));
        }

	public StreamServer(String host, int port) throws UnknownHostException{
		this( new InetSocketAddress(host,port));
	}
	

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " connected" );
		synchronized(streams){streams.add(new Streamer(conn,0));}	
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
	try{
		System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress()  +":"+isa.getPort()+ " has left" );
	} catch(NullPointerException e)
	{
		System.out.println("onClose   " +e);
	}
		Streamer toRemove;
		toRemove = getStreamByConn(conn);
		if(toRemove !=null){
			synchronized(streams){streams.remove(toRemove);}
		}
		conn.close();
	}

	@Override //from WebSocketServer
	public void onMessage( WebSocket conn, String message ) {
		System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + ":s (" + message + ")" );
		if(message.contains("nodeid=")){
			if(message.contains("all")){
				getStreamByConn(conn).setStreamAll(true);
			}
			else
			{
				Integer nodeId = Integer.decode(message.substring(7));
				getStreamByConn(conn).setNodeId(nodeId);
				getStreamByConn(conn).setStreamAll(false);
			}
		}
		else if(message.contains("stream="))
		{
			if( message.contains("stream=true"))
			{
				System.out.println("streaming set to true");
				getStreamByConn(conn).setStreaming(true);
			}
			else if( message.contains("stream=false"))
				getStreamByConn(conn).setStreaming(false);
			else
				System.out.println("stream uknown type");
		}
		else
		{
			System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + ": unknown message (" + message + ")" );
			conn.send("unknown command");
		}
	}
	
	
	@Override
	public void onMessage( WebSocket conn, ByteBuffer message ) {
		System.out.println( "BYtebuffer"+ conn + ":b (" + message + ")" );
	}


/*	public StreamServer( String[] args ) throws InterruptedException , IOException {
		WebSocketImpl.DEBUG = true;
		int port = 8080; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		InetSocketAddress isa = new InetSocketAddress("192.168.1.105",port);
		System.out.println(isa.getHostName());
		
		System.out.println( "StreamServer started on port: " + s.getPort() );

	}*/
	
	
	@Override
	public void onError( WebSocket conn, Exception ex ) {
		
		
		System.out.println("StreamServer "+isa.getHostString()+":" + isa.getPort() + " onError message received\n" + ex);
		ex.printStackTrace();
		if( conn != null ) {
			System.out.println(conn);
			// some errors like port binding failed may not be assignable to a specific websocket
		}
		System.exit(4);
	}

	@Override
	public void onStart() {
		System.out.println("StreamServer started!");
	}
	
	public void stream(AccelerometerReading readings[])
	{
		synchronized(streams)
		{
			Iterator<Streamer> s1 = streams.iterator();
			while(s1.hasNext())
			{
				Streamer s = s1.next();
				try{
					s.stream(readings);
				}
				catch(WebsocketNotConnectedException e)
				{
					s.setStreaming(false);
					System.out.println("removing stream because: "+e);
//					streams.remove(s);
				}
			}
		}

//		broadcast(readings);
	}

	public void pushToStream(AccelerometerReading readings[], Instant instant)
	{
		synchronized(streams)
		{
			Iterator<Streamer> s1 = streams.iterator();
			while(s1.hasNext())
			{
				Streamer s = s1.next();
				try{
					s.pushToStream(readings, instant);
				}
				catch(WebsocketNotConnectedException e)
				{
					s.setStreaming(false);
					System.out.println("removing stream because: "+e);
//					streams.remove(s);
				}
			}
		}

//		broadcast(readings);
	}
	
/*	public void stream(String jsonText){
		//System.out.println(isStreaming);
		ArrayList<WebSocket> toRemove = new ArrayList<WebSocket>();
		
		for(WebSocket conn: streams){
			try{conn.send(jsonText);}
			catch(Exception e){
				toRemove.add(conn);
				e.printStackTrace();
			}
		}
		for(WebSocket conn: toRemove){
			streams.remove(conn);
		}		
	}
*/	
	public Streamer getStreamByConn(WebSocket conn){
		synchronized(streams){
			for(Streamer s: streams){
				if(conn == s.conn){
					return s;
				}
			}
		}
		return null;
	}
	
	public void shutdown() throws IOException, InterruptedException{
//		Iterator<WebSocket> i = this.getConnections().iterator();
//		while(i.hasNext())
//		{
//			System.out.println("closing conneciton");
//			i.next().closeConnection(1,"exit");
//		}
//WebSocketServerFactory wsf = getWebSocketFactory();
		stop(10000);
	}
	
	
}	
