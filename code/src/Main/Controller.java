package Main;

import java.net.Socket;
import java.io.PrintStream;
public class Controller{
    DataLogger dataLogger;
    int busmasterPort = 7332;
    public Controller(DataLogger dInstance){
        dataLogger = dInstance;    
    }
    public void setSampleSize(int sampleSize){
        dataLogger.fileLength = sampleSize;    
    }
    public void setRecording(boolean recordingState){
        if(recordingState){
            dataLogger.startSyncListener();
        }else{
            dataLogger.stopSyncListener();
        }
    }
    public void setBaseName(String name){
        dataLogger.baseName = name;
    }
    public void setSampleRate(int sampleRate){
        Socket socket;
        PrintStream out;
        try{
            socket = new Socket("127.0.0.1" ,busmasterPort);//portnum for busmaster
            out = new PrintStream(socket.getOutputStream());
        }catch(Exception e){
            e.printStackTrace();
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        sb.append("<config>\n");
        sb.append("\t<slave_nodes>\n");
        sb.append("\t\t<node id=\"local\">\n");
        sb.append("\t\t<nodeid>local</nodeid>\n");
        sb.append("\t\t\t<obj_dict><index>0x1006</index><subindex>0</subindex><type>int32</type>\n");
        sb.append("\t\t\t\t<val>");
        sb.append((float)1000000/(float)sampleRate);//Time interval in microseconds
        sb.append("</val>\n");
        sb.append("\t\t\t</obj_dict>\n");
        sb.append("\t\t</node>\n");
        sb.append("\t</slave_nodes>\n");
        sb.append("</config>\n");
        out.print(sb.toString());
        try{
            out.close();
            socket.close();        
        }catch(Exception e){
            e.printStackTrace();
        }
    }     
    
}