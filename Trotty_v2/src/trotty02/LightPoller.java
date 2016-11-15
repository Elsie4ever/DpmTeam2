package trotty02;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
/**
 * 
 * @author 
 *
 */
public class LightPoller extends Thread{
	private Odometer odo;
	private boolean seesLine = false;
	private double distSensorBot;
	//EV3ColorSensor cS = new EV3ColorSensor(LocalEV3.get().getPort("S2"));
	//SampleProvider colorSensor = cS.getColorIDMode();
	//int sampleSize = colorSensor.sampleSize();   
	//float[] sample = new float[sampleSize];
	SampleProvider colorSensor;
	float[] sample;
	public int colorID;
	public boolean seesBlock;

	/**
	 * Receives information from the light sensor
	 * @param colorSensor
	 * @param sample
	 */
	public LightPoller(SampleProvider colorSensor, float[] sample) {
		this.colorSensor = colorSensor;
		this.sample = sample;
	}
/**
 * Runs the light poller, allows the robot to read and process the information received from the light sensor.
 */
	public void run() {
		while (true) {
			colorSensor.fetchSample(sample,0);							// acquire data
			colorID =(int)(sample[0] * 1000.0);					// extract from buffer, cast to int
			/*if (colorID < 0)
				colorID = 0;
			//cont.processUSData(distance, usDirection);			// now take action depending on value
			if (colorID > 50 && filterControl < FILTER_OUT) {
				// bad value, do not set the distance var, however do increment the filter value
				filterControl++;
			} 
			else {
				// distance went below 50, therefore reset everything.
				filterControl = 0;
			}*/
			
			//determine if there is a styrofoam block in sight radius
			if((sample[0] < sample[1]) && (sample[1] > 0) && sample[0] < 300 && sample[1] < 5000 && sample[2] < 5000){	//if it sees a blue block
				this.seesBlock = true;

			}
			else{
				this.seesBlock = false;

			}
			

				try { Thread.sleep(70);  } catch(Exception e){}		// Poor man's timed sampling
			}
			
			
	}
	/**
	 * A boolean that determines if the robot's selected sensor detects a block or not.
	 * @return true if it sees a block, false otherwise
	 */
	public boolean seesBlock(){ //is there a styro block
		return seesBlock;
	}
}
