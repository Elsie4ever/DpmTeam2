/*Write a program to travel to the waypoints (60, 30), (30, 30), (30, 60), and (60, 0) in that order.*/

package trotty02;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation extends Thread {
	private int FORWARD_SPEED;
	private int ROTATE_SPEED;
	private double wheelRadius, width, bandCenter;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private Object lock = new Object();
	private Odometer odometer;
	private final int ERROR = 3;
	private boolean avoid;
	private boolean lsl;

	public Navigation(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			double wheelRadius, double width, double bandCenter, Odometer odometer, boolean avoid, boolean lsl) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.wheelRadius = wheelRadius;
		this.width = width;
		this.bandCenter = bandCenter;
		this.odometer = odometer;
		this.leftMotor.setAcceleration(400);
		this.rightMotor.setAcceleration(400);
		this.avoid = avoid;
		this.lsl = lsl;
		if(this.lsl){
			this.FORWARD_SPEED = 250;
			this.ROTATE_SPEED = 50;
		}
		else{
			this.FORWARD_SPEED = 100;
			this.ROTATE_SPEED = 25;
		}
	}
	
	public void run(){
		forward(60.96);
	}
	
	public void travelTo(double x, double y){
		/**This method causes the robot to travel to the absolute field location (x, y).
		This method should continuously call turnTo(double theta) and then
		set the motor speed to forward(straight). This will make sure that your
		heading is updated until you reach your exact goal. (This method will poll
		the odometer for information)*/
		
		synchronized(lock){
			double heading = calcHeadingMath(x, y);
			//use the coordinate system of the odometer
			turnTo(heading);	
			
			while (!(odometer.getX() < x + 3 && odometer.getX() > x - ERROR && odometer.getY() < y + 3 && odometer.getY() > y - ERROR)) {
				leftMotor.setSpeed(FORWARD_SPEED);
				rightMotor.setSpeed(FORWARD_SPEED);
				leftMotor.forward();
				rightMotor.forward();
				
				//continuously checks the angle and corrects while moving forward if needed
				heading = calcHeadingMath(x, y);
				if(!angleWithinBounds(odometer.getTheta()*180/Math.PI, heading + ERROR, heading - ERROR)){
					turnTo(heading);   //continuously keeps on towards heading if off
				}
			}
			rightMotor.stop(true);
			leftMotor.stop();
		}
	}
	
	public void travelToAvoid(double x, double y){
		/**This method causes the robot to travel to the absolute field location (x, y).
		This method should continuously call turnTo(double theta) and then
		set the motor speed to forward(straight). This will make sure that your
		heading is updated until you reach your exact goal. (This method will poll
		the odometer for information)*/
		
		synchronized(lock){
			double heading = calcHeadingMath(x, y);
			//use the coordinate system of the odometer
			turnTo(heading);	
			
			while (!(odometer.getX() < x + 3 && odometer.getX() > x - ERROR && odometer.getY() < y + 3 && odometer.getY() > y - ERROR)) {
				leftMotor.setSpeed(FORWARD_SPEED);
				rightMotor.setSpeed(FORWARD_SPEED);
				leftMotor.forward();
				rightMotor.forward();
				
				//Avoid obstacles?
				
				//continuously checks the angle and corrects while moving forward if needed
				heading = calcHeadingMath(x, y);
				if(!angleWithinBounds(odometer.getTheta()*180/Math.PI, heading + ERROR, heading - ERROR)){
					turnTo(heading);   //continuously keeps on towards heading if off
				}
			}
			rightMotor.stop(true);
			leftMotor.stop();
		}
	}
	
	public void turnTo(double boardHeading){
		/**This method causes the robot to turn (on point) to the absolute heading
		theta. This method should turn a MINIMAL angle to it's target.*/
		synchronized(lock){
			double deltaTheta = boardHeading - (odometer.getTheta()*180/Math.PI); // Makes sure that the angle is [0, 360]
			deltaTheta = (deltaTheta%360 + 360)%360;
			
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);
			
			//Turns the robot
			if (deltaTheta <= 180) {
				leftMotor.rotate((int)convertAngle(wheelRadius, width, deltaTheta), true);
				rightMotor.rotate((int)(-convertAngle(wheelRadius, width, deltaTheta)), false);
			}
			else {
				leftMotor.rotate((int)(-convertAngle(wheelRadius, width, 360-deltaTheta)), true);
				rightMotor.rotate((int)(convertAngle(wheelRadius, width, 360-deltaTheta)), false);
			}
		}	
	}
	
	public void turnCW(){
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.backward();
		rightMotor.forward();
	}
	
	public void turnCCW(){
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		rightMotor.backward();
		leftMotor.forward();
	}
	
	public void forward(){
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		rightMotor.backward();
		leftMotor.backward();
	}
	
	public void forward(double dist){
		leftMotor.rotate(-convertDistance(wheelRadius, dist), true);
		rightMotor.rotate(-convertDistance(wheelRadius, dist), false);
		
		/*double curX = odometer.getX();
		double curY = odometer.getY();
		double deltaDist = 0;
		
		while(deltaDist < dist){
			leftMotor.backward();
			rightMotor.backward();
			deltaDist = Math.sqrt(Math.pow(odometer.getX() - curX, 2) + Math.pow(odometer.getY() - curY, 2));
		}*/
	}
	
	public void backward(){
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		rightMotor.forward();
		leftMotor.forward();
	}
	
	public void stopMov(){
		leftMotor.stop(true);
		rightMotor.stop();
	}
	
	public boolean isNavigating(){
		/*This method returns true if another thread has called travelTo() or
		turnTo() and the method has yet to return; false otherwise.*/
		
		return !Thread.holdsLock(lock); // Method returns TRUE if this thread is using the lock, thus invert it.
	}
	
	public boolean isTurning(){
		return !(leftMotor.isStalled() && rightMotor.isStalled());
	}
	
	//some wheel calculations
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	//navigation standard conversion
	public static double mathToCompass(double mathAngle){
		/**
		 * Was written because former odometer was in compass mode.
		 * Now that the compass is in math mode, we don't need this anymore.
		 */
		return ((450-mathAngle)%360);
	}
	
	public double calcHeadingMath(double x, double y){
		double xdir = x - odometer.getX();
		double ydir = y - odometer.getY();
		double ratio = Math.abs(ydir/xdir);
		
		if(ratio < 0.01){
			ratio = 0;
		}
		
		double heading = Math.toDegrees(Math.atan(ratio)); //gets the angle absolute value
		
		//double heading = Math.toDegrees(Math.atan(Math.abs(ydir/xdir))); //gets the angle absolute value
		
		//these place it in the correct quadrant
		if (xdir > 0 && ydir < 0) {
			heading = -heading;
		} else if (xdir < 0 && ydir >0) {
			heading = 180 - heading;
		} else if (xdir < 0 && ydir < 0) {
			heading = 180 + heading;
		}
		
		return heading;
	}
	
	public boolean angleWithinBounds(double angle, double upperBound, double lowerBound){
		boolean toReturn = false;
		if(angle < upperBound && angle > lowerBound){ // standard case
			toReturn = true;
		}
		else if(angle <= 3 || angle >= 357){ // if angle is near zero, need tweaking due to wrapping
			if((angle < upperBound || angle > 360 - upperBound) && (angle > lowerBound || angle < 360 + lowerBound)){
				toReturn = true;
			}
		}
		
		return toReturn;
	}
	
	public void travelToSquare(int x, int y){
		int correctX = (int) Math.signum(x-odometer.getX());
		int correctY = (int) Math.signum(y-odometer.getY());

		this.travelToXSquare(x, correctX);
		this.travelToYSquare(y, correctY);
			

		}
	
	private void travelToXSquare(int x, int correctX) {
		if(correctX == 1){
			this.turnTo(0);
			//TODO: implement any adjustments to the odometer and actual turns here
		}
		if(correctX == -1){
			this.turnTo(180);
		}
		
		while (Math.abs(x - odometer.getX()) > 3 ) {
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		}
		
	}
	
	private void travelToYSquare(int y, int correctY) {
		if(correctY == 1){
			this.turnTo(90);
			return;
		}
		if(correctY == -1){
			this.turnTo(270);
			return;
		}
		
		while (Math.abs(y - odometer.getY()) > 3 ) {
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		}
	}
	
}