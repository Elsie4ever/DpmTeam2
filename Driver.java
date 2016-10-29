package collector;
import lejos.hardware.*;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.Navigator;
import collector.ColorPoller;
import collector.Navigation;
import collector.Odometer;
import collector.UltrasonicPoller;

public class Driver {

	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	// Ultrasonic sensor port connected to input S1
	// Color sensor port connected to input S2
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Port usPort = LocalEV3.get().getPort("S1");		
	private static final Port colorPort = LocalEV3.get().getPort("S2");		
	private static int[][]map;
	
	public static void main(String[] args) {
		
		//creates wiInfo which takes in wireless info
		Wireless wiInfo = new Wireless();
	
		//sets up usSensor
		@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			// colorValue provides samples from this instance
		float[] usData = new float[usValue.sampleSize()];				// colorData is the buffer in which data are returned
		
		//sets up colorSensor
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorValue = colorSensor.getMode("Red");			// colorValue provides samples from this instance
		float[] sample = new float[colorValue.sampleSize()];			// colorData is the buffer in which data are returned
		
		final Mapping mapping = new Mapping(map);
		
		final ColorPoller colPol = new ColorPoller(colorSensor, sample);
		final UltrasonicPoller usPoller = new UltrasonicPoller(usValue, usData);	// the selected controller on each cycle
		final Odometer odo = new Odometer(leftMotor, rightMotor, 30, true);
		final Navigation navigator = new Navigation(odo);

		
		
		// setup the display
		LCDInfo lcd = new LCDInfo(odo);
		
		// perform the ultrasonic localization
		USLocalizer usl = new USLocalizer(odo, navigator, usValue, usData, USLocalizer.LocalizationType.FALLING_EDGE, wiInfo.getCorner());
		usl.doLocalization();
		
		// perform the light sensor localization
		LightLocalizer lsl = new LightLocalizer(odo, navigator, colorValue, sample, wiInfo.getCorner());
		lsl.doLocalization();			
		
		//BOT IS DONE LOCALIZATION
		
		//begin by traveling to own zone
		int zoneX = wiInfo.getZoneX();
		int zoneY = wiInfo.getZoneY();
		navigator.travelTo(zoneX, zoneY, map);
		
		
		
		
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);	
		
	}

}
