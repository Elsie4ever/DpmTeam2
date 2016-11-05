package trotty02;

import lejos.robotics.SampleProvider;

//
//  Control of the wall follower is applied periodically by the 
//  UltrasonicPoller thread.  The while loop at the bottom executes
//  in a loop.  Assuming that the us.fetchSample, and cont.processUSData
//  methods operate in about 20mS, and that the thread sleeps for
//  50 mS at the end of each loop, then one cycle through the loop
//  is approximately 70 mS.  This corresponds to a sampling rate
//  of 1/70mS or about 14 Hz.
//

public class UltrasonicPoller extends Thread {
	private SampleProvider us;
	private float[] usData;
	private int distance;
	private boolean seesSomething; 
	//Constructor
	public UltrasonicPoller(SampleProvider us, float[] usData) {
		this.us = us;
		this.usData = usData;
	}

	//return Current distance.
	public int getDistance() {
		return distance;
	}

    //get distance and then stores in distance variable.
	public void run() {

		while (true) {
			us.fetchSample(usData, 0);  
			distance = (int) (usData[0] * 100.0);  
		    
			if(distance<5){
				this.seesSomething=true;
			}
			else{
				this.seesSomething=false;
			}
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}  
		}
	}
	public boolean seesSomething(){ //is there a styro block
		return seesSomething;
	}

}
