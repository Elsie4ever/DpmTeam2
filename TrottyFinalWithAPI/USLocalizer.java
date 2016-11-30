package trotty02;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import trotty02.USLocalizer.LocalizationType;
import trotty02.UltrasonicPoller;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static int ROTATION_SPEED = 100;

	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private int delay = 40;
	private double wheelRadius=2.1;
	private int errorFilter,errorFilterMax,distanceMax,wallDistance;
	private LocalizationType locType;
	private UltrasonicPoller usPoller = new UltrasonicPoller(usSensor, usData);
	private Navigation navigator = null;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int corner;

	/**
	 * constructor to create a localizer object which directs the bot to find its initial position and heading
	 * @param odo the odometer
	 * @param usSensor the ultrasonic sensor being used
	 * @param usData the data being recorded by the front ultrasonic sensor
	 * @param locType the technique of localization being used (Rising or Falling)
	 * @param navigator the Navigator class
	 * @param cornerNum which corner the bot starts in
	 */
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType, Navigation navigator, int cornerNum) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		this.navigator = navigator;
		errorFilter = 0;
		errorFilterMax = 70;
		distanceMax = 70;
		wallDistance = 30;
		this.corner = cornerNum;
	}
	/**
	 * constructor not using a corner input
	 * @param odo the odometer
	 * @param usPoller the ultrasonic poller
	 * @param locType the type of localization
	 * @param navigator the navigator object
	 * @param corner the start corner
	 */
	public USLocalizer(Odometer odo,UltrasonicPoller usPoller, LocalizationType locType, Navigation navigator, int corner) {
		this.odo = odo;
		this.usPoller = usPoller;
		this.locType = locType;
		this.navigator = navigator;
		errorFilter = 0;
		errorFilterMax = 70;
		distanceMax = 70;
		wallDistance = 30;
	}

	/**
	 * Does the localization of the type selected
	 */
	public void doLocalization() {
		double [] pos = new double [3];
		double angleA, angleB;



		if (locType == LocalizationType.FALLING_EDGE) {

			// rotate the robot until it sees no wall
						double currentDistance = usPoller.getDistance();

						//double currentDistance = this.getFilteredData();
						while (currentDistance <= wallDistance) {					//if the distance is less than wallDistance
																					//then keep rotating until it sees no wall
							navigator.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);	
							Delay.msDelay(delay);										
							currentDistance = usPoller.getDistance();
						}

						// keep rotating until the robot sees a wall, then latch the angle

						while (currentDistance > wallDistance) {					//if the distance is bigger than wallDistance
																					//then keep rotating in the same direction
																					//until it sees a wall
							navigator.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);			
							Delay.msDelay(delay);										
							currentDistance = usPoller.getDistance();
						}
						angleA = odo.getAng();	//record angleA
						// switch direction and wait until it sees no wall
			            
						while (currentDistance <= wallDistance) {					//if the distance is less than wallDistance
																					//then keep rotating in the opposite direction
																					//until it sees no wall
							navigator.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);			
							Delay.msDelay(delay);
							Delay.msDelay(3000);
							currentDistance = usPoller.getDistance();
						}

						// keep rotating until the robot sees a wall, then latch the angle
						while (currentDistance > wallDistance) {					//if the distance is bigger than wallDistance
																					//then  rotating in the same direction
																					//until it sees another wall
							navigator.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);			
							Delay.msDelay(delay);										
							currentDistance = usPoller.getDistance();
						}
					
						angleB = odo.getAng();										//record angleB
						// angleA is clockwise from angleB, so assume the average of the
						// angles to the right of angleB is 45 degrees past 'north'

						odo.getLeftMotor().stop(true);				//stop the robot from turning to get more accurate reading
						odo.getRightMotor().stop(false);

						double deltaDegree; 
						deltaDegree = (360+angleB+45-(((360+angleB-angleA)%360)/2))%360-45;	//calculate the new degree which it
																							//should turn to based on angle A and B
						navigator.turnTo(deltaDegree, true);										//turn to that degree
						odo.setPosition(new double[] { 0.0, 0.0, 135.0 }, new boolean[] { true, true, true }); //reset position
						// update the odometer position (example to follow:)
						Delay.msDelay(1000);
					
			            Sound.beep();

		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			while (!usPoller.seesSomething()) {		//if the distance is bigger than wallDistance
															//then keep rotating until it sees a wall
				navigator.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);			
				Delay.msDelay(delay);										
				

			}

			// keep rotating until the robot sees no wall, then latch the angle

			while (usPoller.seesSomething()) {		//if the distance is less than wallDistance
															//then keep rotating in the same direction
															//until it sees no wall
				navigator.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);			
				Delay.msDelay(delay);										
				

			}
			angleA = odo.getAng();							//record angleA

			// switch direction and wait until it sees a wall

			while (!usPoller.seesSomething()) {		//if the distance is bigger than wallDistance
															//then keep rotating in the opposite direction
															//until it sees a wall
				navigator.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);			
				Delay.msDelay(delay);										
			

			}

			// keep rotating until the robot sees no wall, then latch the angle
			while (usPoller.seesSomething()) {		//if the distance is less than wallDistance
															//then  rotating in the same direction
															//until it sees no wall
				navigator.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);			
				Delay.msDelay(delay);										
			

			}
			angleB = odo.getAng();							//record angleB

			// angleB is clockwise from angleA, so assume the average of the
			// angles to the right of angleA is 45 degrees past 'north'

			odo.getLeftMotor().stop(true);
			odo.getRightMotor().stop(false);				//stop the robot to get more accurate reading

			double deltaDegree;
			

			deltaDegree = (360+angleA+45-(((360+angleA-angleB)%360)/2))%360;		//calculate the new degree which it
																					//should turn to based on angle A and B
			navigator.turnTo(deltaDegree, true);											//turn to that degree

			// update the odometer position (example to follow:)
			
			Delay.msDelay(4000);  				//delay 4 seconds to stable the robot before reset position
			
			odo.setPosition(new double[] { 0.0, 0.0, 90.0 }, new boolean[] { true, true, true }); 
            Sound.beep();

		}
	
	navigator.turnTo(135, true);
	odo.setPosition(new double[] { 15, 15, 45.0 }, new boolean[] { true, true, true }); 
	
	if(corner == 2)
		odo.setPosition(new double[] {360-odo.getX(), 0, 135}, new boolean[]{true, false, true}); 
	if(corner == 3)
		odo.setPosition(new double[] {360-odo.getX(), 360-odo.getY(), 225}, new boolean[]{true, true, true}); 
	if(corner == 4)
		odo.setPosition(new double[] {0, 360-odo.getY(), 315}, new boolean[]{false, true, true}); 
	
	/*odo.setPosition(new double[] {usPoller.distance+7, 0, 0}, new boolean[]{true, false, false}); 
		//the 7 compensates for hardware inaccuracies
	navigator.turnTo(270, true);
	odo.setPosition(new double[] {0, usPoller.distance+7, 0}, new boolean[]{false, true, false}); 
	
	navigator.turnTo(0, true);
    
	odo.setPosition(new double [] {0.0, 0.0, 45.0}, new boolean [] {true, true, true});*/
	
	//navigator.travelTo(30, 30);
	//navigator.turnTo(0, true);
	
	}
/**
 * gives the data being read from the sensor
 * @return the distance being read by the sensor
 */
	private float getFilteredData() {
		//usSensor.fetchSample(usData, 0);
		float distance = usData[0];

		return distance;
	}

}