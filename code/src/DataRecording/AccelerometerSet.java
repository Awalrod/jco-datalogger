package DataRecording;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.google.gson.*;

public class AccelerometerSet {

	private String type;
	private String ts;
	private AccelerometerReading data[];

	public AccelerometerSet(AccelerometerReading ar[]){
		SimpleDateFormat timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		type = "Accel1";
		ts = timestamp.format(date);
		data = ar;
	}

	public String toJson(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}

}
