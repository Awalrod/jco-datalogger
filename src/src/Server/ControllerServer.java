package Server;

import Main.Controller;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.nio.ByteBuffer;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ControllerServer extends WebSocketServer{
    Controller controller;
    public ControllerServer(String host, int port,Controller cont)throws UnknownHostException{
        super(new InetSocketAddress(host,port));
        controller = cont;
    }
    public ControllerServer(InetAddress addr, int port,Controller cont)throws UnknownHostException{
        super(new InetSocketAddress(addr,port));
        controller = cont;
    }
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake){
        if(controller.getRecordingStatus()){
           conn.send("{\"recordingStatus\":true}"); 
        }else{
           conn.send("{\"recordingStatus\":false}"); 
        }    
        
    }
    @Override
    public void onMessage(WebSocket conn, String message){
        //no splits
        if(message.equalsIgnoreCase("fileRequest")){
            String jsonText = controller.getFileDetails();
            conn.send("{\"fileList\": "+jsonText+"}");
        }else if(message.equalsIgnoreCase("clearData")){
           controller.clearData(); 
        }else if(message.equalsIgnoreCase("zipRequest")){
            String zipName = controller.createZip();
            conn.send("{\"zipCreated\":\""+zipName+"\"}");
        }
        else{//split
            processMessage(message);
        }
    }
    
    private String processMessage(String message){
        String[] splitString = message.split("=");
        switch(splitString[0])
        {
        case "numSamples":
            return setSampleSize(message);
        case "recording":
            return setRecording(message);
        case "fileName":
            return setBaseName(message);
        case "sampleRate":
            return setSampleRate(message);
        /*case "status":
            return getStatus();
                case "clearData":
            return clearData();*/
        default:
            return ("ERROR: Unknown message");
        }
    }
    public void pushFiles(){
        String jsonText = controller.getFileDetails();
        broadcast("{\"fileList\": "+jsonText+"}");    //Sends fileList to all connections
    }
    private String setSampleRate(String message){
        int sampleRate = Integer.parseInt(message.split("=")[1]);
        controller.setSampleRate(sampleRate);
        return Integer.toString(sampleRate);
    }
    private String setSampleSize(String message){
        int size = Integer.parseInt(message.split("=")[1]);
        controller.setSampleSize(size);
        return Integer.toString(size);
    }
    
    private String setRecording(String message){
        String toState = message.split("=")[1];
        switch(toState)
        {
        case "start":
            controller.setRecording(true);
            break;
        case "stop":
            controller.setRecording(false);
            break;
        }
        return toState;
    }
    private String setBaseName(String message){
        String baseName = message.split("=")[1];
        controller.setBaseName(baseName);
        return baseName;
    }
    
    
    
    
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote){}
    @Override
    public void onMessage(WebSocket conn, ByteBuffer message){}
    @Override 
    public void onError(WebSocket conn, Exception ex){}
    @Override
    public void onStart(){}

    
}
