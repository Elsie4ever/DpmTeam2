package trotty02;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import trotty02.UltrasonicPoller;
import lejos.hardware.lcd.TextLCD;

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

	public ObjectDetector(SampleProvider usSensor, float[] usData, UltrasonicPoller usPoller) {
		this.usSensor = usSensor;
		this.usData = usData;
		this.usPoller = usPoller;
	}
	public String detectObj(){
		//if the robot sees something, stay in loop
		if(usPoller.seesSomething() || usPoller.getDistFront() == 0){
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
	public String getDetection() {
		synchronized (this) {
			return detectionMessage;
		}
	}
	
	
}
