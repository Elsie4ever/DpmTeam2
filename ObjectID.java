package trotty02;

import lejos.hardware.Sound;
import lejos.hardware.lcd.TextLCD;

public class ObjectID extends Thread {
	private ColorPoller coPoller;
	private UltrasonicPoller usPoller;
	private Odometer odo;
	private Mapping map;
	private TextLCD t;
	private static final int THRESHOLD = 60;
	private static final int DIST = 6; //4
	private static double color;
	private static final double COLOR_LAB = 1.75;
	private static final double COLOR_MORNING = 1.75;
	private static final double COLOR_NOON = 1.75;
	private static final double COLOR_NIGHT = 1.75;
	
	
	public ObjectID(ColorPoller coPoller, UltrasonicPoller usPoller, TextLCD t){ // Constructor
		this.coPoller = coPoller;
		this.usPoller = usPoller;
		this.t = t;
		
	}
	
	public void run(){
		t.clear(); // Clear the display
		
		while(true){
			t.drawString("                 ", 0, 5);
			t.drawString("           ", 0, 6);
			
			//Check if there's an object & that it's close enough
			if(detectObj() && withinDist()){
				t.drawString("Object Detected: ", 0, 5);
				//Check what kind of block it is
				if(idObj()){
					t.drawString("    Block", 1, 6); //Styrofoam block
					//Sound.beep();
					map.updateMapAt(odo.getX(),odo.getY(),odo.getTheta(),usPoller.getDistFront());
				}
				else{
					t.drawString("Not Block", 1, 6);
					//Sound.beep();
					//Sound.beep();
				}
			}
			else if(detectObj()){
				t.drawString("Object Detected: ", 0, 5);
				t.drawString("Too Far", 1, 6);
			}
			
			try { Thread.sleep(50); } catch(Exception e){};
		}
	}
	
	public enum Light{
		LAB, MORNING, NOON, NIGHT
	}
	
	public void setStateOfDay(Light state){
		if(state == Light.LAB){
			color = COLOR_LAB;
		}
		else if(state == Light.MORNING){
			color = COLOR_MORNING;
		}
		else if(state == Light.NIGHT){
			color = COLOR_NIGHT;
		}
		else{ //state == Light.NOON
			color = COLOR_NOON;
		}
	}
	
	public boolean detectObj(){
		/**
		 * @return	{boolean} true if there's an object within it's line of sight
		 */
		
		boolean detected = false;
		
		if(UltrasonicPoller.getDistFront() <= THRESHOLD){
			detected = true;
		}
		
		return detected;
	}
	
	public boolean withinDist(){
		/**
		 * @return	{boolean} true if the block is within the distance where colors are read correctly
		 */
		
		boolean within = false;
		
		if(UltrasonicPoller.getDistFront() < DIST){
			within = true;
		}
		
		return within;
	}
	
	public boolean idObj(){
		/**
		 * @return	{boolena} true if it's a styrofoam block
		 */
		
		boolean styro = false;
		
		if(coPoller.getColor() < color){
			styro = true;
		}
		
		return styro;
	}
}
