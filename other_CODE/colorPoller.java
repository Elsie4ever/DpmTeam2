package trotty02;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

public class colorPoller extends Thread{
	SampleProvider colorSensor;
	float[] sample;
	public int colorID;
	public boolean seesBlock;
	public int[][] map;
	
	public colorPoller(SampleProvider colorSensor, float[] sample) {
		this.colorSensor = colorSensor;
		this.sample = sample;
		this.map = map;
	}

	public void run() {

		while (true) {
			colorSensor.fetchSample(sample,0);							// acquire data
			colorID =(int)(sample[0] * 1000.0);					// extract from buffer, cast to int		
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
	public boolean seesBlock(){ //is there a styro block
		return seesBlock;
	}
}