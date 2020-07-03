package Server;

import DataRecording.AccelerometerReading;
import DataFormatting.DataFormatter;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

public class Streamer{
    boolean isStreaming = false;
    boolean streamAll = false;
    WebSocket conn;
    int nodeId;
    
    public Streamer(WebSocket c, int n){
        conn = c;
        nodeId = n;
    }
    
    public void setStreaming(boolean b){
        isStreaming = b;
    }
    
    public void setNodeId(int n){
        nodeId = n;
    }
    public void setStreamAll(boolean b){
        streamAll = b;
    }
    public void stream(AccelerometerReading readings[]) throws WebsocketNotConnectedException
    {
        String jsonText=null;
        if(isStreaming){
            if(streamAll){
                jsonText = DataFormatter.produceJsonString(readings);
            }else{
                try{
                    jsonText = DataFormatter.produceJsonString(readings[nodeId]);
                }catch(ArrayIndexOutOfBoundsException aioobe){
                    aioobe.printStackTrace();
                }
            }
                if(jsonText!=null){
                    conn.send(jsonText);
//System.out.println("*"+jsonText+"*");
                }
  //          }
//            catch(Exception e){
    //            e.printStackTrace();
//                isStreaming = false; //turn it off
    //        }   
        } 
    }
    
}
