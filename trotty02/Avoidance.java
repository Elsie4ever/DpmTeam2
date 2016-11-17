package trotty02;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Avoidance extends Thread{
	
	private boolean avoid = false;
	private UltrasonicPoller usPoller;
	
	private int bandCenter, bandwidth;
	private final int motorStraight = 250, FILTER_OUT = 20;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int distance;
	private int filterControl;
	private int p; //proportion constant
	
	public Avoidance(UltrasonicPoller usPoller, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			int bandCenter, int bandwidth){
		this.usPoller = usPoller;
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.filterControl = 2;
		this.p = 13;
	}
	
	public void run(){
		while(true){
			while(avoid){
				processUSData((int) usPoller.getDistFront(), true);
				processUSData((int) usPoller.getDistBack(), true);
				
			}
		}
	}
	
	public void setAvoid(boolean avoid){
		/**
		 * Tells the code whether to run the avoidance loop inside the tread or not
		 * @param avoid {boolean} true to run avoidance loop, false to exit the loop
		 */
		this.avoid = avoid;
	}
	
	public boolean getAvoid(){
		/**
		 * Return whether or not the avoidance loop is being run
		 * @return {boolean} true if running the avoidance loop, false if not
		 */
		return this.avoid;
	}
	
	public void processUSData(int distance, boolean usForward) {
		
		// Initialize motor rolling forward
		this.leftMotor.setSpeed(motorStraight);
		this.rightMotor.setSpeed(motorStraight);
		this.leftMotor.forward();
		this.rightMotor.forward();
		
		// rudimentary filter - toss out invalid samples corresponding to null signal.
		// (n.b. this was not included in the Bang-bang controller, but easily could have).
		//
		if (distance > bandwidth + 2*bandCenter && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the filter value
			filterControl++;
		} else if (distance > bandwidth + bandCenter){
			// true greater than range, therefore set distance to detected distance
			this.distance = distance;
		} else {
			// distance went below 255, therefore reset everything.
			filterControl = 0;
			this.distance = distance;
		}
		
		// TODO: process a movement based on the us distance passed in (P style)	
		//P style means that the speed/angle of turn is proportional to the distance away from the wall
		//the output of a proportional controller is the multiplication product of the error 
		//signal and the proportional gain
		
		//if US sensor is pointing right
		if(!usForward){
			if (distance > 150) // if the sensor doesn't pick up on any objects nearby
				distance = 150; // default the distance to 150 so we don't overcompensate with a higher value on the p controller
			
			if(distance > bandCenter+3 ) {//if it's a little too far to the right, turn in left.
				
				leftMotor.setSpeed(motorStraight+p*(Math.abs(distance-bandCenter-bandwidth))); //speeds up the right motor to turn left
			}
				
			

			if(distance <= bandCenter-3) //if its a little too close to the left, turn out right slightly
				leftMotor.setSpeed(motorStraight-p*(Math.abs(bandCenter+bandwidth-distance))); // slows the right motor to turn right
	
			if((distance < bandCenter+3)&&(distance > bandCenter-3)){ // if its +/- 3 from the bandCenter, just set the robot straight instead of using a p controller speed
				rightMotor.setSpeed(motorStraight); //applies normal straight speed
				leftMotor.setSpeed(motorStraight);
			}

			leftMotor.forward(); //apply the changes to the speeds
			rightMotor.forward(); //^
		}
		
		//when US sensor is pointing forward
		if (usForward){
			
			if(distance < 11){
				rightMotor.setSpeed(motorStraight);
				rightMotor.backward(); // sets speeds backwards  so the robot gets a little extra space to work with
				leftMotor.backward();
			}
			
			
			else if(distance < bandCenter) //if it's getting close to a wall, turn right a little
				rightMotor.setSpeed(motorStraight-4*p*(Math.abs((bandCenter-distance)))); // adjust the speed setting so it can hone in on the bandCenter
			if(distance >= 15){ // if it isn't going backwards, we go forwards
				leftMotor.forward(); // apply forward speed
				rightMotor.forward();
			}
		}
	}
}
