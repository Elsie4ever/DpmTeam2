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
import trotty02.ObjectSearch;
import trotty02.Odometer;
import trotty02.OdometryDisplay;
import trotty02.SimpleNavigation;
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
	private static final EV3LargeRegulatedMotor frontMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
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
		
		int buttonChoice;
		final TextLCD t = LocalEV3.get().getTextLCD();
		
		// setup the odometer, moving window and display
		Odometer odo = new Odometer(leftMotor, rightMotor, WHEEL_RADIUS, TRACK);
		UltrasonicPoller usPoller = new UltrasonicPoller(usSensor, usData);
		ColorPoller coPoller = new ColorPoller(colorValue, colorData);
		OdometryDisplay odoDisp = new OdometryDisplay(odo, t, usPoller, coPoller);
		SimpleNavigation simpleNavigation = new SimpleNavigation(leftMotor, rightMotor, WHEEL_RADIUS, TRACK, bandCenter,
				odo, true, false);
		SimpleNavigation simpleNavigationLSL = new SimpleNavigation(leftMotor, rightMotor, WHEEL_RADIUS, TRACK, bandCenter,
				odo, true, true);
		
		ObjectID objectID = new ObjectID(coPoller, usPoller, t);
		USLocalizer usLocalizer = new USLocalizer(odo, usPoller, USLocalizer.LocalizationType.RISING_EDGE, simpleNavigationLSL);
		LightLocalizer lsl = new LightLocalizer(odo, usPoller, colorValue, colorData, simpleNavigationLSL);
		ObjectSearch objSearch = new ObjectSearch(objectID, usPoller, simpleNavigation, simpleNavigation,
				odo, leftClaw, rightClaw);
		
		do {
			// clear the display
			t.clear();

			// ask the user which path is the robot following
			t.drawString("< Left | Right >", 0, 0);
			t.drawString("       |        ", 0, 1);
			t.drawString("  Obj. |   Obj. ", 0, 2);
			t.drawString("  ID.  | Search ", 0, 3);
			t.drawString(" ______________ ", 0, 4);
			t.drawString("     v Down     ", 0, 5);
			t.drawString("   Obj. Search  ", 0, 6);
			t.drawString("Without localiz.", 0, 7);

			buttonChoice = Button.waitForAnyPress();
		}
		
		while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT && buttonChoice != Button.ID_DOWN);
		
		if(buttonChoice == Button.ID_LEFT){
			
			odo.start();
			odoDisp.start();
			usPoller.start();
			coPoller.start();
			
			// perform the object identification
			objectID.start();
		}
		else if(buttonChoice == Button.ID_RIGHT){
			
			odo.start();
			odoDisp.start();
			usPoller.start();
			coPoller.start();
			objectID.start();
			
			try { Thread.sleep(1000); } catch(Exception e){}
			
			Sound.setVolume(Sound.VOL_MAX);
			
			// 1. Localize and go to (0, 0) using rising edge
			usLocalizer.doLocalization();
			lsl.doLocalization();
			
			do{
				t.drawString("Press Enter", 0, 7);
			}
			while(Button.waitForAnyPress() != Button.ID_ENTER);
			
			// 2. Perform the object search
			objSearch.start();
		}
		else{
			odo.start();
			odoDisp.start();
			usPoller.start();
			coPoller.start();
			objectID.start();
			
			try { Thread.sleep(1000); } catch(Exception e){}
			
			Sound.setVolume(Sound.VOL_MAX);
			
			// 1. Perform the object search
			objSearch.start();
		}
			
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);	
		
	}
}
