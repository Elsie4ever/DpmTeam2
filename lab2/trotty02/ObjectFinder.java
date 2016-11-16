package trotty02;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class ObjectFinder {
	
	double[][] waypoints = new double[][]{
			  {0, 0},
			  {30, 0},
			  {60, 0},
			  {60, 30},
			  {30, 30},
			  {0, 30},
			  {0, 60},
			  {30, 60},
			  {60, 60}
			};
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private Navigation navigator;
	//private Avoidance avoidance;
	//private Capture capture;
	//private ColorPoller colorPoller;
	//private Display display;
	private LightLocalizer lightLocalizer;
	private UltrasonicPoller USpoller;
	private USLocalizer usLocalizer;
	private double radius;
	private double width;
	
	
	public ObjectFinder(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Navigation navigator,
			Odometer odometer,
			UltrasonicPoller USpoller, USLocalizer usLocalizer, double radius, double width) {
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.navigator = navigator;
		//this.avoidance = avoidance;
		//this.capture = capture;
		//this.colorPoller = colorPoller;
		//this.display = display;
		this.lightLocalizer = lightLocalizer;
		this.USpoller = USpoller;
		this.usLocalizer = usLocalizer;
		this.radius = radius;
		this.width = width;
	}
	
	public void squareDriver (){
		navigator.travelTo(0.0, 60.0);
		navigator.travelTo(60, 60);
		navigator.travelTo(60, 0);
		navigator.travelTo(0, 0);
	}
	
	public void pointDriver(){
		
	}
	
}
