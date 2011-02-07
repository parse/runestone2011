import lejos.nxt.*;

      public class Test {
        public static void main (String[] args) {
          System.out.println("Hello World");
          MehMeh test = new MehMeh();
            //Motor.B.setSpeed(100);
            //Motor.C.setSpeed(100);
            
            LCD.drawString("FORWARD", 0, 0);
            rotateInPlace(100);
            Motor.A.rotate(180,true);
            while (true) {
               
              if (Button.ESCAPE.isPressed())
                break;
            }

        }
        
        public static void rotateInPlace (int degree) {
          Motor.C.rotate(degree,true);
          Motor.B.rotate(-degree,true);
        }
      }
