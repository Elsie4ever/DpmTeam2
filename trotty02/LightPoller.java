package trotty02;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
/**
 * 
 * @author Adam
 * @since 3.0
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
	 * 
	 * @param colorSensor reads the values that the color sensor is reading
	 * @param sample	the values that the color sensor is displaying
	 */
	public LightPoller(SampleProvider colorSensor, float[] sample) {
		this.colorSensor = colorSensor;
		this.sample = sample;
	}
/**
 * starts the sampling process
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
	 * determines whether or not the bot is currently looking at a block
	 * @return the status of if the bot is looking at a block or not
	 */
	public boolean seesBlock(){ //is there a block
		return seesBlock;
	}
	/**
	 * gets the current values being read by the color sensor
	 * @return the most recent values read by the sensor
	 */
	public double [] getRGB(){
		double [] RGB = new double[3];
		
		RGB[0] = sample[0]*100;
		RGB[1] = sample[1]*100;
		RGB[2] = sample[2]*100;
		
		return RGB;
	}
	
	/**
	 * determines whether or not the bot is currently looking at a styro block
	 * @return the status of if the bot is looking at a styrofoam block or not
	 */
	public boolean isFoamBlock(){
		double[] RGB = getRGB();
		if(RGB[0] < RGB[1] && RGB[2] < RGB[1]){
			return true;
		}
		return false;
	}
}