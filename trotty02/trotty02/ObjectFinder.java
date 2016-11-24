package trotty02;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
/**
 * 
 * @author Jean-Christophe
 *
 */
public class ObjectFinder {
	double TILE_LENGTH = 30.48;
	int MAX_AVOIDANCE_COUNTER = 6666;
	int AVOID_SPEED = 100;
	int BAND_CENTER = 13;
	int BAND_WIDTH = 3;
	double seekObjectDistance = TILE_LENGTH;
	double captureDistance = 11;
	int avoidanceCounter = 0;
	boolean avoidMode = false;
	int resolution;
	double[] endCoords = {TILE_LENGTH,TILE_LENGTH*5}; 
	/*
	double[][] waypoints = new double[][]{
			//  {0, TILE_LENGTH/2, 0},
			  {0, TILE_LENGTH, 0},
			//  {0, 1.5*TILE_LENGTH, 0},
			  {0, 2*TILE_LENGTH, 1},
		//	  {TILE_LENGTH/2, 2*TILE_LENGTH, 0},
			  {TILE_LENGTH, 2*TILE_LENGTH, 0},
		//	  {1.5*TILE_LENGTH, 2*TILE_LENGTH, 0},
			  {2*TILE_LENGTH, 2*TILE_LENGTH, 1},
		//	  {2*TILE_LENGTH, 1.5*TILE_LENGTH, 0},
			  {2*TILE_LENGTH, TILE_LENGTH, 0},
		//	  {2*TILE_LENGTH, TILE_LENGTH/2, 0},
			  {2*TILE_LENGTH, 0, 1},
		//	  {1.5*TILE_LENGTH, 0, 0},
			  {TILE_LENGTH, 0, 0},
		//	  {TILE_LENGTH, TILE_LENGTH/2, 0},
			  {TILE_LENGTH, TILE_LENGTH, 0}
			}; 
			*/
	double[][] waypoints = new double[][]{ //{x, y, check top? 0=true 1=false}
			  {1*TILE_LENGTH, 1*TILE_LENGTH, 0},
			  {1*TILE_LENGTH, 2*TILE_LENGTH, 0},
			  {1*TILE_LENGTH, 3*TILE_LENGTH, 0},
			  {1*TILE_LENGTH, 4*TILE_LENGTH, 0},
			  {1*TILE_LENGTH, 5*TILE_LENGTH, 0},
			  {2*TILE_LENGTH, 5*TILE_LENGTH, 0},
			  {3*TILE_LENGTH, 5*TILE_LENGTH, 0},
			  {4*TILE_LENGTH, 5*TILE_LENGTH, 0},
			  {5*TILE_LENGTH, 5*TILE_LENGTH, 0},
			  {5*TILE_LENGTH, 4*TILE_LENGTH, 0},
			  {5*TILE_LENGTH, 3*TILE_LENGTH, 0},
			  {5*TILE_LENGTH, 2*TILE_LENGTH, 0},
			  {5*TILE_LENGTH, 1*TILE_LENGTH, 0},
			  {4*TILE_LENGTH, 1*TILE_LENGTH, 0},
			  {3*TILE_LENGTH, 1*TILE_LENGTH, 0},
			  {2*TILE_LENGTH, 1*TILE_LENGTH, 0},
			  {2*TILE_LENGTH, 2*TILE_LENGTH, 0},
			  {2*TILE_LENGTH, 3*TILE_LENGTH, 0},
			  {2*TILE_LENGTH, 4*TILE_LENGTH, 0},
			  {3*TILE_LENGTH, 4*TILE_LENGTH, 0},
			  {4*TILE_LENGTH, 4*TILE_LENGTH, 0},
			  {4*TILE_LENGTH, 3*TILE_LENGTH, 0},
			  {4*TILE_LENGTH, 2*TILE_LENGTH, 0},
			  {3*TILE_LENGTH, 2*TILE_LENGTH, 0},
			  {3*TILE_LENGTH, 3*TILE_LENGTH, 0}
			}; 
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private Navigation navigator;
	//private Capture capture;
	private LightLocalizer lightLocalizer;
	private UltrasonicPoller USpollerF;
	private UltrasonicPoller USpollerS;
	private USLocalizer usLocalizer;
	private double radius;
	private double width;
	private Capture capture;
	private LightPoller lightPoller;

	/**
	 * Searching the board for an object
	 * @param leftMotor	the robot's left wheel motor
	 * @param rightMotor the robot's right wheel motor
	 * @param navigator navigator object
	 * @param odometer odometer object
	 * @param USpoller ultrasonic sensor poller object
	 * @param usLocalizer ultrasonic sensor localizer
	 * @param radius radius of the wheels
	 * @param width track length, distance between the two wheels
	 */
	public ObjectFinder(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Navigation navigator, 
			LightLocalizer lightLocalizer,
			Odometer odometer,
			UltrasonicPoller USpollerF, UltrasonicPoller USpollerS, USLocalizer usLocalizer, LightPoller lightPoller, int resolution) {

		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.navigator = navigator;

		//this.capture = capture;
		this.lightLocalizer = lightLocalizer;
		this.odometer = odometer;
		this.USpollerF = USpollerF;
		this.USpollerS = USpollerS;
		this.usLocalizer = usLocalizer;
		this.radius = radius;
		this.width = width;
		this.lightPoller = lightPoller;
		this.resolution = resolution;
		this.navigator.setResolution(this.resolution);
	}
	/**
	 * Drives the robot in a square
	 */

	
	public void pointDriver(){
		int i;
		int j;
		double sideAngle;
		while (true) {
		for (i = 0; i < waypoints.length; i++) {
			for (j = 0; j < resolution; j++) {
				if (!avoidMode) {

					navigator.travelTo(waypoints[i][0], waypoints[i][1], true);
					try { Thread.sleep(20);  } catch(Exception e){}	
					double frontDist = USpollerF.getDistance();
					double sideDist = USpollerS.getDistance();
					if (frontDist < seekObjectDistance && waypoints[i][2] == 0) {
						if (checkObject(frontDist, 0)){
							goToEnd();
							return;
						} else {
							sideAngle = odometer.getTheta() + 90;
							while (sideAngle >= 360) {
								sideAngle -= 360;
							}
							navigator.turnTo(sideAngle, true);	
							avoidMode = true;
							avoidanceCounter = 0;
						}
					} else if (sideDist < seekObjectDistance) {
						if (checkObject(sideDist, 1)){
							goToEnd();
							return;
						}
					}
				} else {
					bangbang(USpollerS.getDistance());
					j = resolution; //prevents skipping tiles
					avoidanceCounter++;
					if (avoidanceCounter >= MAX_AVOIDANCE_COUNTER) {
						avoidMode = false;
					}
				}
			}
		}
	}
	}
	
	boolean checkObject(double objectDistance, int sensor) { //sensor 0=front, 1=side
		double travelDistance;
		double sideAngle;
		if (objectDistance > captureDistance ) {
			travelDistance = objectDistance - captureDistance;
		} else {
			travelDistance = 0;
		}
		if (sensor == 1) {
			sideAngle = odometer.getTheta() - 90;
			while (sideAngle < 0) {
				sideAngle += 360;
			}
			navigator.turnTo(sideAngle, true);	
		}
		if (travelDistance > 0) {
			try { Thread.sleep(20);  } catch(Exception e){}	
			navigator.goForward(travelDistance);
	
		}
		try { Thread.sleep(100);  } catch(Exception e){}	
		if (lightPoller.isFoamBlock()) {
			double oppAngle = odometer.getTheta() - 180;
			while (oppAngle < 0) {
				oppAngle += 360;
			}
			try { Thread.sleep(20);  } catch(Exception e){}
			navigator.turnTo(oppAngle, true);
			try { Thread.sleep(20);  } catch(Exception e){}
			capture.CaptureObj();
			return true;
		} else {
			return false;
		}
	}
	
	void bangbang (double distance) {
		try { Thread.sleep(20);  } catch(Exception e){}
		if(distance >= BAND_CENTER+3 ) {//if it's a little too far to the right, turn in left.
			rightMotor.setSpeed(AVOID_SPEED);
			leftMotor.setSpeed(AVOID_SPEED*2); //speeds up the right motor to turn left
			
			leftMotor.forward(); //apply the changes to the speeds
			rightMotor.forward();
		} else { // if its +/- 3 from the bandCenter, just set the robot straight instead of using a p controller speed
			rightMotor.setSpeed(AVOID_SPEED); //applies normal straight speed
			leftMotor.setSpeed(AVOID_SPEED);
			leftMotor.forward(); //apply the changes to the speeds
			rightMotor.forward();
		}
	}
	
	void goToEnd () {
		double sideAngle;
		while (true) {
			navigator.travelTo(endCoords[0],endCoords[1], true);
			int i;
			if (USpollerF.getDistance() < 12) {
				sideAngle = odometer.getTheta() - 90;
				while (sideAngle < 0) {
					sideAngle += 360;
				}
				navigator.turnTo(sideAngle, true);	
				for (i = 0; i < MAX_AVOIDANCE_COUNTER; i++) {
					bangbang(USpollerF.getDistance());
				}
			}
		}
	}
}
