package test;

import serial.FailureException;
import serial.Serial;
import serial.TimeoutException;
import simu.Arduino;

public class LitSen extends Arduino {
	
	LitSen(int baudRate) {
		super(baudRate);
		// TODO Auto-generated constructor stub
	}


	public void setup() throws TimeoutException,FailureException
	{
		pinMode(A4,OUTPUT);
		pinMode(A5,OUTPUT);
	}
	
	int count=0;
	int steps=1000;
	int step=-5;
	
	public void loop() throws TimeoutException,FailureException
	{
		
		sendCommand("st m "+step);
	
		digitalWrite(A5,HIGH);
		digitalWrite(A4,LOW);
		int s1=analogRead(A0);
		
		digitalWrite(A5,LOW);
		digitalWrite(A4,HIGH);
		int s2=analogRead(A0);

		int angle=analogRead(A1);
		Serial.print(steps);
		Serial.print(", ");
		Serial.print(s1);
		Serial.print(", ");
		Serial.print(s2);
		Serial.print(", ");
		Serial.println(angle);
		
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		steps+=step;
		//System.out.printf("s=%d, %d, %d\n",s1,s2, angle);
	}
	
	
	public static void main(String[] args) {
		LitSen device = new LitSen(9600);
		if(device.connect("COM8")) 
			device.go(400);
			
	}

}
