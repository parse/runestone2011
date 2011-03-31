public class Tile {		
	public int dist;
	public Tile previous;
	public int x;
	public int y;
	public int data;
	
	public Tile(int x, int y, int data) {
		this.x = x;
		this.y = y;
		this.data = data;
		dist = 500000;
		previous = null;
	}
}