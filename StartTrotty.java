package trotty02;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import trotty02.ColorPoller;
import trotty02.LightLocalizer;
import trotty02.ObjectID;
//import trotty02.ObjectSearch;
import trotty02.Odometer;
import trotty02.Display;
import trotty02.Navigation;
import trotty02.USLocalizer;
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
	private static final EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S1);
	
	//Constants
	public static final double WHEEL_RADIUS = 2.15; // 2.1
	public static final double TRACK = 8.75; //15.8 15.2
	private static final int bandCenter = 10;			// Offset from the wall (cm) 20
	private static final int bandWidth = 2;				// Width of dead band (cm) 2

	
	public static void main(String[] args) {
		
		//Setup ultrasonic sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		@SuppressWarnings("resource")		// Because we don't bother to close this resource
		SensorModes usSensorFront = new EV3UltrasonicSensor(usPortFront);
		SampleProvider usValueFront = usSensorFront.getMode("Distance");		// usSensor reads distance
		float[] usDataFront = new float[usValueFront.sampleSize()];				// store values in here
		
		SensorModes usSensorSide = new EV3UltrasonicSensor(usPortSide);
		SampleProvider usValueSide = usSensorSide.getMode("Distance");			// usSensor reads distance
		float[] usDataSide = new float[usValueSide.sampleSize()];				// store values in here
		
		SensorModes usSensorBack = new EV3UltrasonicSensor(usPortBack);
		SampleProvider usValueBack = usSensorBack.getMode("Distance");			// usSensor reads distance
		float[] usDataBack = new float[usValueBack.sampleSize()];				// store values in here
		
		//Setup color sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		colorSensor.setCurrentMode(colorSensor.getRGBMode().getName());
		
		SampleProvider colorValue = colorSensor.getMode("RGB");			// colorValue provides samples from this instance
		float[] colorData = new float[colorValue.sampleSize()];			// colorData is the buffer in which data are returned
		
		final TextLCD t = LocalEV3.get().getTextLCD();
		
		// setup the odometer, moving window and displaylay
		Odometer odo = new Odometer(leftMotor, rightMotor, WHEEL_RADIUS, TRACK);
		UltrasonicPoller usPoller = new UltrasonicPoller(usSensorFront, usDataFront, usSensorSide, usDataSide,
				usSensorBack, usDataBack);
		ColorPoller coPoller = new ColorPoller(colorValue, colorData);
		Display display = new Display(odo, t, usPoller, coPoller);
		Navigation Navigation = new Navigation(leftMotor, rightMotor, WHEEL_RADIUS, TRACK, bandCenter,
				odo, true, false);
		Navigation NavigationLSL = new Navigation(leftMotor, rightMotor, WHEEL_RADIUS, TRACK, bandCenter,
				odo, true, true);
		
		ObjectID objectID = new ObjectID(coPoller, usPoller, t);
		USLocalizer usLocalizer = new USLocalizer(odo, usPoller, USLocalizer.LocalizationType.RISING_EDGE, NavigationLSL);
		LightLocalizer lsl = new LightLocalizer(odo, usPoller, colorValue, colorData, lightMotor, NavigationLSL);
		/*ObjectSearch objSearch = new ObjectSearch(objectID, usPoller, Navigation, Navigation,
				odo, clawMotor);
		*/
		
		//First, choose what kind if light settings we're demoing in
		
		int lightingChoice;
		
		do {
			// clear the displaylay
			t.clear();

			// ask the user which path is the robot following
			t.drawString("< Left | Right >", 0, 0);
			t.drawString("       |        ", 0, 1);
			t.drawString("  Lab  | Morning", 0, 2);
			t.drawString("----------------", 0, 3);
			t.drawString("^  Up  |  Down v", 0, 4);
			t.drawString("       |        ", 0, 5);
			t.drawString(" Noon  |  Night ", 0, 6);

			lightingChoice = Button.waitForAnyPress();
		}
		
		while(lightingChoice != Button.ID_LEFT && lightingChoice != Button.ID_RIGHT
				&& lightingChoice != Button.ID_DOWN && lightingChoice != Button.ID_UP);
		
		if(lightingChoice == Button.ID_LEFT){
			objectID.setStateOfDay(ObjectID.Light.LAB);
		}
		else if(lightingChoice == Button.ID_RIGHT){
			objectID.setStateOfDay(ObjectID.Light.MORNING);
		}
		else if(lightingChoice == Button.ID_DOWN){
			objectID.setStateOfDay(ObjectID.Light.NIGHT);
		}
		else{ //lightingChoice == Button.ID_UP
			objectID.setStateOfDay(ObjectID.Light.NOON);
		}
		
		
		//Now select which version to run
		
		int buttonChoice;
		do{
			// clear the display
			t.clear();

			// ask the user what they want to run
			t.drawString("< Left | Right >", 0, 0);
			t.drawString("       |        ", 0, 1);
			t.drawString("Locali.|Odo test", 0, 2);
			t.drawString("----------------", 0, 3);
			t.drawString("^  Up  |  Down v", 0, 4);
			t.drawString("       |  Full  ", 0, 5);
			t.drawString(" Stub  |  Run  ", 0, 6);
			
			buttonChoice = Button.waitForAnyPress();
		}
		
		while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT &&
				buttonChoice != Button.ID_UP && buttonChoice != Button.ID_DOWN);
		
		if(buttonChoice == Button.ID_LEFT){
			
			// testing localization only
			odo.start();
			display.start();
			usPoller.start();
			coPoller.start();
			
			// perform localization
			usLocalizer.doLocalization();
			lsl.doLocalization();
		}
		else if(buttonChoice == Button.ID_RIGHT){
			
			// simply testing the odometer
			odo.start();
			display.start();
			usPoller.start();
			coPoller.start();
			
			leftMotor.flt(true);
			rightMotor.flt();
		}
		else if(buttonChoice == Button.ID_UP){
			
			// to fill if there's another test to run
		}
		else{ // buttonChoice == Button.ID_DOWN
			odo.start();
			display.start();
			usPoller.start();
			coPoller.start();
			objectID.start();
			
			// Now wait for wifi before starting anything
		}
			
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);	
		
	}
}
