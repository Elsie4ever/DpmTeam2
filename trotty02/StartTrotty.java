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
/**
 * 
 * @author Team 02
 * 
 *
 */
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
	
	
	private static final int bandCenter = 20;			// Offset from the wall (cm)
	private static final int bandWidth = 12;
	private static final int resolution = 3;// Width of dead band (cm)
	

	/**
	 * The main that controls the actions of the robot
	 * @param args command arguments
	 */
	public static void main(String[] args) {
		int buttonChoice;
		final WifiTest wifi = new WifiTest();
		wifi.run();
		wifi.getRole();
		int corner = wifi.getCorner();
		
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
		final UltrasonicPoller usPollerF = new UltrasonicPoller(usValueFront, usDataFront);	// the selected controller on each cycle
		final UltrasonicPoller usPollerS = new UltrasonicPoller(usValueSide, usDataSide);	// the selected controller on each cycle

		final Odometer odo = new Odometer(leftMotor, rightMotor, 30, true, usPollerF, lsPoller);
		final Navigation navigator = new Navigation(odo);

		
		do {
			// clear the display
			t.clear();

			// tell the user to press right when ready to start
			t.drawString("< Left : LocalUS + localLight >", 0, 0);
			t.drawString(" UP: LocalUS + ObjectFinder ", 0, 1);
			t.drawString(" Down : ObjectFinder  ", 0, 2);
			t.drawString("       |        ", 0, 4);
			t.drawString("       |        ", 0, 5);
			t.drawString("       |        ", 0, 6);


			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_ENTER);

		if (buttonChoice == Button.ID_LEFT) {
			odo.start();
			usPollerF.start();
			usPollerS.start();
			lsPoller.start();
			LCDInfo lcd = new LCDInfo(odo, usPollerF, usPollerS, lsPoller);
			(new Thread() {
				public void run() {
					USLocalizer usl = new USLocalizer(odo, usPollerF, USLocalizer.LocalizationType.FALLING_EDGE, navigator, wifi.getCorner());
					usl.doLocalization();
					
					Button.waitForAnyPress();
					// perform the light sensor localization
				
					LightLocalizer lsl = new LightLocalizer(odo,colorSensor,sample);
					lsl.doLocalization();
				}	
			}).start();
		} else if (buttonChoice == Button.ID_ENTER) {
			odo.start();
			usPollerF.start();
			

			lsPoller.start();

			
			USLocalizer usl = new USLocalizer(odo, usPollerF, USLocalizer.LocalizationType.FALLING_EDGE, navigator, wifi.getCorner());
			LightLocalizer lsl = new LightLocalizer(odo,colorSensor,sample);

			usl.doLocalization();
			lsl.doLocalization();

			Button.waitForAnyPress();
			usPollerS.start();

			lightMotor.rotate(85);
			ObjectFinder of = new ObjectFinder(leftMotor, rightMotor, navigator, lsl, odo,
					usPollerF, usPollerS, usl, lsPoller, resolution, wifi.getxHalf(), wifi.getyHalf());
			of.pointDriver();
		} else if (buttonChoice == Button.ID_DOWN) {
			LightLocalizer lsl = new LightLocalizer(odo,colorSensor,sample);
			USLocalizer usl = new USLocalizer(odo, usPollerF, USLocalizer.LocalizationType.RISING_EDGE, navigator, wifi.getCorner());
			lightMotor.rotate(85);
			ObjectFinder of = new ObjectFinder(leftMotor, rightMotor, navigator, lsl, odo,
					usPollerF, usPollerS, usl, lsPoller, resolution, wifi.getxHalf(), wifi.getyHalf());
			of.pointDriver();
		}
		System.exit(0);	
}

}