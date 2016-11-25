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
		
		
		/*double angle[]=new double[4];	//declare an array to store the angles 
		int countgridlines=0;	//counter
		while (countgridlines<=3) //this loop detects 4 gridlines 
		{	
			colorSensor.fetchSample(colorData,0);      		//get light sensor Red value 
			int LSvalue=  (int)((colorData[0])*100);			// times 100 into 0~100 scale,easier to test 
			pos=odo.getPosition();	//get current posistion from odometer
			if (LSvalue<=9.5)	//the floor is something above 70, 
				//when it first sees a black line, is 60~50, so i set 50 to make the robot stops quicker
				//note that when the ls is exactly above the black line, the lsvalue is less then 15
			{
				Sound.twoBeeps(); 
				angle[countgridlines]=pos[2];	//store current angle

				countgridlines++;	//counter counts
				if (countgridlines==4)	//if count to 4,then all 4 gridlines are detected, break the loop
				{
					leftMotor.stop();
					rightMotor.stop();
					//stop both motors
					Sound.beep();
					break; 
				}
			} 
			leftMotor.setSpeed((int) ROTATION_SPEED);
			rightMotor.setSpeed((int) ROTATION_SPEED);
			leftMotor.backward();
			rightMotor.forward();
			//rotate the robot counter-clockwise
			try { Thread.sleep(sleepperiod); } catch(Exception e){}	
		}
		// start rotating and clock all 4 gridlines
		
		
		
		
		
		
		double temp=0;
		
		temp=(360-angle[1]+angle[3])%360;
		y=-d*Math.cos(Math.PI*temp/360);
		temp=(360-angle[0]+angle[2])%360;
		x=-d*Math.cos(Math.PI*temp/360);
		theta=(angle[0]+180)+(((360 + angle[2] - angle[0])%360)/2);
		pos=odo.getPosition();
		theta=theta+pos[2];
		if (theta>=360)
		{
			theta=theta % 360;
		}
		if (theta<0)
		{	
			theta=360+theta;
		}
		//above calculation compute the current x,y position relative to 0,0, and the theta where the north is 
		odo.setPosition(new double [] {x, y, 0}, new boolean [] {true, true, true});	
		Sound.buzz();
		//navi.travelTo(0,0);
		navi.turnTo(45, true);
		leftMotor.stop();
		rightMotor.stop();
		odo.setPosition(new double [] {x, y, 0}, new boolean [] {false,false,true});*/
		// when done travel to (0,0) and turn to 0 degrees
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