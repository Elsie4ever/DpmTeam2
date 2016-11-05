package trotty02;

import java.lang.reflect.Array;
import java.util.*;
import lejos.hardware.*;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.Sound;

public class objectID {
	private SampleProvider colorSensor;
	private SampleProvider usSensor;
	private int errorFilter, errorFilterMax, distanceMax;
	private colorPoller ColorPoller;
	private UltrasonicPoller USpoller;
	private Mapping mapping;
	private Odometer odo;
	private float[] colorData, usData;
	final TextLCD t = LocalEV3.get().getTextLCD();
	private boolean isBlock;
	float color;

	//constructor for detection mechanism
	public objectID(SampleProvider usSensor, float[] usData, SampleProvider colorSensor, float[] colorData) {
		this.usSensor = usSensor;
		this.usData = usData;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.usSensor = usSensor;
		this.usData = usData;
	}

	public void run() { 
		double distance;
		while (true) {
			distance = USpoller.getDistance();//get current distance from ultrasonic sensor											 
			if (USpoller.seesSomething()) {	//if the distance is smaller than 5, start detcting																		 
				t.drawString("OBJECT DETECTED", 0, 5);	//show that object is being detected							 

				while (USpoller.seesSomething()) {	//when the distance is smaller than 5											 																 
				isBlock=ColorPoller.seesBlock;														 
				if (isBlock){	//print "Block" if the block is styrofoam																				 
					Sound.beep();									
					t.drawString("BLOCK", 5, 6);
				}else{   //print "not Block" if the block is wooden block
					Sound.twoBeeps();               
					t.drawString("NOT BLOCK", 5, 6);
					updateMap(odo.getX(),odo.getY(),odo.getAng(),USpoller.getDistance());
				}
				Delay.msDelay(1000);
				t.clear();
				 
			}
				}}
		
		}

	private void updateMap(double xDis, double yDis, double heading, double usDistance){
		if(heading<90){
			xDis=xDis-usDistance*Math.cos(Math.abs(90-heading));
			yDis=yDis+usDistance*Math.sin(Math.abs(90-heading));
		}
		else if(heading>90 && heading<180){
			xDis=xDis+usDistance*Math.cos(Math.abs(90-heading));
			yDis=yDis+usDistance*Math.sin(Math.abs(90-heading));
		}
		else if(heading>=180 && heading<270){
			xDis=xDis+usDistance*Math.cos(Math.abs(270-heading));
			yDis=yDis-usDistance*Math.sin(Math.abs(270-heading));
		}
		else{
			xDis=xDis-usDistance*Math.cos(Math.abs(270-heading));
			yDis=yDis-usDistance*Math.sin(Math.abs(270-heading));
		}
		//add the position of block to the map matrix
		mapping.map[(int)Math.ceil(xDis/30)][(int)Math.ceil(yDis/30)]=1;
	}
	
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0); //get current distances from sensor 

																																												
		if (usData[0] * 100 > distanceMax && errorFilter < errorFilterMax) {
			errorFilter++;
			return (distanceMax - 1);
		}

		// set the maximum distace
		else if (usData[0] * 100 > distanceMax && errorFilter >= errorFilterMax) {
			return usData[0] * 100;
		}

																																												
		else {
			float distance = usData[0] * 100;
			if (distance > distanceMax) {
				distance = distanceMax;
			}
			if (distance == 0) {
			 
			}
			errorFilter = 0;
			return distance;
		}
	}
}