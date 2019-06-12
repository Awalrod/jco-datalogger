package DataRecording;

import java.sql.Timestamp;
import com.google.gson.*;

public class AccelerometerSet {

	private double ts;
	private AccelerometerReading data[];

	public AccelerometerSet(AccelerometerReading ar[]){
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		ts = timestamp.getTime();
		data = ar;
	}

	public String toJson(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}

}
