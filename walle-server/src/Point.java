
public class Point {
	public int x;
	public int y;
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int distance(Point p) {
		return (int)Math.round(Math.sqrt((int)Math.pow(this.x - p.x,2) + (int)Math.pow(this.y - p.y,2)));
	}
	
	@Override public boolean equals(Object p) {
		Point point = (Point) p;
		
		return point.x == x && point.y == y;
	}
	public String toString() {
		return "("+x+","+y+")";
	}
}
