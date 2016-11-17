package trotty02;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class LCDInfo implements TimerListener{
	public static final int LCD_REFRESH = 100;
	private Odometer odo;
	private Timer lcdTimer;
	private TextLCD LCD = LocalEV3.get().getTextLCD();;
	private ObjectDetector OD;
	// arrays for displaying data
	private double [] pos;
	private String detection;
	private UltrasonicPoller usPoller;
	private LightPoller lightPoller;
	
	public LCDInfo(Odometer odo, UltrasonicPoller usPoller, LightPoller lightPoller) {
		this.usPoller = usPoller;
		this.lightPoller = lightPoller;
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		// initialise the arrays for displaying data
		pos = new double [3];
		
		// start the timer
		lcdTimer.start();
	}
	
	public void timedOut() { 
		odo.getPosition(pos);
		
		LCD.clear();
		LCD.drawString("X: ", 0, 0);
		LCD.drawString("Y: ", 0, 1);
		LCD.drawString("H: ", 0, 2);
		LCD.drawInt((int)(pos[0]), 3, 0);
		LCD.drawInt((int)(pos[1]), 3, 1);
		LCD.drawInt((int)pos[2], 3, 2);
		
		LCD.drawString("DistFront: ", 0, 3);
		LCD.drawString("DistSide: ", 0, 4);
		LCD.drawInt((int) usPoller.getDistFront(), 11, 3);
		LCD.drawInt((int) usPoller.getDistSide(), 10, 4);
		
		LCD.drawString("RBG: ", 0, 5);
		LCD.drawInt((int) Math.floor(this.lightPoller.getRGB()[1]), 5, 5);
		
		
		if(odo.seesSomething()){
		LCD.drawString("Object Detected.", 0,6);
			if(odo.seesBlock()){
				LCD.drawString("Block!", 0,7);
			}
			else{
				LCD.drawString("Not Block!", 0,7);
			}
		}
		else{
			LCD.drawString("No Object", 0, 6);
		}
		
		
	}
}