package trotty02;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;	
	private double d=6.75;	//8.5//by measurement, the distance between ls and the center of track
	private double x,y,theta; //values to compute
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	
	public static final double WHEEL_RADIUS = 2.1;		//its actually between 2.15 and 2.10 but by trials it seems like 2.1 works perfectly 
	public static final double TRACK = 15.7;			//by measurement, the distance between two wheels

	//get both motors from lab4
	public static double ROTATION_SPEED = 50;	//rotation speed
	public static double forwardspeed=100;	
	private static final int sleepperiod=100;	//i frist have this value like 10 or something
	//later by testing i found its better leave it 0 thus no sleeping time 

	private final static double LIGHT_SENSOR_OFFSET = 30;
	
	public LightLocalizer(Odometer odo, SampleProvider colorSensor, float[] colorData) 
	{
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.leftMotor = this.odo.getLeftMotor();
		this.rightMotor = this.odo.getRightMotor();
	}
	
	public void doLocalization() {
		double [] pos = new double [3];//declare an array to store the odometer value
		Navigation navi=new Navigation(this.odo);//thus i can use method in navigation.java class
		
		
		odo.setPosition(new double [] {0.0, 0.0, 45}, new boolean [] {false, false, true});
		//set current position as 0,0 and 45 degree
		leftMotor.setSpeed((int)forwardspeed);
		rightMotor.setSpeed((int)forwardspeed);
		leftMotor.rotate(convertDistance(WHEEL_RADIUS,16),true);//8.5
		rightMotor.rotate(convertDistance(WHEEL_RADIUS,16),false);//8.5
		//by trial, this distance is enough for the robot to do ls localization
		leftMotor.stop();
		rightMotor.stop();
		//stop both motos
		// above code drives the robot to location listed in tutorial
		
		
		double angle[]=new double[4];	//declare an array to store the angles 
		int countgridlines=0;	//counter
		while (countgridlines<=3) //this loop detects 4 gridlines 
		{	
			colorSensor.fetchSample(colorData,0);      		//get light sensor Red value 
			int LSvalue=  (int)((colorData[1])*100);			// times 100 into 0~100 scale,easier to test 
			pos=odo.getPosition();	//get current posistion from odometer
			if (LSvalue<=10)	//the floor is something above 70, 
				//when it first sees a black line, is 60~50, so i set 50 to make the robot stops quicker
				//note that when the ls is exactly above the black line, the lsvalue is less then 15
			{
				Sound.twoBeeps(); 
				angle[countgridlines]=pos[2]+LIGHT_SENSOR_OFFSET;	//store current angle
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
			try { Thread.sleep(350); } catch(Exception e){}	
		}
		// start rotating and clock all 4 gridlines
		
		
		
		
		
		
		double temp=0;
		temp=180-angle[1]+angle[3];
		y=d*Math.cos(Math.PI*temp/360);
		temp=Math.abs(angle[0]-angle[2]);
		x=-d*Math.cos(Math.PI*temp/360);
		theta=temp/2;
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
		odo.setPosition(new double [] {x, y, 0}, new boolean [] {true, true, false});	
		Sound.buzz();
		//navi.travelTo(0,0);
		navi.turnTo(angle[3], true);
		leftMotor.stop();
		rightMotor.stop();
		odo.setPosition(new double [] {x, y, 90}, new boolean [] {false,false,true});
		// when done travel to (0,0) and turn to 0 degrees
	}
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	//helper method 
}
