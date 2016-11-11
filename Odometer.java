/*
 * Odometer.java
 */

package trotty02;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
public class Odometer extends Thread {
	// robot position
	private double x, y, theta;

	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;

	// lock object for mutual exclusion
	//static final Object lock = new Object();
	private Object lock;
	
	// define the motors used
	
	// class variables needed
	public double WHEEL_RADIUS;
	public double TRACK;
	public static int lastTachoL;			// Tacho L at last sample
	public static int lastTachoR;			// Tacho R at last sample 
	double distL, distR, deltaD, deltaT, dX, dY;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int leftMotorTachoCount, rightMotorTachoCount; //	Current Tacho counts

	// default constructor
	public Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double wheelRadius, double width) {
		lock = new Object();
		
		setX( (double) 0);
		setY( (double) 0);
		setTheta( (double) 90); // Assuming starting towards positive Y axis, math angles
		
		// My own additions
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.WHEEL_RADIUS = wheelRadius;
		this.TRACK = width;
		
		// Reset the tacho values before each run, then re-initialize them
		leftMotor.resetTachoCount();
	    rightMotor.resetTachoCount();
	    setLeftMotorTachoCount(leftMotor.getTachoCount());
	    setRightMotorTachoCount(rightMotor.getTachoCount());
	    lastTachoL = getLeftMotorTachoCount();
		lastTachoR = getRightMotorTachoCount();
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;
	    
		while (true) {
			updateStart = System.currentTimeMillis();
			// put (some of) your odometer code here
			
			//	Calculate displacement
			setLeftMotorTachoCount(leftMotor.getTachoCount());							//	Get the current tacho counts of both wheels
		    setRightMotorTachoCount(rightMotor.getTachoCount());
			distL = Math.PI*WHEEL_RADIUS*(getLeftMotorTachoCount()-lastTachoL)/180;		// compute both wheel displacements
			distR = Math.PI*WHEEL_RADIUS*(getRightMotorTachoCount()-lastTachoR)/180;
			lastTachoL = getLeftMotorTachoCount();										// save tacho counts for next iteration
			lastTachoR = getRightMotorTachoCount();
			
			deltaD = (distL+distR)/2;							// compute vehicle displacement
			deltaT = (distL-distR)/TRACK;						// compute change in heading
			
			synchronized (lock) {
				// don't use the variables x, y, or theta anywhere but here!
				
				//setTheta((getTheta() + deltaT)%(2*Math.PI));
				
				if((getTheta() + deltaT) >= 0){						// update heading
					setTheta((getTheta() + deltaT)%(2*Math.PI));					// to ensure a positive theta as required
				}
				else{
					setTheta(((getTheta() + deltaT)%(2*Math.PI) + 2*Math.PI)%(2*Math.PI));
				}
				
			    dX = deltaD * Math.sin(getTheta());					// compute X component of displacement
				dY = deltaD * Math.cos(getTheta());					// compute Y component of displacement
				setX(getX() + dX);									// update estimates of X and Y position
				setY(getY() + dY);
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
		public void getPosition(double[] position, boolean[] update) {
			// ensure that the values don't change while the odometer is running
			synchronized (lock) {
				if (update[0])
					position[0] = x;
				if (update[1])
					position[1] = y;
				if (update[2])
					position[2] = theta;
			}
		}

		public double getX() {
			double result;

			synchronized (lock) {
				result = x;
			}

			return result;
		}

		public double getY() {
			double result;

			synchronized (lock) {
				result = y;
			}

			return result;
		}

		public double getTheta() {
			double result;

			synchronized (lock) {
				result = theta;
			}

			return result;
		}

		// mutators
		public void setPosition(double[] position, boolean[] update) {
			// ensure that the values don't change while the odometer is running
			synchronized (lock) {
				if (update[0])
					x = position[0];
				if (update[1])
					y = position[1];
				if (update[2])
					theta = position[2];
			}
		}

		public void setX(double x) {
			synchronized (lock) {
				this.x = x;
			}
		}

		public void setY(double y) {
			synchronized (lock) {
				this.y = y;
			}
		}

		public void setTheta(double theta) {
			synchronized (lock) {
				this.theta = theta;
			}
		}

		/**
		 * @return the leftMotorTachoCount
		 */
		public int getLeftMotorTachoCount() {
			return leftMotorTachoCount;
		}

		/**
		 * @param leftMotorTachoCount the leftMotorTachoCount to set
		 */
		public void setLeftMotorTachoCount(int leftMotorTachoCount) {
			synchronized (lock) {
				this.leftMotorTachoCount = leftMotorTachoCount;	
			}
		}

		/**
		 * @return the rightMotorTachoCount
		 */
		public int getRightMotorTachoCount() {
			return rightMotorTachoCount;
		}

		/**
		 * @param rightMotorTachoCount the rightMotorTachoCount to set
		 */
		public void setRightMotorTachoCount(int rightMotorTachoCount) {
			synchronized (lock) {
				this.rightMotorTachoCount = rightMotorTachoCount;	
			}
		}
}