/*
 * File: Odometer.java
 * Written by: Sean Lawlor
 * ECSE 211 - Design Principles and Methods, Head TA
 * Fall 2011
 * Ported to EV3 by: Francois Ouellet Delorme
 * Fall 2015
 * 
 * Class which controls the odometer for the robot
 * 
 * Odometer defines coordinate system as such...
 * 
 * 					90Deg:pos y-axis
 * 							|
 * 							|
 * 							|
 * 							|
 * 180Deg:neg x-axis------------------0Deg:pos x-axis
 * 							|
 * 							|
 * 							|
 * 							|
 * 					270Deg:neg y-axis
 * 
 * The odometer is initalized to 90 degrees, assuming the robot is facing up the positive y-axis
 * 
 */
package trotty02;

import lejos.utility.Timer;
import lejos.utility.TimerListener;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer implements TimerListener {

	private Timer timer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private final int DEFAULT_TIMEOUT_PERIOD = 20;
	private double leftRadius, rightRadius, width;
	private double x, y, theta;
	private double[] oldDH, dDH;
	private UltrasonicPoller usPoller;
	private LightPoller lsPoller;

	
	// constructor
	/**
	 * Constructor for the odometer class
	 * @param leftMotor the left motor of the robot
	 * @param rightMotor the right motor of the robot
	 * @param INTERVAL the length at which it parses the information read
	 * @param autostart boolean value of whether the odometer should start on its own
	 * @param usPoller an ultrasonic poller
	 * @param lsPoller a light poller
	 */
	public Odometer (EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, int INTERVAL, boolean autostart,
			UltrasonicPoller usPoller, LightPoller lsPoller) {
		this.lsPoller = lsPoller;
		this.usPoller = usPoller;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		
		
		this.rightRadius = 2.1;
		this.leftRadius = 2.1;
		this.width = 13.5;
		
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 90;
		this.oldDH = new double[2];
		this.dDH = new double[2];

		if (autostart) {
			// if the timeout interval is given as <= 0, default to 20ms timeout 
			this.timer = new Timer((INTERVAL <= 0) ? INTERVAL : DEFAULT_TIMEOUT_PERIOD, this);
			this.timer.start();
		} else
			this.timer = null;
	}
	
	/**
	 * functions to start/stop the timerlistener
	 */
	public void stop() {
		if (this.timer != null)
			this.timer.stop();
	}
	public void start() {
		if (this.timer != null)
			this.timer.start();
	}
	
	/**
	 * Calculates displacement and heading as title suggests
	 * 
	 * @param data the array containing information of the distance forwards and the current heading
	 */
	private void getDisplacementAndHeading(double[] data) {
		int leftTacho, rightTacho;
		leftTacho = leftMotor.getTachoCount();
		rightTacho = rightMotor.getTachoCount();

		data[0] = (leftTacho * leftRadius + rightTacho * rightRadius) * Math.PI / 360.0;
		data[1] = (rightTacho * rightRadius - leftTacho * leftRadius) / width;
	}
	
	/**
	 * Recompute the odometer values using the displacement and heading changes
	 */
	public void timedOut() {
		this.getDisplacementAndHeading(dDH);
		dDH[0] -= oldDH[0];
		dDH[1] -= oldDH[1];

		// update the position in a critical region
		synchronized (this) {
			theta += dDH[1];
			theta = fixDegAngle(theta);

			x += dDH[0] * Math.cos(Math.toRadians(theta));
			y += dDH[0] * Math.sin(Math.toRadians(theta));
		}

		oldDH[0] += dDH[0];
		oldDH[1] += dDH[1];
	}

	/**
	 * return the X value
	 * @return the x value determined by the odometer
	 */
	public double getX() {
		synchronized (this) {
			return x;
		}
	}

	/**
	 * return the Y value
	 * @return the y value determined by the odometer
	 */
	public double getY() {
		synchronized (this) {
			return y;
		}
	}

	/**
	 * return the theta value
	 * @return the theta value determined by the odometer
	 */
	public double getAng() {
		synchronized (this) {
			return theta;
		}
	}

	/**
	 * set the X, Y and theta values
	 * @param position an array that contains the X value, the Y value and the theta
	 * @param update boolean array that states whether each corresponding value should be updated
	 */
	public void setPosition(double[] position, boolean[] update) {
		synchronized (this) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	// return x,y,theta
	/**
	 * returns nothing, stores X, Y and theta data into input array
	 * @param position the array in which X, Y and theta are stored
	 */
	public void getPosition(double[] position) {
		synchronized (this) {
			position[0] = x;
			position[1] = y;
			position[2] = theta;
		}
	}
	/**
	 * returns a double array containing X, Y and theta
	 * @return the x, y and theta values
	 */
	public double[] getPosition() {
		synchronized (this) {
			return new double[] { x, y, theta };
		}
	}
	
	// accessors to motors
	/**
	 * gives the wheel motors
	 * @return the left and right motors
	 */
	public EV3LargeRegulatedMotor [] getMotors() {
		return new EV3LargeRegulatedMotor[] {this.leftMotor, this.rightMotor};
	}
	/**
	 * gives just the left motor
	 * @return just the left motor
	 */
	public EV3LargeRegulatedMotor getLeftMotor() {
		return this.leftMotor;
	}
	/**
	 * gives just the left motor
	 * @return just the left motor
	 */
	public EV3LargeRegulatedMotor getRightMotor() {
		return this.rightMotor;
	}

	// static 'helper' methods
	/**
	 * gives the principle angle
	 * @param angle if greater than 360 or less than 0, it returns an equivalant angle between 0 and 360
	 * @return the corrected angle
	 */
	public static double fixDegAngle(double angle) {
		if (angle < 0.0)
			angle = 360.0 + (angle % 360.0);

		return angle % 360.0;
	}
/**
 * gives the principle angle between two other angles
 * @param a the starting angle
 * @param b the ending angle
 * @return the minimum angle between b and a
 */
	public static double minimumAngleFromTo(double a, double b) {
		double d = fixDegAngle(b - a);

		if (d < 180.0)
			return d;
		else
			return d - 360.0;
	}
/**
 * gives the current heading
 * @return returns the heading of the bot
 */
	public double getTheta() {
		return theta;
	}
	/**
	 * says if it sees an object on the front usSensor
	 * @return does the bot see something
	 */
	public boolean seesSomething(){
		return this.usPoller.seesSomething();
	}

	/**
	 * says if it sees a blue block on the light sensor
	 * @return true if there is a blue block, otherwise false
	 */
	public boolean seesBlock() {
		return this.lsPoller.seesBlock();
	}

}