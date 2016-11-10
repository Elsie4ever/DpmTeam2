package trotty02;
import java.util.Arrays;

import lejos.robotics.SampleProvider;

//
//  Control of the wall follower is applied periodically by the 
//  UltrasonicPoller thread.  The while loop at the bottom executes
//  in a loop.  Assuming that the us.fetchSample, and cont.processUSData
//  methods operate in about 20mS, and that the thread sleeps for
//  50 mS at the end of each loop, then one cycle through the loop
//  is approximately 70 mS.  This corresponds to a sampling rate
//  of 1/70mS or about 14 Hz.
//


public class UltrasonicPoller extends Thread{
	
	private SampleProvider usFront;
	private float[] usDataFront;
	private static double[] windowFront = {0,0,0,0,0};
	
	private SampleProvider usSide;
	private float[] usDataSide;
	private static double[] windowSide = {0,0,0,0,0};
	
	private SampleProvider usBack;
	private float[] usDataBack;
	private static double[] windowBack = {0,0,0,0,0};
	
	private static Object lock = new Object();
	private static final int THRESHOLD = 80;
	
	private double distance;
	//private static final int THRESHOLD = 50;
	
	public UltrasonicPoller(SampleProvider usFront, float[] usDataFront, SampleProvider usSide, float[] usDataSide,
			SampleProvider usBack, float[] usDataBack) {
		this.usFront = usFront;
		this.usDataFront = usDataFront;
		
		this.usSide = usSide;
		this.usDataSide = usDataSide;
		
		this.usBack = usBack;
		this.usDataBack = usDataBack;
	}

//  Sensors now return floats using a uniform protocol.
//  Need to convert US result to an integer [0,255]
	
	public void run() {
		while (true) {
			usFront.fetchSample(usDataFront,0);							// acquire data
			usSide.fetchSample(usDataSide,0);
			usBack.fetchSample(usDataBack,0);
			
			filter((usDataFront[0]*100.0));								// extract from buffer, cast to int
			windowFilter(this.distance, Sensor.FRONT);
			
			filter((usDataSide[0]*100.0));
			windowFilter(this.distance, Sensor.SIDE);
			
			filter((usDataBack[0]*100.0));
			windowFilter(this.distance, Sensor.BACK);
			
			try { Thread.sleep(50); } catch(Exception e){}				// Poor man's timed sampling
		}
	}
	
	private enum Sensor{
		FRONT, BACK, SIDE
	}
	
	public void filter(double distance){
		/**
		 * Filters the distance to be within the threshold
		 * @param	distance, {double} the value to filter
		 */
		if (distance >= THRESHOLD) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distance = THRESHOLD;
		} else {
			// distance went below 255: reset filter and leave
			// distance alone.
			this.distance = distance;
		}
	}
	
	public static double getDistFront(){
		/**
		 * Get the median value in the window from the front usSensor
		 * @return	{double}, the distance value
		 */
		synchronized(lock){
			double [] clone = clone(windowFront);
			Arrays.sort(clone);
			return clone[clone.length/2];
		}
	}
	
	public static double getDistSide(){
		/**
		 * Get the median value in the window from the side usSensor
		 * @return	{double}, the distance value
		 */
		synchronized(lock){
			double [] clone = clone(windowSide);
			Arrays.sort(clone);
			return clone[clone.length/2];
		}
	}
	
	public static double getDistBack(){
		/**
		 * Get the median value in the window from the back usSensor
		 * @return	{double}, the distance value
		 */
		synchronized(lock){
			double [] clone = clone(windowBack);
			Arrays.sort(clone);
			return clone[clone.length/2];
		}
	}
	
	private static double [] clone (double[] original){
		/**
		 * Clones an array
		 * @param	original, {double []} the array to clone
		 * @return	{double []}, clone of the original []
		 */
		synchronized(lock){
			double [] clone = new double[original.length];
			
			for(int i = 0; i < original.length; i++){
				clone[i] = original[i];
			}
			
			return clone;
		}
	}
	
	private static void shift(double[] window){
		/**
		 * Shifts an array one offset towards the end of the array
		 * @param	window, {double []} to shift  
		 */
		synchronized(lock){
			for(int i = 0; i < window.length - 1; i++){
				window[i] = window[i + 1];
			}
		}
	}
	
	private static void windowFilter(double distance, Sensor type){
		/**
		 * Inputs the distance value into the correct window
		 * @param	distance, {double} distance to input to the window
		 * @param	type, {Sensor} window corresponding to the usSensor that gave the input
		 */
		if(type == Sensor.FRONT){
			synchronized(lock){
				shift(windowFront);
				windowFront[windowFront.length - 1] = distance;
			}
		}
		else if(type == Sensor.SIDE){
			synchronized(lock){
				shift(windowSide);
				windowSide[windowSide.length - 1] = distance;
			}
		}
		else{ // if type == Sensor.Back
			synchronized(lock){
				shift(windowBack);
				windowBack[windowBack.length - 1] = distance;
			}
		}
	}
}
