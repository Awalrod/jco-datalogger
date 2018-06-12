package Server;

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

import java.util.ArrayList;

import com.gcdc.canopen.CanOpenListener;
import com.gcdc.can.CanMessage;
import com.gcdc.canopen.SubEntry;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class StreamServer extends WebSocketServer {
	
	//WebSocket currentConnection;
	ArrayList<WebSocket> streams = new ArrayList<WebSocket>();
	
	public StreamServer( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
		
	}

	public StreamServer( InetSocketAddress address ) {
		super( address );
	}
	
	public StreamServer(String host, int port) throws UnknownHostException{
		super( new InetSocketAddress(host,port));
	}
	

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " connected" );	
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		System.out.println( conn + " has left" );
	}

	@Override //from WebSocketServer
	public void onMessage( WebSocket conn, String message ) {
		System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + ": " + message );
		switch(message){
			case "stream?=true":
				streams.add(conn);
				break;
			case "stream?=false":
				streams.remove(conn);
				break;
		}
	}
	@Override
	public void onMessage( WebSocket conn, ByteBuffer message ) {
		System.out.println( "BYtebuffer"+ conn + ": " + message );
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
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	@Override
	public void onStart() {
		System.out.println("Server started!");
	}
	
	public void stream(String jsonText){
		//System.out.println(isStreaming);
		ArrayList<WebSocket> toRemove = new ArrayList<WebSocket>();
		for(WebSocket conn: streams){
			try{conn.send(jsonText);}
			catch(Exception e){
				if(conn!=null){
					//System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress()+ "Couldn't send json message");
						
				}				
				toRemove.add(conn);
				e.printStackTrace();
			}
		}
		for(WebSocket conn: toRemove){
			streams.remove(conn);
		}		
	}
	
	
}	