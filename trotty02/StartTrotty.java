package trotty02;

import trotty02.LightLocalizer;
import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;
import trotty02.UltrasonicPoller;

public class StartTrotty {

	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	// Ultrasonic sensor port connected to input S1
	// Color sensor port connected to input S2
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3LargeRegulatedMotor clawMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static final EV3LargeRegulatedMotor lightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));

	private static final Port usPortFront = LocalEV3.get().getPort("S2");
	private static final Port usPortSide = LocalEV3.get().getPort("S3");
	private static final Port usPortBack = LocalEV3.get().getPort("S4");
	private static final EV3ColorSensor cS = new EV3ColorSensor(LocalEV3.get().getPort("S1"));
	final static double sensorToAxis = 10.5;
	static boolean captured = false;


	public static void main(String[] args) {
		int buttonChoice;
		//final Odometer odo = new Odometer(leftMotor, rightMotor, 30, true, usPoller);


		final TextLCD t = LocalEV3.get().getTextLCD();
		Sound.setVolume(50);		



		//Setup ultrasonic sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
		SensorModes usSensorFront = new EV3UltrasonicSensor(usPortFront);
		SampleProvider usValueFront = usSensorFront.getMode("Distance");			// colorValue provides samples from this instance
		float[] usDataFront = new float[usValueFront.sampleSize()];				// colorData is the buffer in which data are returned
		
		@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
		SensorModes usSensorSide = new EV3UltrasonicSensor(usPortSide);
		SampleProvider usValueSide = usSensorSide.getMode("Distance");			// colorValue provides samples from this instance
		float[] usDataSide = new float[usValueSide.sampleSize()];				// colorData is the buffer in which data are returned
		
		@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
		SensorModes usSensorBack = new EV3UltrasonicSensor(usPortBack);
		SampleProvider usValueBack = usSensorBack.getMode("Distance");			// colorValue provides samples from this instance
		float[] usDataBack = new float[usValueBack.sampleSize()];				// colorData is the buffer in which data are returned

		//Setup color sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data

		//SensorModes colorSensor = new EV3ColorSensor(csPort);
		//SampleProvider colorSensor = colorSensor.getMode("ColorID");			// colorValue provides samples from this instance
		//float[] sample = new float[colorValue.sampleSize()];			// colorData is the buffer in which data are returned
		final SampleProvider colorSensor = cS.getRGBMode();
		int sampleSize = colorSensor.sampleSize();   
		final float[] sample = new float[sampleSize];

		// setup the odometer and display
		final LightPoller lsPoller = new LightPoller(colorSensor, sample);
		final UltrasonicPoller usPoller = new UltrasonicPoller(usValueFront, usDataFront,
				usValueSide, usDataSide, usValueSide, usDataSide);	// the selected controller on each cycle
		final Odometer odo = new Odometer(leftMotor, rightMotor, 30, true, usPoller, lsPoller);
		final Navigation navigator = new Navigation(odo);

		do {
			// clear the display
			t.clear();

			// tell the user to press right when ready to start
			t.drawString("< Left | Right >", 0, 0);
			t.drawString("       |        ", 0, 1);
			t.drawString(" local | turn   ", 0, 2);//right uses falling edge
			t.drawString("       |        ", 0, 4);
			t.drawString("   up  |forward ", 0, 5);
			t.drawString("  60   |        ", 0, 6);


			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT
				&& buttonChoice != Button.ID_UP);

		if (buttonChoice == Button.ID_LEFT) {
			odo.start();

			usPoller.start();
			lsPoller.start();
			LCDInfo lcd = new LCDInfo(odo);



			(new Thread() {
				public void run() {
					USLocalizer usl = new USLocalizer(odo, usPoller, USLocalizer.LocalizationType.RISING_EDGE, navigator);
					usl.doLocalization();
					
					Button.waitForAnyPress();
					// perform the light sensor localization
					LightLocalizer lsl = new LightLocalizer(odo,colorSensor,sample);
					lsl.doLocalization();	

					

	
				}	
			}).start();
			while (Button.waitForAnyPress() != Button.ID_ESCAPE);
			System.exit(0);	

		} else if (buttonChoice == Button.ID_RIGHT) {
			odo.start();
			//LightLocalizer ll = new LightLocalizer(odo, navigator, 5.0);
			USLocalizer usl = new USLocalizer(odo, usPoller, USLocalizer.LocalizationType.RISING_EDGE, navigator);
			ObjectFinder of = new ObjectFinder(leftMotor, rightMotor, navigator,
					odo, usPoller, usl, 2.1, 15.0);
			of.squareDriver();
			while (Button.waitForAnyPress() != Button.ID_ESCAPE);
			System.exit(0);	
		}
}

}