package trotty02;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import trotty02.UltrasonicPoller;
import lejos.hardware.lcd.TextLCD;
/**
 * 
 * @author Adam, Shi Yu, Elsie, Jean-Christophe, Ke
 *
 */
public class ObjectDetector {
	private SampleProvider usSensor;
	private float[] usData;
	private UltrasonicPoller usPoller;
	private boolean objectDetected = false;
	public String detectionMessage = "";
	private boolean block = false;
	//EV3ColorSensor cS = new EV3ColorSensor(LocalEV3.get().getPort("S2"));
	//SampleProvider colorSensor = cS.getColorIDMode();
	//int sampleSize = colorSensor.sampleSize();   
	//float[] sample = new float[sampleSize];
	//TextLCD t = LocalEV3.get().getTextLCD();
/**
 * Constructor for the object detection class that allows the robot to detect objects
 * @param usSensor The ultrasonic sensor
 * @param usData The data read by the ultrasonic sensor
 */
	public ObjectDetector(SampleProvider usSensor, float[] usData) {
		this.usSensor = usSensor;
		this.usData = usData;
	}
	/**
	 * A method that communicates to humans when an object has been detected
	 * @return a String that says that an object has been detected or not
	 */
	public String detectObj(){
		this.usPoller = new UltrasonicPoller(usSensor, usData);
		//if the robot sees something, stay in loop
		if(usPoller.seesSomething() || usPoller.getDistance() == 0){
			detectionMessage = "Object Detected";
			return detectionMessage;
		}
		else{

			//	colorSensor.fetchSample(sample, 0);
			//	System.out.println(sample[0]);
			//if()
			detectionMessage ="No Object";
			return detectionMessage;
		}
		//TODO block or styro
	
	}
	/**
	 * getting the detection message
	 * @return a string that says whether an object has been detected or not
	 */
	public String getDetection() {
		synchronized (this) {
			return detectionMessage;
		}
	}
	
	
}

