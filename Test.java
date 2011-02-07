import lejos.nxt.*;

      public class Test {
        public static void main (String[] args) {
          System.out.println("Hello World");
          MehMeh test = new MehMeh();
            Motor.B.setSpeed(100);
            Motor.C.setSpeed(100);
            
            LCD.drawString("FORWARD", 0, 0);
            while (true) {
              test.forward();
              
              if (Button.ESCAPE.isPressed())
                break;
            }

        }
      }
