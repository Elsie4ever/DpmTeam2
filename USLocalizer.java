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
	private static final double FRONT_SENSOR_OFFSET = 12.5;
	private static final double BACK_SENSOR_OFFSET = 3.5;
	private final double FE_OFFSET = -1*Math.PI/180;
	private final double RE_OFFSET = -3.4*Math.PI/180;
	
	public USLocalizer(Odometer odo, UltrasonicPoller usPoller, LocalizationType locType, Navigation simNav) {
		this.odo = odo;
		this.usPoller = usPoller;
		this.locType = locType;
		this.simNav = simNav;
	}
	
	public void doLocalization() {
		// double [] pos = new double [3];
		double angleA, angleB;
		double heading;
		
		if (locType == LocalizationType.FALLING_EDGE){
			// rotate the robot until it sees no wall
			while(UltrasonicPoller.getDistFront() <= WALL_DIST){
				simNav.turnCW();
				
				if(UltrasonicPoller.getDistFront() > WALL_DIST){
					simNav.stopMov();
					break;
				}
			}
			
			odo.setTheta(0);
			heading = odo.getTheta(); // Remember the "heading"
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(!(UltrasonicPoller.getDistFront() < WALL_DIST)){
				simNav.turnCW();
				
				if(UltrasonicPoller.getDistFront() <= WALL_DIST){
					simNav.stopMov();
					break;
				}
			}
			angleA = odo.getTheta() - heading;
			
			// switch direction and wait until it sees no wall
			while(UltrasonicPoller.getDistBack() <= WALL_DIST + FRONT_SENSOR_OFFSET - BACK_SENSOR_OFFSET){
				simNav.turnCCW();
				
				if(UltrasonicPoller.getDistBack() > WALL_DIST + FRONT_SENSOR_OFFSET - BACK_SENSOR_OFFSET){
					simNav.stopMov();
					break;
				}
			}
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(!(UltrasonicPoller.getDistBack() < WALL_DIST + FRONT_SENSOR_OFFSET - BACK_SENSOR_OFFSET)){
				simNav.turnCCW();
				
				if(UltrasonicPoller.getDistBack() <= WALL_DIST + FRONT_SENSOR_OFFSET - BACK_SENSOR_OFFSET){
					simNav.stopMov();
					break;
				}
			}
			angleB = ((2*Math.PI - (odo.getTheta() - heading))%(2*Math.PI) + 2*Math.PI)%(2*Math.PI) + angleA;
			
			//readjust angleB to be the opposite of it since we're using the back sensor
			angleB = (angleB + Math.PI)%Math.PI;
			
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
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			
			// rotate the robot until it sees a wall
			while(UltrasonicPoller.getDistFront() >= WALL_DIST){
				simNav.turnCW();
				
				if(UltrasonicPoller.getDistFront() < WALL_DIST){
					simNav.stopMov();
					break;
				}
			}
			
			odo.setTheta(0);
			heading = odo.getTheta(); // Remember the "heading"
			
			// keep rotating until the robot sees no wall, then latch the angle
			while(!(UltrasonicPoller.getDistFront() > WALL_DIST)){
				simNav.turnCW();
				
				if(UltrasonicPoller.getDistFront() >= WALL_DIST){
					simNav.stopMov();
					break;
				}
			}
			angleA = odo.getTheta() - heading;
			
			// switch direction and wait until it sees a wall
			while(UltrasonicPoller.getDistBack() >= WALL_DIST + FRONT_SENSOR_OFFSET - BACK_SENSOR_OFFSET){
				simNav.turnCCW();
				
				if(UltrasonicPoller.getDistBack() < WALL_DIST + FRONT_SENSOR_OFFSET - BACK_SENSOR_OFFSET){
					simNav.stopMov();
					break;
				}
			}
			
			// keep rotating until the robot sees no wall, then latch the angle
			while(!(UltrasonicPoller.getDistBack() > WALL_DIST + FRONT_SENSOR_OFFSET - BACK_SENSOR_OFFSET)){
				simNav.turnCCW();
				
				if(UltrasonicPoller.getDistBack() >= WALL_DIST + FRONT_SENSOR_OFFSET - BACK_SENSOR_OFFSET){
					simNav.stopMov();
					break;
				}
			}
			angleB = ((2*Math.PI - (odo.getTheta() - heading))%(2*Math.PI) + 2*Math.PI)%(2*Math.PI) + angleA;
			
			//readjust angleB to be the opposite of it since we're using the back sensor
			
			angleB = (angleB + Math.PI)%Math.PI;
			
			// Turn back to heading
			simNav.turnTo(heading);
			
			// calculate the actual heading
			double curHeading = (angleB - angleA) - (Math.PI + (angleB - Math.PI/2)/2);
			
			// update the odometer position (example to follow:)
			odo.setTheta(curHeading);
			
			//Turn to the x-axis
			simNav.turnTo(0);
			
			// Mend the theta with our testing mean
			odo.setTheta(odo.getTheta() + RE_OFFSET);
		}
	}
}
