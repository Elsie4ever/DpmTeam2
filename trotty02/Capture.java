package trotty02;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
/**
 * 
 * @author Team 02
 *
 */
public class Capture {
	public EV3LargeRegulatedMotor armMotor;
	/**
	 * Constructor for the capture class
	 * @param armMotor the motor that controls the capturing arm
	 */
	Capture(EV3LargeRegulatedMotor armMotor){
		this.armMotor = armMotor;
	}
	/**
	 * Rotates the arm to trap the block when prompted
	 */
	public void CaptureObj(){
		armMotor.rotateTo(50);
		//TODO: determine actualy angle- 50 was a random number
	}

}