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
 * Odometer defines cooridinate system as such...
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
/**
 * 
 * @author Adam, Ke, Jean-Christophe, Elsie, Shi Yu
 *
 */
public class Odometer implements TimerListener {

	private Timer timer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private final int DEFAULT_TIMEOUT_PERIOD = 20;
	private double leftRadius, rightRadius, width;
	private double x, y, theta;
	private double[] oldDH, dDH;
	private UltrasonicPoller usPoller;
	private LightPoller lsPoller;

	
	/**
	 * Constructor for the odometer
	 * @param leftMotor left wheel motor
	 * @param rightMotor right wheel motor
	 * @param INTERVAL interval of the polling
	 * @param autostart boolean value representing whether the robot starts or not
	 * @param usPoller ultrasonic sensor poller object
	 * @param lsPoller light sensor poller object
	 */
	public Odometer (EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, int INTERVAL, boolean autostart,
			UltrasonicPoller usPoller, LightPoller lsPoller) {
		this.lsPoller = lsPoller;
		this.usPoller = usPoller;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		
		// default values, modify for your robot
		this.rightRadius = 2.1;
		this.leftRadius = 2.1;
		this.width = 13.5;
		
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 90.0;
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
	 * functions to stop the timerlistener
	 */
	public void stop() {
		if (this.timer != null)
			this.timer.stop();
	}
	/**
	 * function to start the timerlistener
	 */
	public void start() {
		if (this.timer != null)
			this.timer.start();
	}
	
	/**
	 * Calculates displacement and heading as title suggests
	 * @param data the array of data read by the sensor
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
	 * Gets the X value 
	 * @return the current X value
	 */
	public double getX() {
		synchronized (this) {
			return x;
		}
	}

	/**
	 * Gets the Y value
	 * @return the current Y value
	 */
	public double getY() {
		synchronized (this) {
			return y;
		}
	}

	/**
	 * Gets the angle
	 * @return returns the current theta
	 */
	public double getAng() {
		synchronized (this) {
			return theta;
		}
	}

	/**
	 * Sets X, Y and theta
	 * @param position double array containing the values representing the position of the robot
	 * @param update boolean array that details which slots in the array should be updated
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

	/**
	 * Returns X, Y and theta
	 * @param position the position of the robot
	 */
	public void getPosition(double[] position) {
		synchronized (this) {
			position[0] = x;
			position[1] = y;
			position[2] = theta;
		}
	}
/**
 * Gets the current position
 * @return a double array of the position: X Y and theta values
 */
	public double[] getPosition() {
		synchronized (this) {
			return new double[] { x, y, theta };
		}
	}
	
	/**
	 * Accessing motors
	 * @return motor object array containing the left and right motors
	 */
	public EV3LargeRegulatedMotor [] getMotors() {
		return new EV3LargeRegulatedMotor[] {this.leftMotor, this.rightMotor};
	}
	/**
	 * Accessing the left motor
	 * @return motor object of the left motor
	 */
	public EV3LargeRegulatedMotor getLeftMotor() {
		return this.leftMotor;
	}
	/**
	 * Accessing the right motor
	 * @return motor object of the right motor
	 */
	public EV3LargeRegulatedMotor getRightMotor() {
		return this.rightMotor;
	}

	// static 'helper' methods
	/**
	 * Makes the angle fit under 360, wrapping around when necessary
	 * @param angle the angle that has been read
	 * @return the corrected angle
	 */
	public static double fixDegAngle(double angle) {
		if (angle < 0.0)
			angle = 360.0 + (angle % 360.0);

		return angle % 360.0;
	}
/**
 * Determine the smallest angle in order to turn from angle A to angle B
 * @param a starting angle value
 * @param b final angle value
 * @return the smallest angle from angle A to angle B in degrees
 */
	public static double minimumAngleFromTo(double a, double b) {
		double d = fixDegAngle(b - a);

		if (d < 180.0)
			return d;
		else
			return d - 360.0;
	}
/**
 * gets the current theta
 * @return theta value
 */
	public double getTheta() {
		return theta;
	}
	/**
	 * Gets the boolean response of whether the sensor detects something or not
	 * @return true if there is something detected, false otherwise
	 */
	public boolean seesSomething(){
		return this.usPoller.seesSomething();
	}
/**
 * Gets boolean response of whther the sensor detects a styrofoam block or not
 * @return true if the detected block in a styrofoam block, false otherwise
 */
	public boolean seesBlock() {
		return this.lsPoller.seesBlock();
	}

}
