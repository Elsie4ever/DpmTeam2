package trotty02;
/**
 * 
 * @author 
 *
 */
public interface UltrasonicController {
	
	public void processUSData(int distance);
	
	public int readUSDistance();
}
