package trotty02;

import java.util.Arrays;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import trotty02.Navigation;

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;
	private float[] colorArray = new float[3];
	private Navigation simNav;
	private UltrasonicPoller usPoller;
	private EV3LargeRegulatedMotor lightMotor;
	private static final double BLACK_LINE = 0.35;
	private static final float TRAVEL_SPEED = 200;
	private static final double SQUARE_DIST = 30.48;
	private static final double OFFSET = 9.8;
	private static final double US_OFFSET_FRONT = 12.5;
	private static final double US_OFFSET_BACK = 3.5;
	private static double xCorner;
	private static double yCorner;
	
	
	public LightLocalizer(Odometer odo, UltrasonicPoller usPoller, SampleProvider colorSensor, float[] colorData,
			EV3LargeRegulatedMotor lightMotor, Navigation simNav) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.simNav = simNav;
		this.usPoller = usPoller;
		this.lightMotor = lightMotor;
		
		// This needs to be switched to the values gotten from wifi
		xCorner = 30.48;
		yCorner = 30.48;
	}
	
	public void doLocalization() {
		double angleX1 = 0;
		double angleX2 = 0;
		double angleY1 = 0;
		double angleY2 = 0;
		double distA, distB;
		
		// turn the light sensor to face the ground
		lightMotor.rotate(-90);
		
		// drive to location listed in tutorial
		simNav.turnTo(270);
		distA = UltrasonicPoller.getDistFront() + US_OFFSET_FRONT;
		simNav.turnTo(0);
		distB = UltrasonicPoller.getDistBack() + US_OFFSET_BACK;
		
		simNav.turnTo(45);
		simNav.travelTo(odo.getX() + (SQUARE_DIST - distB), odo.getY() + (SQUARE_DIST - distA));
		simNav.stopMov();
		
		// start rotating and clock all 4 gridlines
		simNav.turnCW();
		int counter = 0;
		while(simNav.isTurning()){
			colorSensor.fetchSample(colorData, 0);
			if(colorData[0] <= BLACK_LINE & counter == 0){
				angleX2 = odo.getTheta();
				counter ++;
				Sound.beep();
				try { Thread.sleep(500); } catch(Exception e){}
			}
			else if(colorData[0] <= BLACK_LINE & counter == 1){
				angleY1 = odo.getTheta();
				counter ++;
				Sound.beep();
				try { Thread.sleep(500); } catch(Exception e){}
			}
			else if(colorData[0] <= BLACK_LINE & counter == 2){
				angleX2 = odo.getTheta();
				counter ++;
				Sound.beep();
				try { Thread.sleep(500); } catch(Exception e){}
			}
			else if(colorData[0] <= BLACK_LINE & counter == 3){
				simNav.stopMov();
				angleY2 = odo.getTheta();
				counter ++;
				Sound.beep();
				break;
			}
		}
		
		// do trig to compute (0,0) and 0 degrees
		double X = -OFFSET*Math.cos((angleY2 - angleY1)/2);
		double Y = -OFFSET*Math.cos((angleX2 - angleX1)/2);
		double H = Math.PI/2 -((angleY2 - Math.PI) - (angleY2-angleY1)/2);
		
		// when done travel to (0,0) and turn to 0 degrees
		simNav.stopMov();
		simNav.turnTo(0);
		simNav.stopMov();
		odo.setTheta(odo.getTheta() + H);
		simNav.stopMov();
		simNav.travelTo(odo.getX()+X, odo.getY()+Y);
		odo.setX(xCorner);
		odo.setY(yCorner);
		simNav.turnTo(0);
		simNav.stopMov();
		
		// return the light sensor to face the front
		lightMotor.rotate(90);
	}
}
