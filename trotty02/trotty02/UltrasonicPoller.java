package trotty02;
import lejos.robotics.SampleProvider;
import lejos.hardware.motor.*;

//
//  Control of the wall follower is applied periodically by the 
//  UltrasonicPoller thread.  The while loop at the bottom executes
//  in a loop.  Assuming that the us.fetchSample, and cont.processUSData
//  methods operate in about 20mS, and that the thread sleeps for
//  50 mS at the end of each loop, then one cycle through the loop
//  is approximately 70 mS.  This corresponds to a sampling rate
//  of 1/70mS or about 14 Hz.
//


public class UltrasonicPoller extends Thread{
	private SampleProvider us;
	private float[] usData;
	public static int distance = 0;
	//private boolean usForward; //create boolean in USPoller
	public boolean seesSomething = false;
	public boolean usForward = true;
	public int filterControl = 0;
	public int FILTER_OUT = 10;

	public UltrasonicPoller(SampleProvider us, float[] usData) {
		this.us = us;
		this.usData = usData;
	}

//  Sensors now return floats using a uniform protocol.
//  Need to convert US result to an integer [0,255]
	
	public void run() {

		while (true) {
			us.fetchSample(usData,0);							// acquire data
			distance =(int)(usData[0]*100.0);					// extract from buffer, cast to int
			if (distance > 50)
				distance = 50;
			//cont.processUSData(distance, usDirection);			// now take action depending on value
			if (distance > 50 && filterControl < FILTER_OUT) {
				// bad value, do not set the distance var, however do increment the filter value
				filterControl++;
			} 
			else {
				// distance went below 50, therefore reset everything.
				filterControl = 0;
			}
			
			//determine if there is an object in sight radius
			if((distance < 50)&&(distance > 0)){	//if it sees an object
				this.seesSomething = true;

			}
			else{
				this.seesSomething = false;

			}
			

				try { Thread.sleep(70);  } catch(Exception e){}		// Poor man's timed sampling
			}
			
			
	}

	public static int getDistance(){
		return distance;
	}
	
	public boolean seesSomething(){ //is there an object at all
		return seesSomething;
	}


	
}