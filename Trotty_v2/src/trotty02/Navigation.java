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
/**
 * 
 * @author Elsie, Adam, Shi Yu, Ke
 *
 */
public class Navigation {
	final static int FAST = 200, SLOW = 100, ACCELERATION = 444;
	final static double DEG_ERR = 1.0, CM_ERR = 1.0;
	final static double RADIUS = 2.1;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	
/**
 * Navigation method that contains all the functions telling the robot how to move around
 * @param odo Creates an odometer
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

	/**
	 * Functions to set the motor speeds jointly
	 * @param lSpd The left wheel speed
	 * @param rSpd The right wheel speed
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
	 * constantly updating it's heading. 
	 * Allows the robot to travel to a certain destination on the board with wheels rotating forwards
	 * @param x destination value on the x axis
	 * @param y destination value on the y axis
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

	/**
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 * Rotates the robot to the desired angle
	 * @param angle Angle of the turn
	 * @param stop Boolean that determines if the bot stops or not after the turn
	 */
	public void turnTo(double angle, boolean stop) {
		double error = angle - this.odometer.getAng();
		while (Math.abs(error) > DEG_ERR) {
			if(error > 360)
				error -=360;
			if(error < 0)
				error +=360;
			
			if (error < -180.0) {
				this.setSpeeds(-SLOW, SLOW);
			} else if (error < 0.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else if (error > 180.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else {
				this.setSpeeds(-SLOW, SLOW);
			}
			error = angle - this.odometer.getAng();
		}
	if (stop) {
		this.setSpeeds(0, 0);
	}
}
	
	/**
	 * Allows the robot to move in a straight line an amount specified by the distance argument
	 * @param distance Desired distance to travel in centimeters
	 */
public void goForward(double distance) {
	this.travelTo(odometer.getX() + Math.cos(Math.toRadians(this.odometer.getAng())) * distance, odometer.getY() + Math.sin(Math.toRadians(this.odometer.getAng())) * distance);
}

/**
 * Avoids an obstacle that is not a styrofoam block
 * @param usPoller object that takes data from the ultrasonic sensor
 */
public void avoid(UltrasonicPoller usPoller){
	double distance = usPoller.getDistance();
	
	this.travelTo(0,0); //backs up for space
	
	//avoids block and goes to center
	this.travelTo(60, 0);
	this.travelTo(30, 30);
	
}

/**
 * Triggers the claw to descend and capture a block
 * @param leftSMotor object representing the robot's left motor
 * @param rightSMotor object representing the robot's right motor
 */
public void capture(EV3LargeRegulatedMotor leftSMotor, EV3LargeRegulatedMotor rightSMotor) {
	leftSMotor.rotateTo(-200);	
	rightSMotor.rotate(-200);	
	//arm motors bring down the net

}
/**
 * Tells the robot to go to the final destination where the blocks must be brought.
 */
public void bringBlockHome() {
	this.travelTo(75, 75);
}

/**
 * Allows the robot to travel to a certain destination on the board with wheels rotating backwards
 * @param x Destination value on the x axis
 * @param y Destination value on the y axis
 */
public void travelToBackwards(double x, double y) {
	while((odometer.getX() > x + 2)||(odometer.getX() < x - 2)||(odometer.getY() > y + 2)||(odometer.getY() < y - 2)){
		this.setSpeeds(-150, -150);
	}
	this.setSpeeds(0, 0);
	
}
/**
 * Stops the robot's wheels, setting the motor speeds at 0. 
 */
public void stop(){
	this.setSpeeds(0, 0);
}

/**
 * Converting a desired distance into the actual distance as understood by the robot
 * @param radius 
 * @param distance
 * @return returns an int that represents the converted distance
 */
private static int convertDistance(double radius, double distance) {
	return (int) ((180.0 * distance) / (Math.PI * radius));
}



}
