/*
* @author Sean Lawlor
* @date November 3, 2011
* @class ECSE 211 - Design Principle and Methods
* 
* Modified by F.P. Ferrie
* February 28, 2014
* Changed parameters for W2014 competition
*/
package trotty02;

public enum StartCorner {
	BOTTOM_LEFT(1, 0, 0, "BL"), BOTTOM_RIGHT(2, 300, 0, "BR"), TOP_RIGHT(3, 300, 300, "TR"), TOP_LEFT(4, 0, 300,
			"TL"), NULL(0, 0, 0, "NULL");

	private int id, x, y;
	private String name;
	/**
	 * Constructor for StartCorner, which determines which corner of the board the robot starts at
	 * @param id id argument represents the chosen corner
	 * @param x the start value on the x axis
	 * @param y the start value on the y axis
	 * @param name argument that identifies which role the robot will have during the game
	 */
	private StartCorner(int id, int x, int y, String name) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.name = name;
	}
	/**
	 * acquires the name in a readable string format
	 */
	public String toString() {
		return this.name;
	}
	/**
	 * gets the coordinates
	 * @return the x and the y values in an int array
	 */
	public int[] getCoordinates() {
		return new int[] { this.x, this.y };
	}
	/**
	 * gets the x value and returns it as an int 
	 * @return the x value
	 */
	public int getX() {
		return this.x;
	}
	/**
	 * gets the y values and returns it as an int
	 * @return
	 */
	public int getY() {
		return this.y;
	}
	/**
	 * gets the role identification information and returns it as an int
	 * @return the id value received
	 */
	public int getId() {
		return this.id;
	}
	/**
	 * Ensures that the corner values correspond to the corner information received 
	 * @param cornerId the corner information received
	 * @return void
	 */
	public static StartCorner lookupCorner(int cornerId) {
		for (StartCorner corner : StartCorner.values())
			if (corner.id == cornerId)
				return corner;
		return NULL;
	}
}