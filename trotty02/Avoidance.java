package trotty02;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
/**
 * 
 * @author Shi Yu
 *
 */
public class Avoidance extends Thread{
	
	private boolean avoid = false;
	private UltrasonicPoller usPollerF;
	private UltrasonicPoller usPollerS;

	private int bandCenter, track;
	private static final int EMERGENCY = 5;
	private static final int motorStraight = 150;
	private static final int motorTurn = 100;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private static final int P = 7; //proportion constant
	private int horizontal = bandCenter;
	private int vertical = bandCenter;
	private double prevHeading;
	
	/**
	 * Constructor for the avoidance class 
	 * @param usPoller Ultrasonic Poller
	 * @param leftMotor
	 * @param rightMotor
	 * @param bandCenter distance between the robot and the wall
	 * @param track width of the robot
	 */
	public Avoidance(UltrasonicPoller usPollerF, UltrasonicPoller usPollerS, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			int bandCenter, int track){
		this.usPollerF = usPollerF;
		this.usPollerS = usPollerS;

		//Default Constructor
		this.bandCenter = bandCenter;
		this.track = track;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
	}
	
	public void run(){
		while(true){
			while(avoid){
				processUSData(usPollerF.getDistance(), usPollerS.getDistance());
			}
		}
	}
	
	/**
	 * Tells the code whether to run the avoidance loop inside the tread or not
	 * @param avoid {boolean} true to run avoidance loop, false to exit the loop
	 */
	public void setAvoid(boolean avoid){
		this.avoid = avoid;
	}
	
	/**
	 * Return whether or not the avoidance loop is being run
	 * @return {boolean} true if running the avoidance loop, false if not
	 */
	public boolean getAvoid(){
		return this.avoid;
	}
	
	/**
	 * Processes the distance from both the front and the side sensors to allow
	 * wheel movements - avoid the block.
	 * @param distFront distance seen by the front sensor
	 * @param distSide distance seen by the side sensor
	 */
	public void processUSData(double distFront, double distSide){
		//TODO: Movement based on information from the front & the side
		
		horizontal = (int) distSide;
		vertical = (int) distFront;
		
		if(horizontal < vertical){
			if(horizontal < bandCenter){ // turn CCW
				leftMotor.setSpeed(motorStraight);
				rightMotor.setSpeed(motorStraight + P*Math.abs(horizontal - bandCenter));
				leftMotor.forward();
				rightMotor.forward();
			}
			else{ // turn CW
				leftMotor.setSpeed(motorStraight + P*Math.abs(horizontal - bandCenter));
				rightMotor.setSpeed(motorStraight);
				leftMotor.forward();
				rightMotor.forward();
			}
		}
		
		else{
			if(vertical < bandCenter){ // turn CCW
				leftMotor.setSpeed(motorStraight);
				rightMotor.setSpeed(motorStraight + P*Math.abs(vertical - bandCenter) + motorTurn);
				leftMotor.forward();
				rightMotor.forward();
			}
			else{ // turn CW
				leftMotor.setSpeed(motorStraight + motorTurn);
				rightMotor.setSpeed(motorStraight);
				leftMotor.forward();
				rightMotor.forward();
			}
		}
	}
}