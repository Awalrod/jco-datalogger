package DataRecording;

import java.sql.Timestamp;
import com.google.gson.*;

public class AccelerometerSet {

	private String type;
	private double ts;
	private AccelerometerReading data[];

	public AccelerometerSet(AccelerometerReading ar[]){
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		type = "Accel1";
		int i;
		int j = 0;
		for(i = 0; i<ar.length; i++){
			if(ar[i] == null)
				break;
			j++;
		}
		data = new AccelerometerReading[j];
		for(i=0; i<j; i++){
			data[i] = ar[i];
		}
		ts = (double)timestamp.getTime()/1000.0;
		//data = ar;
	}

	public String toJson(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}

}
