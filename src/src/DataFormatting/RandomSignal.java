package DataFormatting;


import java.time.*;
import java.math.*;
import java.util.Random;

public class RandomSignal extends Signal
{
	Random rand;
	
	public RandomSignal(String name)
	{
		super(name);
		rand = new Random();	
	}

	public double evaluate(Instant t)
	{
//	   double freq;
//        double amplitude;
//        double t0;
//        double offset;
//        double jitter;
		double phase = 0;
		double td = (double)t.getEpochSecond() + ((double)t.getNano()/1e9);
//System.out.println("t:"+td);

		double retval = offset + amplitude* rand.nextGaussian();
		return(retval);
	}

}
