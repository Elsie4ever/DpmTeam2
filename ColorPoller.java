package trotty02;

import lejos.robotics.SampleProvider;

public class ColorPoller extends Thread {
	private SampleProvider co;
	private float[] coData;
	private float color;
	
	public ColorPoller(SampleProvider co, float[] coData) {
		this.co = co;
		this.coData = coData;
	}
	
	public void run() {
		while (true) {
			co.fetchSample(coData,0);							// acquire data
			color = (coData[0]/coData[2]);						// extract from buffer, ratio R/B
			try { Thread.sleep(50); } catch(Exception e){}		// Poor man's timed sampling
		}
	}
	
	public float getColor(){
		/**
		 * Return the ratio between the color Red and the color Blue
		 * @return	{float} ratio Red/Blue
		 */
		return this.color;
	}
}
