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
		ts = timestamp.getTime();
		data = ar;
	}

	public String toJson(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}

}
