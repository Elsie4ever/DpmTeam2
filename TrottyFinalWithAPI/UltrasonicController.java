package trotty02;
/**
 * 
 * @author 
 *
 */
public interface UltrasonicController {
	
	/**
	 * interprets the incoming readings from the sensors and gives instructions to react to them
	 * @param distance the distance read from the sensor
	 */
	public void processUSData(int distance);
	
	/**
	 * gets the distance from the sensor
	 * @return the distance
	 */
	public int readUSDistance();
}
