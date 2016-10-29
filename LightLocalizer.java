package collector;
import lejos.robotics.SampleProvider;

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;	
	private int corner;
	
	public LightLocalizer(Odometer odo, Navigation navigator, SampleProvider colorSensor, float[] colorData, int corner) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.corner = corner;
	}
	
	public void doLocalization() {
		//TODO: determine and implement best localization routine
		//TODO: use corner position to correct the angle
	}

}
