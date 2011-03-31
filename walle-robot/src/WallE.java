import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;

public class WallE {
	public static void main (String[] args) {
		//rotateInPlace(1000);
		//Motor.A.rotate(360,true);
		//rotateUltra();
      
      
      	UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S1);

    /*while (!Button.ESCAPE.isPressed()) {
      LCD.clear();
      LCD.drawString(sonic.getVersion(), 0, 0);
      LCD.drawString(sonic.getProductID(), 0, 1);
      LCD.drawString(sonic.getSensorType(), 0, 2);
      LCD.drawInt(sonic.getDistance(), 0, 3);
      try{Thread.sleep(1000);}catch(Exception e) {}
    }*/
		// Start "game loop"
		while (true) {
			Motor.B.forward();
			Motor.C.forward();
			try{Thread.sleep(1000);}catch(Exception e) {}	//Sleep 1 sec
			Motor.B.stop();
			Motor.C.stop();
			
			//Check the distances in 3 directions and choose the way with the most space
			int a1, a2, a3 = 0;
			int b1, b2, b3 = 0;
			int c1, c2, c3 = 0;
			Motor.A.rotateTo(-90);
			try{Thread.sleep(1000);}catch(Exception e) {}
			a1 = sonic.getDistance(); //Should be replaced with one getDistances() that saves 8 values to an array
			a2 = sonic.getDistance();
			a3 = sonic.getDistance();
			a1 = (a1 + a2 + a3) / 3; //Mean value for more accuracy
			if(a1 < 83) {
				Sound.beep();
			}
			Motor.A.rotateTo(0);
			try{Thread.sleep(1000);}catch(Exception e) {}
			b1 = sonic.getDistance();
			b2 = sonic.getDistance();
			b3 = sonic.getDistance();
			b1 = (b1 + b2 + b3) / 3; //Mean value for more accuracy
			if(b2 < 83) {
				Sound.beep();
			}
			Motor.A.rotateTo(90);
			try{Thread.sleep(1000);}catch(Exception e) {}
			c1 = sonic.getDistance();
			c2 = sonic.getDistance();
			c3 = sonic.getDistance();
			c1 = (c1 + c2 + c3) / 3; //Mean value for more accuracy
			if(c3 < 83) {
				Sound.beep();
			}
			
			if(a1 > a2 && a1 > a3) { //Most space to the left, rotate left
				rotateInPlace(-251);
			}
			else if(a3 > a1 && a3 > a2) { //Most space to the right, rotate right
				rotateInPlace(251);
			}
        
        	// Handle termination of program
        	if (Button.ESCAPE.isPressed()) {
        		break;
        	}
		} 
		LCD.drawString("EXIT", 0, 0);
	}
	
	public static void rotateInPlace (int degree) {
    	LCD.drawString("ROTATE IN PLACE", 0, 0);
    	Motor.C.rotate(degree,true);
    	Motor.B.rotate(-degree); 
	}
	
	public static void driveForward (int degree) {
		LCD.drawString("DRIVE FORWARD", 0, 0);
		Motor.C.rotate(degree, true);
    	Motor.B.rotate(degree); 
	}
	
	public static void rotateUltra () {
		Motor.A.rotateTo(0);
		try{Thread.sleep(1000);}catch(Exception e) {}
		Motor.A.rotateTo(90);
		try{Thread.sleep(1000);}catch(Exception e) {}
		Motor.A.rotateTo(180);
		try{Thread.sleep(1000);}catch(Exception e) {}
		Motor.A.rotateTo(270);
		try{Thread.sleep(1000);}catch(Exception e) {}
		Motor.A.rotateTo(180);
		try{Thread.sleep(1000);}catch(Exception e) {}
		Motor.A.rotateTo(90);
		try{Thread.sleep(1000);}catch(Exception e) {}
		Motor.A.rotateTo(0);
		try{Thread.sleep(1000);}catch(Exception e) {}
	}
}
