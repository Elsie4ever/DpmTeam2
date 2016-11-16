package trotty02;

import trotty02.Navigation;
import lejos.hardware.Sound;
import trotty02.UltrasonicPoller;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static double ROTATION_SPEED = 30;

	private Odometer odo;
	private LocalizationType locType;
	private static final double WALL_DIST = 30;
	private Navigation simNav;
	private UltrasonicPoller usPoller;
	private static final double FRONT_SENSOR_OFFSET = 12.8;
	private static final double BACK_SENSOR_OFFSET = 3.9;
	private final double FE_OFFSET = 0; //-1*Math.PI/180;
	private final double RE_OFFSET = 0; //-3.4*Math.PI/180;
	private enum Sensor{
		FRONT, BACK
	}
	private Sensor sensor;
	
	public USLocalizer(Odometer odo, UltrasonicPoller usPoller, LocalizationType locType, Navigation simNav) {
		this.odo = odo;
		this.usPoller = usPoller;
		this.locType = locType;
		this.simNav = simNav;
	}
	
	public void doLocalization() {
		double angleA, angleB;
		double heading;
		
		//Check which sensor to sense with
		//selectSensor();
		sensor = Sensor.BACK;
		
		if (locType == LocalizationType.FALLING_EDGE){
			// rotate the robot until it sees no wall
			while(getWallDist() <= WALL_DIST){
				simNav.turnCW();
				
				if(getWallDist() > WALL_DIST){
					simNav.stopMov();
					break;
				}
			}
			
			odo.setTheta(0);
			heading = odo.getTheta(); // Remember the "heading"
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(!(getWallDist() < WALL_DIST)){
				simNav.turnCW();
				
				if(getWallDist() <= WALL_DIST){
					simNav.stopMov();
					break;
				}
			}
			angleA = odo.getTheta() - heading;
			
			// switch direction and wait until it sees no wall
			while(getWallDist() <= WALL_DIST){
				simNav.turnCCW();
				
				if(getWallDist() > WALL_DIST){
					simNav.stopMov();
					break;
				}
			}
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(!(getWallDist() < WALL_DIST)){
				simNav.turnCCW();
				
				if(getWallDist() <= WALL_DIST){
					simNav.stopMov();
					break;
				}
			}
			
			angleB = ((2*Math.PI - (odo.getTheta() - heading))%(2*Math.PI) + 2*Math.PI)%(2*Math.PI) + angleA;
			
			// Turn back to heading
			simNav.turnTo(heading);
			
			// calculate the actual heading
			double curHeading = (angleB - angleA) - (angleB - Math.PI/2)/2;
			
			// update the odometer position (example to follow:)
			odo.setTheta(curHeading);
			
			//Turn to the x-axis
			simNav.turnTo(0);
			
			// Mend the theta with our testing mean
			odo.setTheta(odo.getTheta() + FE_OFFSET);
		}
		
		else { // RISING_EDGE
			/**
			 * Turn CCW until it sees the left wall.
			 * Set that angle to zero.
			 * Turn CW until it sees the right wall.
			 * (LeftAngle - RightAngle)/2 = 225 degrees
			 */
			
			//Turn until it see a wall
			while(getWallDist() >= WALL_DIST){
				simNav.turnCW();
				
				if(getWallDist() < WALL_DIST){
					simNav.stopMov();
					break;
				}
			}
			
			//Turn until it doens't see the wall
			while(getWallDist() <= WALL_DIST){
				simNav.turnCW();
				
				if(getWallDist() > WALL_DIST){
					simNav.stopMov();
					break;
				}
			}
			
			//Set that angle to zero
			odo.setTheta(0);
			
			//Turn until it sees a wall
			while(getWallDist() >= WALL_DIST){
				simNav.turnCCW();
				
				if(getWallDist() < WALL_DIST){
					simNav.stopMov();
					break;
				}
			}
			
			//Turn until it doesn't see a wall
			while(getWallDist() <= WALL_DIST){
				simNav.turnCCW();
				
				if(getWallDist() > WALL_DIST){
					simNav.stopMov();
					break;
				}
			}
			
			double curTheta = Math.toRadians(225) + odo.getTheta()/2;
			odo.setTheta(curTheta);
			
			simNav.turnTo(0);
		}
	}
	
	private void selectSensor(){
		if(UltrasonicPoller.getDistFront() < WALL_DIST){
			sensor = Sensor.FRONT;
			Sound.beepSequenceUp();
		}
		else if(UltrasonicPoller.getDistBack() < WALL_DIST){
			sensor = Sensor.BACK;
			Sound.beepSequence();
		}
		else{
			sensor = Sensor.BACK;
			Sound.beepSequence();
		}
	}
	
	private double getWallDist(){
		double dist;
		if(sensor == Sensor.BACK){
			dist = UltrasonicPoller.getDistBack();
		}
		else{ //sensor == Sensor.FRONT
			dist = UltrasonicPoller.getDistFront();
		}
		return dist;
	}
}
