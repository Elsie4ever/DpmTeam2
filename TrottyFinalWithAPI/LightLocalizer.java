package trotty02;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
/**
 * 
 * @author Elsie
 * @since 3.0
 *
 */
public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;	
	private double d=11;	//8.5//by measurement, the distance between ls and the center of track
	private double x,y,theta; //values to compute
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private Navigation navigator = null;
	
	public static final double WHEEL_RADIUS = 2.1;		//its actually between 2.15 and 2.10 but by trials it seems like 2.1 works perfectly 
	public static final double TRACK = 15.7;			//by measurement, the distance between two wheels

	//get both motors from lab4
	public static double ROTATION_SPEED = 100;	//rotation speed
	public static double forwardspeed=100;	
	private static final int sleepperiod=200;	//i frist have this value like 10 or something
	//later by testing i found its better leave it 0 thus no sleeping time 
	
	/**
	 * 
	 * @param odo the odometer
	 * @param colorSensor the light sensor that detects the color
	 * @param colorData	the actual values recorded by the color sensor
	 */
	public LightLocalizer(Odometer odo, SampleProvider colorSensor, float[] colorData) 
	{
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.leftMotor = this.odo.getLeftMotor();
		this.rightMotor = this.odo.getRightMotor();
	}
	
	/**
	 * makes the bot spin around at a location near black gridlines in order
	 * to calculate its precise position on the board
	 */
	public void doLocalization() {
		double [] pos = new double [3];//declare an array to store the odometer value
		Navigation navi=new Navigation(this.odo);//thus i can use method in navigation.java class
		
		
		odo.setPosition(new double [] {0.0, 0.0, 135}, new boolean [] {false, false, true});
		//set current position as 0,0 and 45 degree
		leftMotor.setSpeed((int)forwardspeed);
		rightMotor.setSpeed((int)forwardspeed);
		leftMotor.rotate(convertDistance(WHEEL_RADIUS,17),true);//8.5
		rightMotor.rotate(convertDistance(WHEEL_RADIUS,17),false);//8.5
		//by trial, this distance is enough for the robot to do ls localization
		leftMotor.stop();
		rightMotor.stop();
		
		//stop both motos
		// above code drives the robot to location listed in tutorial
		
		
	
	}
	/**
	 * converts an angle into an arclength
	 * 
	 * @param radius the wheel radius
	 * @param width the distance between the two centers of the wheels on the bot
	 * @param angle the angle to convert into a distance
	 * @return
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	/**
	 * converts a distance into an angle
	 * 
	 * @param radius the wheel radius
	 * @param distance the distance to convert into an angle
	 * @return
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	//helper method 
}