package DataFormatting;


import java.time.*;
import java.math.*;

public class Sinusoid extends Signal
{
	public Sinusoid(String name)
	{
		super(name);
	}

	public double evaluate(Instant t)
	{
//	   double freq;
//        double amplitude;
//        double t0;
//        double offset;
//        double jitter;
//		double phase = 0;
		double td = (double)t.getEpochSecond() + ((double)t.getNano()/1e9);
//System.out.println("t:"+td);

		double retval = offset + amplitude* Math.sin( 2.0*Math.PI*freq*(td-t0) + phase);
		return(retval);
	}

}
