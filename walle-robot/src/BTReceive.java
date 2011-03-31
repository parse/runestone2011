import lejos.nxt.*;
import lejos.nxt.comm.*;
import lejos.robotics.navigation.Pilot;
import lejos.robotics.navigation.TachoPilot;
import java.io.*;

public class BTReceive {
	
	static boolean connected;
	
	public static void main(String[] args) throws Exception { 
		
		String connectedStr = "Connected";
        String waiting = "Waiting...";
        String closing = "Closing...";
		
        Sound.setVolume(100);
        
        Pilot pilot = new TachoPilot(2.145f, 2.145f, 6f, Motor.B, Motor.C, false);
        //Pilot pilot = new TachoPilot(5.4f, 5.4f, 15.5f, Motor.B, Motor.C, false);
        UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S1);
        Button.ESCAPE.addButtonListener(new myButtonListener());
        File rogerroger = new File("excellent.wav");
        
		while(true) {
		
			LCD.drawString(waiting,0,0);
			LCD.refresh();
	
	        BTConnection btc = Bluetooth.waitForConnection();
	        connected = true;
	        
			LCD.clear();
			LCD.drawString(connectedStr,0,0);
			Sound.twoBeeps();
			LCD.refresh();	
	
			DataInputStream dis = btc.openDataInputStream();
			DataOutputStream dos = btc.openDataOutputStream();
			        
	        while(connected) {
				
				int input;
				int deg;
				int command[] = new int[2];
				int i;
				
				for (i=0;i<command.length;i++) {
					command[i] = dis.readInt();
				}
				
				while(command[0] >= 1 && command[0] <= 8 || command[0] == 10) {
					switch(command[0]) {
						case 1: //rotate(int x)
							dos.writeInt(1); //Acking received command, "1"
							dos.flush();
							input = command[1];
							pilot.rotate(input);
							/*double cons = 2.82;
							deg = (int)Math.round(input*cons);
							Motor.C.rotate((deg),true);
							Motor.B.rotate(-deg);*/
							dos.writeInt(1); //Acking completed command, "1"
							dos.flush();
							break;
						case 2: //moveForward(int x)
							dos.writeInt(2); //Acking received command, "2"
							dos.flush();
							input = command[1];
							//pilot.setSpeed(900);
							pilot.travel(input);
							/*input = input * 21;
							Motor.C.setSpeed(400);
							Motor.B.setSpeed(400);
							Motor.C.rotate(input,true);
							Motor.B.rotate(input);*/
							dos.writeInt(1); //Acking completed command, "1"
							dos.flush();
							break;
						case 3: //sensorRotate(int x)
							dos.writeInt(3); //Acking received command, "3"
							dos.flush();
							input = command[1];
							//Motor.A.rotate(input);
							Motor.A.rotateTo(-input);
							dos.writeInt(1); //Acking completed command, "1"
							dos.flush();
							break;
						case 4: //getDistance()
							dos.writeInt(4); //Acking received command, "4"
							dos.flush();
							sonic.ping();
							//Sound.beep();
							dos.writeInt(sonic.getDistance());  //Acking distance
							dos.flush();
							break;
						case 5: //getBattery()
							dos.writeInt(5); //Acking received command, "5"
							dos.flush();
							dos.writeInt(Battery.getVoltageMilliVolt()); //Acking battery
							dos.flush();
							break;
						case 6: //strengthBT()
							dos.writeInt(6); //Acking received command, "6"
							dos.flush();
							dos.writeInt(btc.getSignalStrength()); //Acking signalstrength
							dos.flush();
							break;	
						case 7: //play()
							dos.writeInt(7); //Acking received command, "7"
							dos.flush();
							input = command[1];
							Sound.playSample(rogerroger,100);
							dos.writeInt(1); //Acking completed command, "1"
							dos.flush();
							break;
						case 8: //ping()
							dos.writeInt(8); //Acking received command, "8"
							dos.flush();
							dos.writeInt(1); //Acking completed command, "1"
							dos.flush();
							break;
						case 10: //kill()
							System.exit(0);
							break;
							
					}
					for (i=0; i<command.length; i++) {
						command[i] = dis.readInt();
					}
				}
				//shutdown()
				dos.writeInt(9); //Acking received command, "9"
				dos.flush();
				connected = false;
				dos.writeInt(1); //Acking completed command, "1"
				dos.flush();
				Sound.playSample(rogerroger,100);
				Thread.sleep(2000); // wait excellent to play
				
				dis.close();
				dos.close();
				Thread.sleep(100); // wait for data to drain
				LCD.clear();
				LCD.drawString(closing,0,0);
				LCD.refresh();
				btc.close();
				LCD.clear();
				Sound.beep();
			}
		}
	}
}

class myButtonListener implements ButtonListener{
	public void buttonPressed(Button b){
		Sound.beep();
		System.exit(0);
	}
	public void buttonReleased(Button b){
		
	}
}