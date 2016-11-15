package trotty02;
import lejos.robotics.SampleProvider;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.lcd.TextLCD;

/**
 * 
 * @author Potassium
 *
 */
public class LightLocalizer extends Thread{
	private Odometer odo;
	private boolean seesLine = false;
	private double distSensorBot;
	private Navigation navigator = null;
	EV3ColorSensor cS = new EV3ColorSensor(LocalEV3.get().getPort("S2"));
	SampleProvider colorRGBSensor = cS.getRGBMode();
	int sampleSize = colorRGBSensor.sampleSize();   
	float[] sample = new float[sampleSize];

/**
 * Constructor for the light sensor's localization. Uses the odo, navigator and distSensorBot attributes to determine the location of the robot 
 * @param odo
 * @param navigator
 * @param distSensorBot
 */
	public LightLocalizer(Odometer odo, Navigation navigator, double distSensorBot) {
		this.odo = odo;
		this.navigator = navigator;
		this.distSensorBot = distSensorBot;
	}
/**
 * Method describing the localization strategy
 */
	public void doLocalization() {
		Sound.setVolume(50);

		// drive to location listed in tutorial
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
		//placing robot at appropriate place

		navigator.turnTo(90, true);

		while (!seesLine){ 

			//if does not see black line
			colorRGBSensor.fetchSample(sample, 0);
			 
			if (!(sample[0] < 0.09)){ //if does not see black line
				seesLine = false;
			}
			else{
				seesLine = true;
				Sound.beep(); //if it sees a line
			}
			//try { Thread.sleep(10);  } catch(Exception e){}		// Poor man's timed sampling


			//go forward to designated point
			//System.out.println("Not seeing a line!");
			navigator.setSpeeds(150, 150);
		}
		//navigator.goForward(10); //goes forwards a little extra (near to 0,0)
		seesLine = false;

		navigator.turnTo(0, true);

		while (!seesLine){ //if does not see black line
			colorRGBSensor.fetchSample(sample, 0);
			 
			if (!(sample[0] < 0.09)){ //if does not see black line
				seesLine = false;
			}
			else{
				seesLine = true;
				Sound.beep(); //if it sees a line
			}

			//try { Thread.sleep(10);  } catch(Exception e){}		// Poor man's timed sampling

			//go forward to designated point
			//System.out.println("Not seeing a line!");
			navigator.setSpeeds(150, 150);
		}
		seesLine = false;
		navigator.turnTo(45, false);
		navigator.goForward(9); //goes forwards a little extra (near to 0,0)


		//rotation
		double initial_y = 0;
		double initial_x = 0;
		double deltaAngle = 10;
		double theta_yMinus = 0;
		double deltaTheta = 0;

		for(int i = 0; i < 3; i++){
			while(!seesLine){
				navigator.turnTo(odo.getAng() + deltaAngle,  true);
				colorRGBSensor.fetchSample(sample, 0);
				 
				if ((sample[0] < 0.09)){ //if does not see black line
					try { Thread.sleep(100);  } catch(Exception e){}		// Poor man's timed sampling

					seesLine = true;
					Sound.beep(); //if it sees a line
				}
			}
			seesLine = false;
		}

		//bottom line (3rd line seen)
		//			System.out.println("Found d"+ odo.getAng());
		Sound.beep();
		initial_y = odo.getAng();


		while(!seesLine){
			navigator.turnTo(odo.getAng() + deltaAngle,  true);
			colorRGBSensor.fetchSample(sample, 0);
			 

			if ((sample[0] < 0.09)){ //if does see black line
				try { Thread.sleep(100);  } catch(Exception e){}		// Poor man's timed sampling

				seesLine = true;
				Sound.beep(); //if it sees a line
			}
		}
		seesLine = false;

		initial_x = odo.getAng();
		Sound.beep();

		//right line, 4th line
		//					System.out.println("Found r, "+ odo.getAng());
		//odo.setPosition(new double[] {0,  0, odo.getAng()+30}, new boolean[] {false, false, true});


		while(!seesLine){//5th time
			navigator.turnTo(odo.getAng() + deltaAngle,  true);
			colorRGBSensor.fetchSample(sample, 0);
			 

			if ((sample[0] < 0.09)){ //if does not see black line
				try { Thread.sleep(100);  } catch(Exception e){}		// Poor man's timed sampling

				seesLine = true;
				Sound.beep(); //if it sees a line
			}
		}
		seesLine = false;

		//5th line, top
		odo.setPosition(new double[] {distSensorBot * Math.cos(2*Math.PI/360*(odo.getAng()-initial_y)/2), 0, 0}, new boolean[] {true, false, false});
		Sound.beep();

		//					System.out.println("Found u"+ odo.getAng());


		while(!seesLine){//6th time
			navigator.turnTo(odo.getAng() + deltaAngle,  true);
			colorRGBSensor.fetchSample(sample, 0);

			if ((sample[0] < 0.09)){ //if sees black line
				try { Thread.sleep(100);  } catch(Exception e){}		// Poor man's timed sampling

				seesLine = true;
				Sound.beep(); //if it sees a line
				
			}
		}
		seesLine = false;
		odo.setPosition(new double[] {0,  distSensorBot * Math.cos((2*Math.PI/360*(odo.getAng()-initial_x))/2), 0}, new boolean[] {false, true, false});
		//					System.out.println("Found l"+ odo.getAng());
		theta_yMinus = odo.getAng();
		deltaTheta = (270-(theta_yMinus + initial_y)/2);
		Sound.beep();
 
		// left line


		//odo.setPosition(new double[] {odo.getX(), odo.getY() , deltaTheta}, new boolean[] {false, false, true});
		navigator.travelTo(0, 0);
		odo.setPosition(new double[] {0,  0, 80}, new boolean[] {false, false, true}); //adjustment of angle

		navigator.turnTo(0, true);



		
	}
}

