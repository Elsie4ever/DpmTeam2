package trotty02;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
/**
 * 
 * @author Team 02
 *
 */
public class Capture {
	public EV3LargeRegulatedMotor armMotor, leftMotor, rightMotor;
	/**
	 * Constructor for the capture class
	 * @param armMotor the motor that controls the capturing arm
	 */
	Capture(EV3LargeRegulatedMotor armMotor, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor){
		this.armMotor = armMotor;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
	}
	/**
	 * Rotates the arm to trap the block when prompted
	 */
	public void CaptureObj(){
		armMotor.setSpeed(77);
		armMotor.rotate(120, false);
		//TODO: determine actualy angle- 50 was a random number
	}

}