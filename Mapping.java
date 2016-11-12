package trotty02;

public class Mapping {
	int[][] map = new int[12][12];
	//blocks are 1
	//empty or unexplored space is 0 
	
	
	public Mapping(){
		for(int i = 0; i < 12; i++){
			for(int j = 0; j < 12; j++){
				map[i][j] = 0;
			}
		}
		//sets up empty map
	}
	
	public int readMapAt(int x, int y){
		return map[x][y];
	}
	
	public void updateMapAt(double x, double y, double T, double distance){
		//takes in current bot position and usPoller distance
		//updates the map in the square where it saw the block
		map[(int)((x+distance*Math.cos(T))/30)][(int)((y+distance*Math.sin(T))/30)] = 1;
		
	}
	
}
