package DataFormatting;


import java.time.*;


public class Sinusoid extends Signal
{
    public Sinusoid(String name)
    {
        super(name);
    }
    
    public double evaluate(Instant t)
    {
        return(7.43);
    }
	
}
