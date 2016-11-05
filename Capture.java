package trotty02;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Capture {
	public EV3LargeRegulatedMotor armMotor;

	Capture(EV3LargeRegulatedMotor armMotor){
		this.armMotor = armMotor;
	}
	public void CaptureObj(){
		armMotor.rotateTo(50);
		//TODO: determine actualy angle- 50 was a random number
	}

}
