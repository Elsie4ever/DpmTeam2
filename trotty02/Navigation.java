/*
 * File: Navigation.java
 * Written by: Sean Lawlor
 * ECSE 211 - Design Principles and Methods, Head TA
 * Fall 2011
 * Ported to EV3 by: Francois Ouellet Delorme
 * Fall 2015
 * 
 * Movement control class (turnTo, travelTo, flt, localize)
 */
package trotty02;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation {
	final static int FAST = 200, SLOW = 100, ACCELERATION = 444;
	final static double DEG_ERR = 1.0, CM_ERR = 1.0;
	final static double RADIUS = 2.1;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	

	public Navigation(Odometer odo) {
		this.odometer = odo;

		EV3LargeRegulatedMotor[] motors = this.odometer.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];

		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}

	/*
	 * Functions to set the motor speeds jointly
	 */
	public void setSpeeds(float lSpd, float rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	public void setSpeeds(int lSpd, int rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	/*
	 * Float the two motors jointly
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
	}

	/*
	 * TravelTo function which takes as arguments the x and y position in cm Will travel to designated position, while
	 * constantly updating it's heading
	 */
	public void travelTo(double x, double y) {
		this.leftMotor.setSpeed(SLOW);
		this.rightMotor.setSpeed(SLOW);
		double minAng;
		minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
			if (minAng < 0)
				minAng += 360.0;
		double distance = Math.sqrt((x-odometer.getX())*(x-odometer.getX()) + (y-odometer.getY())*(y-odometer.getY()));
		while (distance >= this.CM_ERR) {
			minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
			while (minAng < 0)
				minAng += 360.0;
			turnTo(minAng, true);
			
			this.leftMotor.forward();
			this.rightMotor.forward();
			this.leftMotor.setSpeed(SLOW);
			this.rightMotor.setSpeed(SLOW);
			leftMotor.rotate(convertDistance(RADIUS, distance), true);
			rightMotor.rotate(convertDistance(RADIUS, distance), false);
			distance = Math.sqrt((x-odometer.getX())*(x-odometer.getX()) + (y-odometer.getY())*(y-odometer.getY()));
		}
		this.setSpeeds(0, 0);
	}

	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 */
	public void turnTo(double angle, boolean stop) {

		if((angle < 90 && this.odometer.getAng() > 270) || (angle > 270 && this.odometer.getAng() < 90)){  /*avoids wraparound issues when the robot is */
			if(angle < 90){																																								   /*turning through 0 degrees*/
				angle+=360;
			}
			else{
				angle=360-angle;
			}
		}
		
		double error = angle - this.odometer.getAng();
		
		while (Math.abs(error) > DEG_ERR) {

			if((angle < 90 && this.odometer.getAng() > 270) || (angle > 270 && this.odometer.getAng() < 90)){
				if(angle < 90){
					angle+=360;
				}
				else{
					angle=360-angle;
				}
			}
			
			error = angle - this.odometer.getAng();

			if (error < -180.0) {
				this.setSpeeds(-SLOW, SLOW);
			} else if (error < 0.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else if (error > 180.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else {
				this.setSpeeds(-SLOW, SLOW);
			}
		}

		if (stop) {
			this.setSpeeds(0, 0);
		}
	}
	

/*
 * Go foward a set distance in cm
 */
public void goForward(double distance) {
	this.travelTo(odometer.getX() + Math.cos(Math.toRadians(this.odometer.getAng())) * distance, odometer.getY() + Math.sin(Math.toRadians(this.odometer.getAng())) * distance);
}

public void avoid(UltrasonicPoller usPoller){
	double distance = usPoller.getDistFront();

	this.travelTo(0,0);
	//backs up for space
	
//avoids block and goes to center
	this.travelTo(60, 0);
	
	this.travelTo(30, 30);
	
}


public void capture(EV3LargeRegulatedMotor leftSMotor, EV3LargeRegulatedMotor rightSMotor) {
	leftSMotor.rotateTo(-200);	
	rightSMotor.rotate(-200);	
	//arm motors bring down the net

}


public void bringBlockHome() {
	this.travelTo(75, 75);
}

public void travelToBackwards(double x, double y) {
	while((odometer.getX() > x + 2)||(odometer.getX() < x - 2)||(odometer.getY() > y + 2)||(odometer.getY() < y - 2)){
		this.setSpeeds(-150, -150);
	}
	this.setSpeeds(0, 0);
	
}

public void stop(){
	this.setSpeeds(0, 0);
}

private static int convertDistance(double radius, double distance) {
	return (int) ((180.0 * distance) / (Math.PI * radius));
}



}
