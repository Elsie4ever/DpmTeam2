package trotty02;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.utility.Timer;
import lejos.utility.TimerListener;
/**
 * 
 * @author 
 *
 */
public class LCDInfo implements TimerListener{
	public static final int LCD_REFRESH = 100;
	private Odometer odo;
	private Timer lcdTimer;
	private TextLCD LCD = LocalEV3.get().getTextLCD();;
	private ObjectDetector OD;
	// arrays for displaying data
	private double [] pos;
	private String detection;
	
	public LCDInfo(Odometer odo) {
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		// initialise the arrays for displaying data
		pos = new double [3];
		
		// start the timer
		lcdTimer.start();
	}
/**
 * 
 */
	public void timedOut() { 
		odo.getPosition(pos);
		
		LCD.clear();
		LCD.drawString("X: ", 0, 0);
		LCD.drawString("Y: ", 0, 1);
		LCD.drawString("H: ", 0, 2);
		LCD.drawInt((int)(pos[0]), 3, 0);
		LCD.drawInt((int)(pos[1]), 3, 1);
		LCD.drawInt((int)pos[2], 3, 2);
		if(odo.seesSomething()){
		LCD.drawString("Object Detected.", 0,5);
			if(odo.seesBlock()){
				LCD.drawString("Block!", 0,6);
			}
			else{
				LCD.drawString("Not Block!", 0,6);
			}
		}
		else{
			LCD.drawString("No Object", 0, 5);
		}
		
		
	}
}
