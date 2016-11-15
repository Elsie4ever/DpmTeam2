package trotty02;

import lejos.robotics.SampleProvider;
import trotty02.USLocalizer.LocalizationType;
import trotty02.UltrasonicPoller;
/**
 * 
 * @author Adam, Elsie, Shi Yu, Jean-Christophe, Ke
 *
 */
public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static double ROTATION_SPEED = 30;

	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	private UltrasonicPoller usPoller = new UltrasonicPoller(usSensor, usData);
	private Navigation navigator = null;
/**
 * Constructor 1 for the Localizer using the ultrasonic sensor
 * @param odo odometer object
 * @param usSensor ultrasonic sensor object
 * @param usData a float array of the data read by the sensor
 * @param locType the type of localization, falling edge oor rising edge
 * @param navigator navigator object
 */
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType, Navigation navigator) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		this.navigator = navigator;
	}
	/**
	 * COnstructor 2 for the Localizer using the ultrasonic sensor
	 * @param odo odometer object
	 * @param usPoller ultrasonic poller object
	 * @param locType the type of localization, either falling edge or rising edge
	 * @param navigator navigator object
	 */
	public USLocalizer(Odometer odo,UltrasonicPoller usPoller, LocalizationType locType, Navigation navigator) {
		this.odo = odo;
		this.usPoller = usPoller;
		this.locType = locType;
		this.navigator = navigator;
	}
/**
 * Localization algorithm
 */
	public void doLocalization() {
		double [] pos = new double [3];
		double angleA, angleB;



		if (locType == LocalizationType.FALLING_EDGE) {

			while(usPoller.seesSomething() || usPoller.getDistance() == 0){ //while it sees a wall, keep rotating
				navigator.turnTo(odo.getAng() + 15, true);
			}

			while(!usPoller.seesSomething()){ //while it sees nothing, keep rotating
				// rotate the robot by +1 degree until it sees a wall
				navigator.turnTo(odo.getAng() - 15, false);
			}

			//then latch the angle
			angleA = odo.getAng();



			// switch direction and wait until it sees no wall
			while(usPoller.seesSomething()){ //while it sees something, keep rotating
				// rotate the robot by -1 degree
				navigator.turnTo(odo.getAng() + 15, false);
			}


			// keep rotating until the robot sees a wall
			while(!usPoller.seesSomething()){ 
				navigator.turnTo(odo.getAng() + 15, true);

			}
			angleB = odo.getAng();
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'

			//if angleA is lower than 0, it jumps up to 360, this fixes the math
			if(angleA > 180){
				angleA-=360;
			}			

			// update the odometer position (example to follow:)

			odo.setPosition(new double [] {0.0, 0.0, (45 - ((angleA - angleB)/2))}, new boolean [] {true, true, true});
			navigator.turnTo(0, true); //adjust heading so it faces pos x axis

		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			while(usPoller.seesSomething() || usPoller.getDistance() == 0){ //while it sees a wall, keep rotating
				navigator.turnTo(odo.getAng() + 10, true);
			}

			while(!usPoller.seesSomething()){ //while it sees nothing, keep rotating
				navigator.turnTo(odo.getAng() - 10, false);
			}
			angleA = odo.getAng();

			while(usPoller.seesSomething()){ //while it sees a wall, keep rotating
				navigator.turnTo(odo.getAng() - 10, true);
			}

			angleB = odo.getAng();



			if(angleA > 180){
				angleA-=360;
			}
			

			odo.setPosition(new double [] {0.0, 0.0, ((0 - (angleA - angleB)/2))}, new boolean [] {true, true, true});
				
		}
		
	navigator.turnTo(180, true);
	odo.setPosition(new double[] {usPoller.distance+7, 0, 0}, new boolean[]{true, false, false}); 
		//the 7 compensates for hardware inaccuracies
	navigator.turnTo(270, true);
	odo.setPosition(new double[] {0, usPoller.distance+7, 0}, new boolean[]{false, true, false}); 
	
	navigator.turnTo(0, true);


	
	//navigator.travelTo(30, 30);
	//navigator.turnTo(0, true);
	
	}

	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0];

		return distance;
	}

}
