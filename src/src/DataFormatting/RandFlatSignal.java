package DataFormatting;


import java.time.*;
import java.math.*;
import java.util.Random;
import com.gcdc.mathutils.PseudoRandomFlat;

public class RandFlatSignal extends Signal
{
	PseudoRandomFlat rand;
	double a2 = 0;
	int len = 32;
	
	public RandFlatSignal(String name)
	{
		super(name);
		try{
			rand = new PseudoRandomFlat();	
		}
		catch(Exception e)
		{
			System.out.println("RandFlatSignal, internal error\n"+e);
		}
	}

	public double evaluate(Instant t)
	{
//	   double freq;
//        double amplitude;
//        double t0;
//        double offset;
//        double jitter;
		if( (a2!=amplitude) || (len != (int)freq) )
		{
			a2 = amplitude;
			len = (int)freq;
			try{
				rand =  new PseudoRandomFlat(len, a2);
			}
			catch(Exception e)
			{
				System.out.println("RandFlatSignal, internal error\n"+e);
			}
		}
		return(rand.getNext());
	}
}
