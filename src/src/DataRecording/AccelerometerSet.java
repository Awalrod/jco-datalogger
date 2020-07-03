package DataRecording;

import java.sql.Timestamp;
import com.google.gson.*;
import java.time.Instant;

public class AccelerometerSet {

//	private String type;
	private double ts;
	private AccelerometerReading accel1[];

	public AccelerometerSet(AccelerometerReading ar[], Instant instant){
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//		type = "Accel1";
		int i;
		int j = 0;
		for(i = 0; i<ar.length; i++){
			if(ar[i] == null)
				break;
			j++;
		}
		accel1 = new AccelerometerReading[j];
		for(i=0; i<j; i++){
			accel1[i] = ar[i];
		}
		long secs = instant.getEpochSecond();
                long nano = instant.getNano();
                ts = secs+ (double)nano/1e9;
		
		//data = ar;
	}

	public AccelerometerSet(AccelerometerReading ar[]){
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//		type = "Accel1";
		int i;
		int j = 0;
		for(i = 0; i<ar.length; i++){
			if(ar[i] == null)
				break;
			j++;
		}
		accel1 = new AccelerometerReading[j];
		for(i=0; i<j; i++){
			accel1[i] = ar[i];
		}
		ts = (double)timestamp.getTime()/1000.0;
		//data = ar;
	}

	public String toJson(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}

}
