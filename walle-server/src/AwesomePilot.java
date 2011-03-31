/*
 * Robot AwesomePilot
 * @author: Julius Sandgren
 */

import java.rmi.RemoteException;
import java.util.ArrayList; 
import java.util.Iterator;
import java.util.Comparator;
import java.util.PriorityQueue;


public class AwesomePilot implements Runnable {
	public boolean active = true;
	RmiServer robotServer;
	MapServerManagement mapConn;
	
	//TaggedSquare Class
	class TaggedArea {
		public boolean explored = false;
		public ArrayList<Point> entryPoints;
		public ArrayList<Point> exitPoints;
		public int[] lowerLeft;
		public int[] upperRight;
		public int width;
		public int height;
		public int discoveredTiles;
		public int tagged;
		public int score;
		public int[][] localMap;
		
		public TaggedArea(int[] lR, int[] uR) {
			lowerLeft = lR;
			upperRight = uR;
			entryPoints = calculateEntryPoints(lR, uR);
			exitPoints = new ArrayList<Point>();
			discoveredTiles = 0;
			width = upperRight[Xs] - lowerLeft[Xs] + 1;
			height = upperRight[Ys] - lowerLeft[Ys] + 1;
			tagged = 0;
			score = 1000;
			localMap = new int[width][height];
		}
		
		public ArrayList<Point> calculateEntryPoints(int[] lowerLeft, int[] upperRight) {
			//Point[] points = new Point[(upperRight[Xs] - lowerLeft[Xs]+1)*2 + (upperRight[Ys] - lowerLeft[Ys]+1)*2];
			ArrayList<Point> points = new ArrayList<Point>();			
			int i = 0;
			
			//Left bounds
			for(i = lowerLeft[Ys]; i<=upperRight[Ys]; i++) {
				if(lowerLeft[Xs] != 0 && map[lowerLeft[Xs]-1][i] != WALL) {
					points.add(new Point(lowerLeft[Xs]-1,i));
				}
			}
			
			//Upper bounds
			for(i = lowerLeft[Xs]; i<=upperRight[Xs]; i++) {
				if(upperRight[Ys] != mapSize[Ys]-1 && map[i][upperRight[Ys]+1] != WALL) {
					points.add(new Point(i,upperRight[Ys]+1));
				}
			}
			
			//Right bounds
			for(i = lowerLeft[Ys]; i<=upperRight[Ys]; i++) {
				if(upperRight[Xs] != mapSize[Xs]-1 && map[upperRight[Xs]+1][i] != WALL) {
					points.add(new Point(upperRight[Xs]+1,i));
				}
			}
			
			//Lower bounds
			for(i = lowerLeft[Xs]; i<=upperRight[Xs]; i++) {
				if(lowerLeft[Ys] != 0 && map[i][lowerLeft[Ys]-1] != WALL) {
					points.add(new Point(i,lowerLeft[Ys]-1));
				}
			}
			
			return points;
		}
	}
	
	//Constants
	public static final int UP = 0;		//UP = FORWARD
	public static final int RIGHT = 1;
	public static final int DOWN = 2;
	public static final int LEFT = 3;
	public static final int NULL = 4;
	public static final int Xs = 0;
	public static final int Ys = 1;
	public static final int TILE_SIZE = 25;
	public static final int TILE_SIZE_IN_STEPS = 10;
	public static final int SENSOR_MAX_DISTANCE = 150;
	public static final int[] mapSize = {100,100};
	public static final int TAGGED_AREA_SIZE = 5;
	public static final int[] STARTING_POSITION = {25,25};
	
	public static final int WALL_SAFE_DISTANCE_CM = 5;
	
	
	public static final int UNEXPLORED = 0;
	public static final int TAGGED = 1;
	public static final int FLOOR = 2;
	public static final int WALL = 3;
	public static final int ROBOT = 4;
	public static final int OTHERROBOT = 5;
	public static final int OCCUPIED = 6;
	public static final int CURRENTORDER = 10;
	
	//Variables
	int map[][] = new int[mapSize[0]][mapSize[1]];
	ArrayList<TaggedArea> taggedAreas = new ArrayList<TaggedArea>();
	int currentUltraDistances[] = new int[4];
	int currentPosition[] = new int[2];
	int currentHeading = UP;
	int currentUltraHeading = UP;
	
	
	
	public AwesomePilot(RmiServer rs) { 
		connectPilot(rs); 
		initialize();
		
	}
	
	public void run() {
		runPilot();
	}
	
	protected void finalize() throws Throwable {
		shutdownPilot();
	}

	
	public void runPilot() {
		while (active) {
			System.out.println("Running awesomepilot");
			
			map = mapConn.getMap(1);
			
			clearMarkers(ROBOT);
			clearMarkers(TAGGED);
			
			//Create all taggedAreas
			for(int i=0; i<mapSize[Xs]/TAGGED_AREA_SIZE; i++) {
				for(int j=0; j<mapSize[Ys]/TAGGED_AREA_SIZE; j++) {
					int[] lL = {i*TAGGED_AREA_SIZE,j*TAGGED_AREA_SIZE}; 
					int[] uR = {(i+1)*TAGGED_AREA_SIZE -1,(j+1)*TAGGED_AREA_SIZE -1}; 
					taggedAreas.add(new TaggedArea(lL, uR));
				}
			}
			
			
			Point closestEntryPoint = null;
			int distanceToClosest = 500000;
			TaggedArea closestTA = null;
			TaggedArea lastExplored = null;
			TaggedArea lastTagged = null;
			boolean donelidone = false;
			
			while(!donelidone) {
				
				//Fetch new map from mapserver
				fetchNewMap();
				
				//Check if we already are in the tagged area, then we dont need to look for an entrypoint
				if(lastTagged != null && pointIsInArea(new Point(currentPosition[Xs],currentPosition[Ys]),
						new Point(lastTagged.lowerLeft[Xs],lastTagged.lowerLeft[Ys]),
						new Point(lastTagged.upperRight[Xs],lastTagged.upperRight[Ys])) == true) {
					
					closestTA = lastTagged;
				}
				else {
					
					//Find closest entry point of all tagged areas which can be entered
					while(enterTaggedArea(closestTA,closestEntryPoint) == false) {
						
						closestEntryPoint = null;
						distanceToClosest = 500000;
						closestTA = null;
						
						//Iterate through the taggedAreas
						Iterator<TaggedArea> i = taggedAreas.iterator();
						while(i.hasNext()) {
							TaggedArea tA = (TaggedArea) i.next();
							
							if(tA.tagged == ROBOT && !tA.explored ) {	//Only check entry points if not fully explored and not tagged by other
								
								//Iterate through the taggedArea's entrypoints
								Iterator<Point> entryIterator = tA.entryPoints.iterator();
								while(entryIterator.hasNext()) {
									Point entryPoint = (Point) entryIterator.next();
									//Loop until we find a reachable entrypoint
									//Find the closest entry point
									ArrayList<int[]> path;
									
									//If it is the same position as the robot this is the best entryPoint
									if((new Point(currentPosition[Xs],currentPosition[Ys])).equals(entryPoint)) {
										distanceToClosest = 0;
										closestEntryPoint = entryPoint;
										closestTA = tA;
									}
									else if((path = findPathTo(new Point(currentPosition[Xs],currentPosition[Ys]),entryPoint)) != null ) {
										//System.out.println("size= " + path.size());
										if(path.size() < distanceToClosest) {
											distanceToClosest = path.size();
											closestEntryPoint = entryPoint;
											closestTA = tA;
										}
									}
								}
									
		
							}
							
						}
						
						
						if(closestTA == null) { 
							//If we still haven't found a tagged area break
							break;
						}
					}
				}
				
				if(closestTA != null) {
					//We have entered the area
					System.out.println("Explore area ("+ closestTA.lowerLeft[Xs] + ","+closestTA.lowerLeft[Ys]+")("+ closestTA.upperRight[Xs] + ","+closestTA.upperRight[Ys]+")");
					ArrayList<Point> exitPoints = explore(closestTA);
					System.out.println("Exitpoints: " + exitPoints.toString());
					System.out.println("Entrypoints: " + closestTA.entryPoints.toString());
					
					//Done exploring
					lastExplored = closestTA;
					
					//Reset closest TaggedArea
					closestEntryPoint = null;
					distanceToClosest = 500000;
					closestTA = null;
					lastTagged = null;
					
					//Check exit points against other entry points
					Iterator<Point> exitIterator = exitPoints.iterator();
					while(exitIterator.hasNext()) {
						Point exitPoint = (Point) exitIterator.next();
						
						//Iterate through the taggedAreas
						Iterator<TaggedArea> i = taggedAreas.iterator();
						while(i.hasNext()) {
							TaggedArea tA = (TaggedArea) i.next();
							if(tA.tagged == ROBOT && !tA.explored ) {	//Only check entry points if not fully explored and not tagged by other
								//Iterate through their entry points
								Iterator<Point> entryIterator = tA.entryPoints.iterator();
								while(entryIterator.hasNext()) {
									Point entryPoint = (Point) entryIterator.next();
									
									if(entryPoint.equals(exitPoint)) {
										System.out.println("Reusing exitpoint as entrypoint for another area");
										closestTA = tA;
										closestEntryPoint = entryPoint;
									}
								}
							}						
						}
						
					}
					
					
				}
				else {
					//Cannot reach any tagged area
					System.out.println("Cannot reach any tagged Area");	
					
					updateOwnershipTaggedAreas();
					
					//Check all exit points against all entry points
					boolean entryPointFound = false;
					Iterator<TaggedArea> i = taggedAreas.iterator();
					while(i.hasNext()) {
						TaggedArea tA = (TaggedArea) i.next();
						
						Iterator<Point> exitIterator = tA.exitPoints.iterator();
						while(exitIterator.hasNext()) {
							Point exitPoint = (Point) exitIterator.next();
							
							//Iterate through the taggedAreas
							Iterator<TaggedArea> i2 = taggedAreas.iterator();
							while(i2.hasNext()) {
								TaggedArea compareTA = (TaggedArea) i2.next();
								if(!compareTA.explored ) {	//Only check entry points if not fully explored and not tagged by other
									//Iterate through their entry points
									Iterator<Point> entryIterator = compareTA.entryPoints.iterator();
									while(entryIterator.hasNext()) {
										Point entryPoint = (Point) entryIterator.next();
										
										if(entryPoint.equals(exitPoint)) {
											entryPointFound = true;
										}
									}
								}						
							}
						}
					}
					if(!entryPointFound && lastExplored != null) {
						System.out.println("We are donelidone");
						donelidone = true;
					}
					else {
						//Tag new area
						TaggedArea bestTA = null;
						int bestScore = 0;
						
						//Calculate score for all tagged areas
						Iterator<TaggedArea> i2 = taggedAreas.iterator();
						while(i2.hasNext()) {
							TaggedArea candTA = (TaggedArea) i2.next();
							if(candTA.tagged == 0) {	//If not yet tagged
								//Find statistics about the area created by (all tagged areas + candidate area)
								int minX = candTA.lowerLeft[Xs];
								int minY = candTA.lowerLeft[Ys];
								int maxY = candTA.upperRight[Ys];
								int maxX = candTA.upperRight[Xs];
								int countTagged = 1;
								int countTaggedByOther = 0;
								
								Iterator<TaggedArea> i3 = taggedAreas.iterator();
								while(i3.hasNext()) {
									TaggedArea taggedTA = (TaggedArea) i3.next();
									if(taggedTA.tagged != 0) {
										if(taggedTA.tagged == ROBOT) {
											minX = Math.min(minX, taggedTA.lowerLeft[Xs]);
											minY = Math.min(minY, taggedTA.lowerLeft[Ys]);
											maxX = Math.max(maxX, taggedTA.upperRight[Xs]);
											maxY = Math.max(maxY, taggedTA.upperRight[Ys]);
											countTagged++;
										}
										else {	//OTHER ROBOT
											countTaggedByOther++;
										}
									}
									
								}
								
								//Calculate size of area
								int areaSize = (maxX-minX+1) * (maxY-minY+1) / (TAGGED_AREA_SIZE * TAGGED_AREA_SIZE);
								
								//Calculate fill percent
								double fillPercent = (double)(countTagged + countTaggedByOther) / (double)areaSize;
								
								//Difference in width and height
								int dimensionsDiff = Math.abs((maxX-minX+1) - (maxY-minY+1)) / TAGGED_AREA_SIZE;
								
								//Measure distance to center of map
								int distToCenter = (int)Math.round((Math.abs( (candTA.lowerLeft[Xs] + (int)Math.floor(TAGGED_AREA_SIZE/2)) - STARTING_POSITION[Xs]) +
																	Math.abs( (candTA.lowerLeft[Ys] + (int)Math.floor(TAGGED_AREA_SIZE/2)) - STARTING_POSITION[Ys])
																	)/TAGGED_AREA_SIZE);
								
								
								//Calculate score for tagged area
								candTA.score = 1000 - (dimensionsDiff*3 + distToCenter + (int)Math.round((1.0 - fillPercent)*10.0));
								//System.out.println("("+candTA.lowerLeft[Xs]+","+candTA.lowerLeft[Ys]+") dist="+distToCenter+ " diff="+dimensionsDiff+ " area="+areaSize + " fillPercent="+fillPercent +  " score="+candTA.score);
								
								//Pick the highest score
								if(candTA.score > bestScore ) {
									bestScore = candTA.score;
									bestTA = candTA;
								}
							}
										
						}
						
						if(bestTA != null) {
							System.out.println("Found best area : "+bestTA.lowerLeft[Xs]+","+bestTA.lowerLeft[Ys]+" score="+bestTA.score);
							System.out.println("Tagging area ("+ bestTA.lowerLeft[Xs] + ","+bestTA.lowerLeft[Ys]+")("+ bestTA.upperRight[Xs] + ","+bestTA.upperRight[Ys]+")");
							if(tagArea(bestTA,true)) {
								lastTagged = bestTA;
							}
							else {
								System.out.println("Could not tag area");
								bestTA.tagged = OTHERROBOT;
							}
							
						}
						else {	//No best TA could be found
							System.out.println("No best TA could be found");
						}
					}	
					
				}
}
			active = false;
			try {
				robotServer.play(7);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public synchronized void connectPilot(RmiServer rs) {
		robotServer = rs;
		mapConn = new MapServerManagement();
	}
	
	
	public synchronized void shutdownPilot() {
		System.out.println("Closing awesomepilot");
		active = false;
		mapConn.disconnect();
		
		System.exit(0);
		System.out.println("Awesomepilot closed down");
	}
	
	public void initialize() {
		currentPosition = STARTING_POSITION;
		mapConn.updateMap(currentPosition[Xs],currentPosition[Ys], ROBOT, 1);
		
	}
	
	public ArrayList<Point> explore(TaggedArea ta) {
		boolean savedPos = false;
		int pos[] = new int[2];
		boolean wentForward = true;
		boolean wentSide = false;
		int robotDirections[] = updateRobotDirections();
		int initalRobotDirections[] = updateRobotDirections();
		int dirs[] = {RIGHT,LEFT};
		//Adding our startposition as exitpoint
		if(!(ta.exitPoints.contains(new Point(currentPosition[Xs],currentPosition[Ys])))) {
			ta.exitPoints.add(new Point(currentPosition[Xs],currentPosition[Ys]));
		}
		//Removing our startposition as entrypoint
		removeEntryPoints(currentPosition[Xs],currentPosition[Ys],ta);
		//One tile discovered
		ta.discoveredTiles++;
		//Measure distance from start position
		mapSides(ta,robotDirections);
		
		for (int dir = 0; dir <= 1; dir++) {
			while(wentForward || wentSide) {
				if(ableToWalk(initalRobotDirections[UP],ta)) {
					System.out.println("Going forward");
					headTo(initalRobotDirections[UP]);
					moveOneTile();
					robotDirections = updateRobotDirections();
					mapSides(ta,robotDirections);
					System.out.println("discovered tiles = " + ta.discoveredTiles);
					wentForward = true;
					wentSide = false;
				}
				else {
					wentForward = false;
					if(ableToWalk(initalRobotDirections[dirs[dir]],ta)) {
						System.out.println("Going sideways");
						if(!savedPos) {
							pos[Xs] = currentPosition[Xs];
							pos[Ys] = currentPosition[Ys];
							savedPos = true;
							System.out.println("savedPos: " + pos[Xs] + " " + pos[Ys]);
						}
						
						headTo(initalRobotDirections[dirs[dir]]);
						moveOneTile();
						robotDirections = updateRobotDirections();
						mapSides(ta,robotDirections);
						System.out.println("discovered tiles = " + ta.discoveredTiles);
						wentSide = true;
					}
					else {
						wentSide = false;
					}
				}
				
				if(ta.discoveredTiles == ta.height*ta.width) {
					ta.explored = true;
					System.out.println("Fully explored");
					break;
				}
				
			}
			System.out.println("Cannot go forward or sideways");
			if(dir == 0 && savedPos) {
				System.out.println("Navigating to savedPos: " + pos[Xs] + " " + pos[Ys]);
				
				fetchNewMap();	//Fetch new map so dijkstras will work properly
				if(!navigateToPoint(new Point(pos[Xs],pos[Ys]))) {
					System.out.println("Could not navigate to point");
				}
				headTo(initalRobotDirections[UP]);
			}
			
			//Reset variables for next loop
			wentForward = true;
			wentSide = false;
			
		}
		if(ta.discoveredTiles == ta.height*ta.width) {
			ta.explored = true;
			System.out.println("Fully explored");
		}
		
		System.out.println("Done exploring");
		return ta.exitPoints;
	}
	
	public void mapSides(TaggedArea ta, int robotDirections[]) {
		int borderLoc;
		int dist;
		borderLoc = borderCheck(ta);
		if(borderLoc != NULL) {
			dist = checkDistance(borderLoc,false);
			updateMap(currentPosition,dist,borderLoc,ta,false);
		}
		else {
			dist = checkDistance(robotDirections[LEFT],false);
			updateMap(currentPosition,dist,robotDirections[LEFT],ta,false);
			dist = checkDistance(robotDirections[RIGHT],false);
			updateMap(currentPosition,dist,robotDirections[RIGHT],ta,false);
		}
	}
	
	//Implemented the 3-angle measuring and that the map overwriteable when dist = 0
	public boolean ableToWalk(int dir, TaggedArea ta) {
		int dist = Math.min(checkDistance(dir,true),borderDist(ta,dir));
		System.out.println("Distance to " + dir +" = " + dist);
		if(dist > 0) {
			updateMap(currentPosition, dist, dir, ta, false); //Overwrite disallowed 
			return true;
		}
		else {
			updateMap(currentPosition, dist, dir, ta, true); //Overwrite allowed
			return false;
		}
	}
	
	public int borderCheck(TaggedArea ta) {
		int dir = NULL;
		if(currentHeading == UP || currentHeading == DOWN) {
			if(currentPosition[Xs] == ta.lowerLeft[Xs]) {
				dir = RIGHT;
			}
			else if(currentPosition[Xs] == ta.upperRight[Xs]) {
				dir = LEFT;
			}
		}
		else {
			if(currentPosition[Ys] == ta.lowerLeft[Ys]) {
				dir = UP;
			}
			else if(currentPosition[Ys] == ta.upperRight[Ys]) {
				dir = DOWN;
			}		
		}
		return dir;
	}
	
	public int[] updateRobotDirections() {
		int robotDirs[] = {UP,RIGHT,DOWN,LEFT};
		switch(currentHeading) {
			case UP:
				break;
			case RIGHT:
				robotDirs[UP] = RIGHT;
				robotDirs[RIGHT] = DOWN;
				robotDirs[DOWN] = LEFT;
				robotDirs[LEFT] = UP;
				break;
			case DOWN:
				robotDirs[UP] = DOWN;
				robotDirs[RIGHT] = LEFT;
				robotDirs[DOWN] = UP;
				robotDirs[LEFT] = RIGHT;
				break;
			case LEFT:
				robotDirs[UP] = LEFT;
				robotDirs[RIGHT] = UP;
				robotDirs[DOWN] = RIGHT;
				robotDirs[LEFT] = DOWN;
				break;
		}
		return robotDirs;
	}
	
	public void updateMap(int pos[], int dist, int dir, TaggedArea ta, boolean overwrite) {
		int i;
		int borderDist = borderDist(ta,dir);
		int wallPos[] = new int[2];	
		
		switch(dir) {
			case UP:
				if(dist < borderDist) {
					wallPos[Xs] = pos[Xs];
					wallPos[Ys] = pos[Ys]+dist+1;
				}
				
				for(i = pos[Ys]+1; i <= (pos[Ys]+dist+1) && i <= (pos[Ys]+borderDist); i++) {
					if(i == wallPos[Ys]) {
						updateLocalAndGlobalMap(pos[Xs], i, WALL, ta, overwrite);
					}
					else {
						if(isBorderTile(pos[Xs], i, ta)) {
							if(!(ta.exitPoints.contains(new Point(pos[Xs],i)))) {
								ta.exitPoints.add(new Point(pos[Xs],i));
							}
						}
						updateLocalAndGlobalMap(pos[Xs], i, FLOOR, ta, overwrite);
					}
				}
				break;
			case RIGHT:
				if(dist < borderDist) {
					wallPos[Xs] = pos[Xs]+dist+1;
					wallPos[Ys] = pos[Ys];
				}
				
				for(i = pos[Xs]+1; i <= (pos[Xs]+dist+1) && i <= (pos[Xs]+borderDist); i++) {
					if(i == wallPos[Xs]) {
						updateLocalAndGlobalMap(i, pos[Ys], WALL, ta, overwrite);
					}
					else {
						if(isBorderTile(i, pos[Ys], ta)) {
							if(!(ta.exitPoints.contains(new Point(i, pos[Ys])))) {
								ta.exitPoints.add(new Point(i, pos[Ys]));
							}
						}
						updateLocalAndGlobalMap(i, pos[Ys], FLOOR, ta, overwrite);
					}
				}
				break;
			case DOWN:
				if(dist < borderDist) {
					wallPos[Xs] = pos[Xs];
					wallPos[Ys] = pos[Ys]-dist-1;
				}
				
				for(i = pos[Ys]-1; i >= (pos[Ys]-dist-1) && i >= (pos[Ys]-borderDist); i--) {
					if(i == wallPos[Ys]) {
						updateLocalAndGlobalMap(pos[Xs], i, WALL, ta, overwrite);
					}
					else {
						if(isBorderTile(pos[Xs], i, ta)) {
							if(!(ta.exitPoints.contains(new Point(pos[Xs], i)))) {
								ta.exitPoints.add(new Point(pos[Xs], i));
							}
						}
						updateLocalAndGlobalMap(pos[Xs], i, FLOOR, ta, overwrite);
					}
				}
				break;
			case LEFT:
				if(dist < borderDist) {
					wallPos[Xs] = pos[Xs]-dist-1;
					wallPos[Ys] = pos[Ys];
				}
				
				for(i = pos[Xs]-1; i >= (pos[Xs]-dist-1) && i >= (pos[Xs]-borderDist); i--) {
					if(i == wallPos[Xs]) {
						updateLocalAndGlobalMap(i, pos[Ys], WALL, ta, overwrite);
					}
					else {
						if(isBorderTile(i, pos[Ys], ta)) {
							if(!(ta.exitPoints.contains(new Point(i, pos[Ys])))) {
								ta.exitPoints.add(new Point(i, pos[Ys]));
							}
						}
						updateLocalAndGlobalMap(i, pos[Ys], FLOOR, ta, overwrite);	
					}
				}
				break;
		}
	}
	
	//Added overwrite mode
	public void updateLocalAndGlobalMap(int x, int y, int data, TaggedArea ta, boolean overwrite) {
		if(ta.localMap[x-ta.lowerLeft[Xs]][y-ta.lowerLeft[Ys]] == 0 || overwrite) {
			if(ta.localMap[x-ta.lowerLeft[Xs]][y-ta.lowerLeft[Ys]] == 0) {
				ta.discoveredTiles++;
			}
			mapConn.updateMap(x,y,data,1);
			removeEntryPoints(x,y,ta);
			ta.localMap[x-ta.lowerLeft[Xs]][y-ta.lowerLeft[Ys]] = data;
			
			//Paint corner if needed
			paintCorners(x,y,ta);
		}
	}
	
	public void removeEntryPoints(int x, int y, TaggedArea ta) {
		Point[] posArray = new Point[4];
		posArray[0] = new Point(x+1,y);
		posArray[1] = new Point(x-1,y);
		posArray[2] = new Point(x,y+1);
		posArray[3] = new Point(x,y-1);
		for(int j = 0; j < 4; j++) {
			if(ta.entryPoints.contains(posArray[j])) {
				ta.entryPoints.remove(posArray[j]);
			}
		}
	}
	
	public boolean isBorderTile(int x, int y, TaggedArea ta) {
		return x == ta.lowerLeft[Xs] || x == ta.upperRight[Xs] ||
		y == ta.lowerLeft[Ys] || y == ta.upperRight[Ys];
	}
	
	//Returns the number of tiles remaining from currentposition to any border
	public int borderDist(TaggedArea ta, int dir) {
		int result = 0;
		switch(dir) {
			case DOWN:
				result = currentPosition[Ys] - ta.lowerLeft[Ys];
				break;
			case LEFT:
				result = currentPosition[Xs] - ta.lowerLeft[Xs];
				break;
			case UP:
				result = ta.upperRight[Ys] - currentPosition[Ys];
				break;
			case RIGHT:
				result = ta.upperRight[Xs] - currentPosition[Xs];
				break;
		}
		return result;
	}
	
	//Measures 3 times in 3 different angles for checkInTripleAngles = true and returns the smallest value, 
	//otherwise in just one angle
	public int checkDistance(int dir, boolean checkInTripleAngels) {
		int dist[] = new int[3];
		int degrees = 0;
		try {
			if(dir != currentHeading) {
				switch(dir) {
					case UP : 
						switch(currentHeading) {
							case DOWN : degrees = 180; break;
							case LEFT : degrees = 90; break;
							case RIGHT : degrees = -90; break;
						} 
						break;
					case LEFT : 
						switch(currentHeading) {
							case DOWN : degrees = 90; break;
							case RIGHT : degrees = 180; break;
							case UP : degrees = -90; break;
						} 
						break;
					case RIGHT : 
						switch(currentHeading) {
							case DOWN : degrees = -90; break;
							case LEFT : degrees = 180; break;
							case UP : degrees = 90; break;
						} 
						break;
					case DOWN : 
						switch(currentHeading) {
							case UP : degrees = 180; break;
							case LEFT : degrees = -90; break;
							case RIGHT : degrees = 90; break;
						} 
						break;
				}
			}
			System.out.println("degrees = " +degrees);
			if(checkInTripleAngels) {
				robotServer.sensorRotate(degrees-18);
				dist[0] = robotServer.getDistance(); //Measure in -18 degree angle
				robotServer.sensorRotate(degrees);
				dist[1] = robotServer.getDistance(); //Measure in 0 degree angle
				robotServer.sensorRotate(degrees+18);
				dist[2] = robotServer.getDistance(); //Measure in 18 degree angle
				
				dist[0] = Math.min(dist[0],dist[1]);
				dist[0] = Math.min(dist[0],dist[2]); //Calculate the minimum distance
			}
			else {
				robotServer.sensorRotate(degrees);
				dist[0] = robotServer.getDistance();
			}
			robotServer.sensorRotate(0);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		
		//Special case to give robot more space
		if(dist[0] < (Math.round(TILE_SIZE*0.5) + 5)) {
			return 0;
		}
		else {
			//Returns 1 tile for 1.5*TILE_SIZE, 2 tiles for 2.5*TILE_SIZE cm...
			return (int)Math.floor(((dist[0]-(TILE_SIZE/2))/TILE_SIZE));
		}
		
	}
	
	public void fetchNewMap() {
		map = mapConn.getMap(1);
	}
	
	public void updateRobotPos(int[] oldPos, int[] newPos) {
		if(oldPos[Xs] < newPos[Xs]) {
			for(int i=newPos[Ys]-2; i<=newPos[Ys]+2; i++) {
				mapConn.updateMap(oldPos[Xs]+3, i, 4,1);
			}
			for(int i=newPos[Ys]-2; i<=newPos[Ys]+2; i++) {
				mapConn.updateMap(oldPos[Xs]-2, i, 2,1);
			}
		}
		else if(oldPos[Xs] > newPos[Xs]) {
			for(int i=newPos[Ys]-2; i<=newPos[Ys]+2; i++) {
				mapConn.updateMap(oldPos[Xs]-2, i, 4,1);
			}
			for(int i=newPos[Ys]-2; i<=newPos[Ys]+2; i++) {
				mapConn.updateMap(oldPos[Xs]+3, i, 2,1);
			}
		}
		else if(oldPos[Ys] < newPos[Ys]) {
			for(int i=newPos[Xs]-2; i<=newPos[Xs]+2; i++) {
				mapConn.updateMap(i,oldPos[Ys]+3, 4,1);
			}
			for(int i=newPos[Xs]-2; i<=newPos[Xs]+2; i++) {
				mapConn.updateMap(i,oldPos[Ys]-2, 2,1);
			}
		}
		else if(oldPos[Ys] > newPos[Ys]) {
			for(int i=newPos[Xs]-2; i<=newPos[Xs]+2; i++) {
				mapConn.updateMap(i,oldPos[Ys]-2, 4,1);
			}
			for(int i=newPos[Xs]-2; i<=newPos[Xs]+2; i++) {
				mapConn.updateMap(i,oldPos[Ys]+3, 2,1);
			}
		}
	}
	
	public boolean pointIsInArea(Point pos,Point lowerLeft,Point upperRight) {
		if(pos == null) {
			return false;
		}
		else {
			return pos.x >= lowerLeft.x && pos.x <= upperRight.x && pos.y >= lowerLeft.y && pos.y <= upperRight.y; 	
		}
	}

	
	public void updateMapWithReadings() {
		
		for(int j=0; j<=3; j++) {
			
			for(int i=1; i <= Math.floor(Math.min(currentUltraDistances[j]/TILE_SIZE,SENSOR_MAX_DISTANCE/TILE_SIZE)); i++) {
				//Draw all ground that's been found
				switch(j) {
					case UP : mapConn.updateMap(currentPosition[Xs], currentPosition[Ys]+i, 1, 1); break;
					case DOWN : mapConn.updateMap(currentPosition[Xs], currentPosition[Ys]-i, 1, 1); break;
					case LEFT : mapConn.updateMap(currentPosition[Xs]-i, currentPosition[Ys], 1, 1); break;
					case RIGHT : mapConn.updateMap(currentPosition[Xs]+i, currentPosition[Ys], 1, 1); break;
				}
			}

			//Draw any walls that have been discovered
			if(currentUltraDistances[j] < SENSOR_MAX_DISTANCE) {
				switch(j) {
					case UP : mapConn.updateMap(currentPosition[Xs], currentPosition[Ys]+((int)Math.floor(currentUltraDistances[UP]/TILE_SIZE)+1), 2, 1); break;
					case DOWN : mapConn.updateMap(currentPosition[Xs], currentPosition[Ys]-((int)Math.floor(currentUltraDistances[DOWN]/TILE_SIZE)+1), 2, 1); break;
					case LEFT : mapConn.updateMap(currentPosition[Xs]-((int)Math.floor(currentUltraDistances[LEFT]/TILE_SIZE)+1), currentPosition[Ys], 2, 1); break;
					case RIGHT : mapConn.updateMap(currentPosition[Xs]+((int)Math.floor(currentUltraDistances[RIGHT]/TILE_SIZE))+1, currentPosition[Ys], 2, 1); break;
				}
			}
		}
	}
	
	public boolean tagArea(TaggedArea tA, boolean paintEnabled) {
		
		
		//Fetch new map from mapserver
		fetchNewMap();
		boolean ableToTag = true;
		for(int x=tA.lowerLeft[Xs]; x<=tA.upperRight[Xs]; x++) {
			for(int y=tA.lowerLeft[Ys]; y<=tA.upperRight[Ys]; y++) {
				//if(map[x][y] == OTHERROBOT || map[x][y] == OCCUPIED) {
				if(map[x][y] != UNEXPLORED && map[x][y] != ROBOT) {
					ableToTag = false;
				} 

		
			}
		}
		if(ableToTag && paintEnabled) {
			mapConn.updateMapRectangle(tA.lowerLeft, tA.upperRight, TAGGED, 1);
			//Mark as tagged
			tA.tagged = ROBOT;
		}
		
		
		
		return ableToTag;
	}

	
	public int[] findClosestPoint( int pos[],ArrayList<int[]> list) {
		int[] closestPoint = list.remove(1);
		int closestDistance = (int)Math.round(Math.sqrt(Math.pow((pos[Xs] - closestPoint[Xs]),2) + Math.pow((pos[Ys] - closestPoint[Ys]),2)));
		int[] point;
			
		for(int i = 0; i<list.size(); i++) {
			point = list.get(i);
			
			if((int)Math.round(Math.sqrt(Math.pow((pos[Xs] - point[Xs]),2) + Math.pow((pos[Ys] - point[Ys]),2))) < closestDistance) {
				closestPoint = point;
			}
		}
		return closestPoint;
	}
	
	public void checkSurroundnings() {
		
		try {
				
			switch(currentHeading) {
				case UP : 
					currentUltraDistances[UP] = robotServer.getDistance();
					robotServer.sensorRotate(90);
					currentUltraDistances[LEFT] = robotServer.getDistance();
					robotServer.sensorRotate(90);
					currentUltraDistances[DOWN] = robotServer.getDistance();
					robotServer.sensorRotate(-270);
					currentUltraDistances[RIGHT] = robotServer.getDistance();
					robotServer.sensorRotate(90);
					break;
				case DOWN : 
					currentUltraDistances[DOWN] = robotServer.getDistance();
					robotServer.sensorRotate(90);
					currentUltraDistances[RIGHT] = robotServer.getDistance();
					robotServer.sensorRotate(90);
					currentUltraDistances[UP] = robotServer.getDistance();
					robotServer.sensorRotate(-270);
					currentUltraDistances[LEFT] = robotServer.getDistance();
					robotServer.sensorRotate(90);
					break;
				case LEFT :
					currentUltraDistances[LEFT] = robotServer.getDistance();
					robotServer.sensorRotate(90);
					currentUltraDistances[DOWN] = robotServer.getDistance();
					robotServer.sensorRotate(90);
					currentUltraDistances[RIGHT] = robotServer.getDistance();
					robotServer.sensorRotate(-270);
					currentUltraDistances[UP] = robotServer.getDistance();
					robotServer.sensorRotate(90);
					break;
				case RIGHT :
					currentUltraDistances[RIGHT] = robotServer.getDistance();
					robotServer.sensorRotate(90);
					currentUltraDistances[UP] = robotServer.getDistance();
					robotServer.sensorRotate(90);
					currentUltraDistances[LEFT] = robotServer.getDistance();
					robotServer.sensorRotate(-270);
					currentUltraDistances[DOWN] = robotServer.getDistance();
					robotServer.sensorRotate(90);
					break;
			}
			
			System.out.println("UP=" + currentUltraDistances[UP] + " LEFT=" + currentUltraDistances[LEFT] + " RIGHT=" + currentUltraDistances[RIGHT] + " DOWN=" + currentUltraDistances[DOWN]);
			System.out.println("#UP=" +Math.round(Math.min(currentUltraDistances[UP]/TILE_SIZE,SENSOR_MAX_DISTANCE/TILE_SIZE)) + " #LEFT=" + Math.round(Math.min(currentUltraDistances[LEFT]/TILE_SIZE,SENSOR_MAX_DISTANCE/TILE_SIZE)) + " #RIGHT=" + Math.round(Math.min(currentUltraDistances[RIGHT]/TILE_SIZE,SENSOR_MAX_DISTANCE/TILE_SIZE)) + " #DOWN=" + Math.round(Math.min(currentUltraDistances[DOWN]/TILE_SIZE,SENSOR_MAX_DISTANCE/TILE_SIZE)));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void moveOneTile() {
		if (!active) {
			shutdownPilot();
		}
		
		try {
			robotServer.moveForward(TILE_SIZE_IN_STEPS);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		//Save old position
		int[] oldPosition = {currentPosition[Xs],currentPosition[Ys]};
		
		//Update position
		currentPosition[Xs] += (currentHeading % 2) * -(currentHeading-2);
		currentPosition[Ys] += ((currentHeading+1) % 2) * -(currentHeading-1);
		
		//Update the robot's position in the map
		mapConn.updateMap(oldPosition[Xs],oldPosition[Ys], FLOOR, 1);
		mapConn.updateMap(currentPosition[Xs],currentPosition[Ys], ROBOT, 1);
	}
	
	
	public void headTo(int dir) {
		if(dir != currentHeading) {
			
			try {
				switch(dir) {
					case UP : 
						switch(currentHeading) {
							case DOWN : robotServer.rotate(180); break;
							case LEFT : robotServer.rotate(-90); break;
							case RIGHT : robotServer.rotate(90); break;
						}; break;
					case LEFT : 
						switch(currentHeading) {
							case DOWN : robotServer.rotate(-90); break;
							case RIGHT : robotServer.rotate(180); break;
							case UP : robotServer.rotate(90); break;
						}; break;
					case RIGHT : 
						switch(currentHeading) {
							case DOWN : robotServer.rotate(90); break;
							case LEFT : robotServer.rotate(180); break;
							case UP : robotServer.rotate(-90); break;
						}; break;
					case DOWN : 
						switch(currentHeading) {
							case UP : robotServer.rotate(180); break;
							case LEFT : robotServer.rotate(90); break;
							case RIGHT : robotServer.rotate(-90); break;
						}; break;
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		currentHeading = dir;	 
		}
	}
	
	public void goToCoordinate(int x, int y) {
		//Move in x-way
		if(x < currentPosition[Xs]) {
			headTo(LEFT);
		}
		else if(x > currentPosition[Xs]) {
			headTo(RIGHT);
		}	
		while(x != currentPosition[Xs]) {
			moveOneTile();
		}
		
		//Move in y-way
		if(y < currentPosition[Ys]) {
			headTo(DOWN);
		}
		else if(y > currentPosition[Ys]) {
			headTo(UP);
		}
		while(y != currentPosition[Ys]) {
			moveOneTile();
		}
	}
	
	public ArrayList<int[]> findPathTo(Point pos1, Point pos2) {
		
		//Path to return
		ArrayList<int[]> path = new ArrayList<int[]>();
	
		
		if(map[pos2.x][pos2.y] != FLOOR && map[pos2.x][pos2.y] != ROBOT && map[pos2.x][pos2.y] != OTHERROBOT ) { //Base case: target unexplored
			return null;
		}
		else if(pos1.equals(pos2)) {	//Base case: source = target
			System.out.println("SOurce = dest");
			int[] p = {pos1.x, pos1.y};
			path.add(p);
			return path;
		}
		
		class TileComparator implements Comparator<Tile> {
		    @Override
		    public int compare(Tile x, Tile y)
		    {
		    	if(x.dist < y.dist) {
		    		return -1;
		    	}
		    	else if(x.dist > y.dist){
		    		return 1;
		    	}
		    	return 0;
		    }
		}
		
		Tile[][] djiMap = new Tile[mapSize[Xs]][mapSize[Ys]];
		
		
		//Prio queue
		Comparator<Tile> comparator = new TileComparator();
        PriorityQueue<Tile> queue = new PriorityQueue<Tile>(mapSize[Xs]*mapSize[Ys], comparator);
		
        //Allocate map array and fill queue
		for(int i=0; i<mapSize[Xs]; i++) {
			for(int j=0; j<mapSize[Ys]; j++) {
				if(map[i][j] == FLOOR || map[i][j] == ROBOT ) {
					djiMap[i][j] = new Tile(i,j,map[i][j]);
					if(i == pos1.x && j == pos1.y) {
						//Distance to source = 0
						djiMap[pos1.x][pos1.y].dist = 0;
					}
					queue.add(djiMap[i][j]);
				}
			}
		}
		
		//Begin the awesomeness of Dijkstra
		 while (queue.size() != 0) {
			
			 Tile t = queue.poll();
			 //System.out.println("going through queue " + t.x + "," + t.y + " dist="+ t.dist + " data="+t.data );
			 if(t.dist == 500000) {	//No more tile is reachable
				 return null;
			 }
			 else if(t.x == pos2.x && t.y == pos2.y) { // Target is reached
				 break;
			 }
			 Tile[] neighbors = new Tile[4];
			 neighbors[LEFT] = djiMap[t.x-1][t.y];
			 neighbors[RIGHT] = djiMap[t.x+1][t.y]; 
			 neighbors[UP] = djiMap[t.x][t.y+1]; 
			 neighbors[DOWN] = djiMap[t.x][t.y-1]; 
			 for(int i=0; i<4; i++) {
				 if(neighbors[i] != null && neighbors[i].data != WALL && queue.contains(neighbors[i])) {
				     int candDist = t.dist+2;
				     
				     //Checks to see if the robots keeps the same heading, then decrease the cost by 1
				     if(t.previous != null) {
                            switch(i) {
                                case LEFT : 
                                    if(t.previous.x == neighbors[i].x+2) {
                                        candDist--;
                                    } break;
                                case UP : 
                                    if(t.previous.y == neighbors[i].y-2) {
                                        candDist--;
                                    } break;
                                case DOWN : 
                                    if(t.previous.y == neighbors[i].y+2) {
                                        candDist--;
                                    } break;
                                case RIGHT : 
                                    if(t.previous.x == neighbors[i].x-2) {
                                        candDist--;
                                    } break;
                            }
                     }
					 
					 if(candDist < neighbors[i].dist) {	 
						 queue.remove(neighbors[i]);
						 neighbors[i].dist = candDist;
						 neighbors[i].previous = t;
						 queue.add(neighbors[i]);
						 
					 }
					 
				 }
			 }
			
	     }
		 
		 //Backtrack to source
		
		 Tile u = djiMap[pos2.x][pos2.y];
		 //if()
		 while(u.previous != null) {
			 int[] wayPoint = {u.x,u.y};
			 path.add(0,wayPoint);
			 u = u.previous;
		 }
		 
		 return path;
	}
	
	public boolean navigateToPoint(Point point) {
		
		ArrayList<int[]> path = findPathTo(new Point(currentPosition[Xs],currentPosition[Ys]),point);
		
		if(path != null) {

			Iterator<int[]> i = path.iterator();
			while(i.hasNext()) {
				//System.out.println("following path");
				int[] p = (int[]) i.next();
				goToCoordinate(p[Xs],p[Ys]);
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	public Point findValidEntryPoint(TaggedArea tA) {
		//Iterate through the taggedArea's entrypoints
		Iterator<Point> entryIterator = tA.entryPoints.iterator();
		while(entryIterator.hasNext()) {
			Point entryPoint = (Point) entryIterator.next();
			//Loop until we find a reachable entrypoint
			//System.out.println("findPathTo(" + "("+currentPosition[Xs]+","+currentPosition[Ys]+")"+entryPoint.toString());
			if(findPathTo(new Point(currentPosition[Xs],currentPosition[Ys]),entryPoint) != null) {
				return entryPoint;
			}
		}
		return null;
	}
	
	public boolean enterTaggedArea(TaggedArea tA,Point entryPoint) {
		
		System.out.println("Trying to enter tagged area");
		if(tA == null || entryPoint == null) {
			return false;
		}
		
		if(navigateToPoint(entryPoint) == false) {
			return false;
		}
		
		//Check if we can enter area
		if(currentPosition[Xs] < tA.lowerLeft[Xs] && checkDistanceAndDrawWallIfPresent(RIGHT,tA) > 0 ) {
			headTo(RIGHT);
		}
		else if(currentPosition[Xs] > tA.upperRight[Xs]  && checkDistanceAndDrawWallIfPresent(LEFT,tA) > 0) {
			headTo(LEFT);
		}
		else if(currentPosition[Ys] > tA.upperRight[Ys]  && checkDistanceAndDrawWallIfPresent(DOWN,tA) > 0 ) {
			headTo(DOWN);
		}
		else if(currentPosition[Ys] < tA.lowerLeft[Ys]  && checkDistanceAndDrawWallIfPresent(UP,tA) > 0) {
			headTo(UP);
		}
		else {	//Could not enter area OR currentPosition possibly not correct
			tA.entryPoints.remove(entryPoint);	//Remove entryPoint
			return false;
		}
		
		tA.entryPoints.remove(entryPoint);	//Remove entryPoint
		//Enter area
		System.out.println("Entering tagged area");
		moveOneTile();
		return true;
		
	}
	
	public int checkDistanceAndDrawWallIfPresent(int dir, TaggedArea tA) {
		int dist = checkDistance(dir,false);
		if(dist == 0) {
			switch(dir) {
				case UP : updateLocalAndGlobalMap(currentPosition[Xs],currentPosition[Ys]+1,WALL,tA,false); break;
				case DOWN : updateLocalAndGlobalMap(currentPosition[Xs],currentPosition[Ys]-1,WALL,tA,false); break;
				case LEFT : updateLocalAndGlobalMap(currentPosition[Xs]-1,currentPosition[Ys],WALL,tA,false); break;
				case RIGHT : updateLocalAndGlobalMap(currentPosition[Xs]+1,currentPosition[Ys],WALL,tA,false); break;

			}	
		}
		return dist;
	}
	
	public void updateOwnershipTaggedAreas() {
		Iterator<TaggedArea> i = taggedAreas.iterator();
		while(i.hasNext()) {
			TaggedArea tA = (TaggedArea) i.next();
			
			//Kolla om det är taggat av annan robot
			if(tA.tagged != ROBOT && !tagArea(tA,false)) {
				tA.tagged = OTHERROBOT;
			}
		}
	}
	
	public void paintCorners(int x,int y,TaggedArea tA) {
		Point[] cornerTiles = new Point[4];
		Point[] sideTiles = new Point[4];
		cornerTiles[0] = new Point(x+1,y+1);
		cornerTiles[1] = new Point(x+1,y-1);
		cornerTiles[2] = new Point(x-1,y-1);
		cornerTiles[3] =new Point(x-1,y+1);
		
		sideTiles[0] = new Point(x,y+1);
		sideTiles[1] = new Point(x+1,y);
		sideTiles[2] = new Point(x,y-1);
		sideTiles[3] = new Point(x-1,y);
		
		for(int i=0; i<4; i++) {
			
			if(pointIsInArea(sideTiles[(i+1)%4],new Point(tA.lowerLeft[Xs],tA.lowerLeft[Ys]),new Point(tA.upperRight[Xs],tA.upperRight[Ys]))
					&& tA.localMap[ sideTiles[(i+1)%4].x -tA.lowerLeft[Xs]] [ sideTiles[(i+1)%4].y -tA.lowerLeft[Ys]] == TAGGED) {
				
				if(cornerTiles[i].x-tA.lowerLeft[Xs] > 0 && cornerTiles[i].x-tA.lowerLeft[Xs] < TAGGED_AREA_SIZE &&
						cornerTiles[i].y-tA.lowerLeft[Ys] > 0 && cornerTiles[i].y-tA.lowerLeft[Ys] < TAGGED_AREA_SIZE &&
						tA.localMap[cornerTiles[i].x-tA.lowerLeft[Xs]][cornerTiles[i].y-tA.lowerLeft[Ys]] == WALL && tA.localMap[sideTiles[i].x-tA.lowerLeft[Xs]][sideTiles[i].y-tA.lowerLeft[Ys]] != WALL) {
					
					updateLocalAndGlobalMap(sideTiles[(i+1)%4].x, sideTiles[(i+1)%4].y,WALL, tA, false);
				}
			}
			if(pointIsInArea(sideTiles[i],new Point(tA.lowerLeft[Xs],tA.lowerLeft[Ys]),new Point(tA.upperRight[Xs],tA.upperRight[Ys]))
					&& tA.localMap[ sideTiles[i].x -tA.lowerLeft[Xs]] [ sideTiles[i].y -tA.lowerLeft[Ys]] == TAGGED) {
				
				if(cornerTiles[i].x-tA.lowerLeft[Xs] > 0 && cornerTiles[i].x-tA.lowerLeft[Xs] < TAGGED_AREA_SIZE &&
						cornerTiles[i].y-tA.lowerLeft[Ys] > 0 && cornerTiles[i].y-tA.lowerLeft[Ys] < TAGGED_AREA_SIZE &&
						tA.localMap[cornerTiles[i].x-tA.lowerLeft[Xs]][cornerTiles[i].y-tA.lowerLeft[Ys]] == WALL && tA.localMap[sideTiles[(i+1)%4].x-tA.lowerLeft[Xs]][sideTiles[(i+1)%4].y-tA.lowerLeft[Ys]] != WALL) {	
					
					updateLocalAndGlobalMap(sideTiles[i].x, sideTiles[i].y,WALL, tA, false);
				}
			}
		}
	}
	
	public void clearMarkers(int type) {
		for(int i=0; i<mapSize[Xs]-1; i++) {
			for(int j=0; j<mapSize[Ys]-1; j++) {
				if(map[Xs][Ys] == type) {
					mapConn.updateMap(i, j, 0, 1);
				}
			}
		}
	}
}
