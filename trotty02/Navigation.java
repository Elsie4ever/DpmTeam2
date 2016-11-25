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

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * 
 * @author Adam
 *
 */
public class Navigation {
	double TILE_LENGTH = 30.48;
	final static int FAST = 250, SLOW = 100, ACCELERATION = 111;
	final static double DEG_ERR = 1.0, CM_ERR = 22;
	final static double RADIUS = 2.1;
	final static double TRACK = 12.7;
	int resolution;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	
	
/**
 * constructor for Navigation object
 * @param odo the odometer
 */
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
	
	/**
	 * sets the speeds of each wheel and has them go forward
	 * @param lSpd the speed of the left wheel
	 * @param rSpd the speed of the right wheel
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

	/**
	 * sets the speeds of each wheel and has them go forward
	 * @param lSpd the speed of the left wheel
	 * @param rSpd the speed of the right wheel
	 */
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

	/**
	 * Float the two motors jointly
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
	}

	
	/**
	 * TravelTo function which takes as arguments the x and y position in cm Will travel to designated position, while
	 * constantly updating it's heading
	 * 
	 * @param x the x coordinate destination
	 * @param y the y coordinate destination
	 * @param cutShort ???
	 */
	public void travelTo(double x, double y, boolean cutShort) {
		double minAng;
		minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
			if (minAng < 0)
				minAng += 360.0;
		double distance = Math.sqrt((x-odometer.getX())*(x-odometer.getX()) + (y-odometer.getY())*(y-odometer.getY()));
		if (cutShort) {
			if (distance>TILE_LENGTH/resolution) {
				distance = TILE_LENGTH/resolution;
			}
		}
		//while (distance >= this.CM_ERR) {
			minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
			while (minAng < 0)
				minAng += 360.0;
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				
			}
			turnTo(minAng, true);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				
			}
			this.leftMotor.setSpeed(FAST);
			this.rightMotor.setSpeed(FAST);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				
			}
			leftMotor.rotate(convertDistance(RADIUS, distance), true);
			rightMotor.rotate(convertDistance(RADIUS, distance), false);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				
			}
			distance = Math.sqrt((x-odometer.getX())*(x-odometer.getX()) + (y-odometer.getY())*(y-odometer.getY()));
		//}
	}

	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 */
	/**
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 * @param angle the angle to turn to 
	 * @param stop if true, the bot will stop after turning to the desired angle
	 */
	public void turnTo(double angle, boolean stop) {

		angle = angle - odometer.getTheta();
		while (angle < 0){
			angle = angle + 360;
		}
		while (angle > 360){
			angle = angle - 360;
		}
		leftMotor.setSpeed(SLOW);
		rightMotor.setSpeed(SLOW);
		if (angle <= 180){
			leftMotor.rotate(-convertAngle(RADIUS, TRACK, angle), true);
			rightMotor.rotate(convertAngle(RADIUS, TRACK, angle), false);
		} else {
			angle = 360 - angle;
			leftMotor.rotate(convertAngle(RADIUS, TRACK, angle), true);
			rightMotor.rotate(-convertAngle(RADIUS, TRACK, angle), false);
		}
	}
	

/**
 *
 * Go forward a set distance in cm
 *
 * @param distance the distance to travel forwards
 */
public void travelDistance(double distance) {
	this.travelTo(odometer.getX() + Math.cos(Math.toRadians(this.odometer.getAng())) * distance, odometer.getY() + Math.sin(Math.toRadians(this.odometer.getAng())) * distance, false);
}

/**
 * another method to go forwards without using travel to
 * @param distance the distance to go forwards
 */
public void goForward(double distance) {
	Sound.beep();
	this.leftMotor.setSpeed(SLOW);
	this.rightMotor.setSpeed(SLOW);
	try {
		Thread.sleep(50);
	} catch (InterruptedException e) {
		
	}
	leftMotor.rotate(convertDistance(RADIUS, distance), true);
	rightMotor.rotate(convertDistance(RADIUS, distance), false);
}

/**
 * has the bot go backwards
 * @param x the x destination
 * @param y the y destination
 */
public void travelToBackwards(double x, double y) {
	while((odometer.getX() > x + 2)||(odometer.getX() < x - 2)||(odometer.getY() > y + 2)||(odometer.getY() < y - 2)){
		this.setSpeeds(-150, -150);
	}
	this.setSpeeds(0, 0);
	
}

/**
 * stops the bot
 */
public void stop(){
	this.setSpeeds(0, 0);
}

/**
 * converts a distance to degree
 * @param radius the wheel radius
 * @param distance the distance to convert
 * @return the converted angle
 */
private static int convertDistance(double radius, double distance) {
	return (int) ((180.0 * distance) / (Math.PI * radius));
}

/**
 * converts an angle to a distance
 * @param radius the wheel radius
 * @param width the track length of the bot
 * @param angle the angle to convert to an arclength
 * @return the distance calculated from the angle
 */
private static int convertAngle(double radius, double width, double angle) {
	return convertDistance(radius, Math.PI * width * angle / 360.0);
}
/**
 * sets a resolution
 * @param resolution used to adjust the for loop to prevent skipping tiles
 */
public void setResolution(int resolution){
	this.resolution = resolution;
}
}