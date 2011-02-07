import lejos.nxt.*;

public class WallE {
  public static void main (String[] args) {
    
      rotateInPlace(100);
      Motor.A.rotate(180,true);
      
      // Start "game loop"
      while (true) {
        
        // Handle termination of program
        if (Button.ESCAPE.isPressed()) {
          break;
        }
      }
  }
  
  public static void rotateInPlace (int degree) {
    LCD.drawString("ROTATE IN PLACE", 0, 0);
    Motor.C.rotate(degree,true);
    Motor.B.rotate(-degree,true);
  }
}
