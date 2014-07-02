package serial;

import serial.FailureException;
import serial.TimeoutException;

public class Arduino extends Device {

	static int HIGH = 0x1;
	static int LOW  = 0x0;

	static int INPUT = 0x0;
	static int OUTPUT = 0x1;
	static int INPUT_PULLUP = 0x2;

	static int SS   = 10;
	static int MOSI = 11;
	static int MISO = 12;
	static int SCK  = 13;

	static int SDA = 18;
	static int SCL = 19;
	static int LED_BUILTIN = 13;

	static int A0 = 14;
	static int A1 = 15;
	static int A2 = 16;
	static int A3 = 17;
	static int A4 = 18;
	static int A5 = 19;
	static int A6 = 20;
	static int A7 = 21;
	
	
	Arduino(int baudRate) {
		super(baudRate);
	}

	public void pinMode(String pin, String mode) throws TimeoutException,FailureException
	{
		sendCommand("pm "+pin+" "+mode);
	}

	public void pinMode(int pin, int mode) throws TimeoutException,FailureException
	{
		String szMode;
		if(mode==OUTPUT) szMode="o";
		else if(mode==INPUT) szMode="i";
		else throw new FailureException("Wrong pin mode:"+mode);
		
		sendCommand("pm "+pin+" "+szMode);
	}
	
	public void digitalWrite(int pin, int value) throws TimeoutException,FailureException
	{
		String szValue;
		if(value==HIGH) szValue="h";
		else if(value==LOW) szValue="l";
		else throw new FailureException("Wrong digital write value:"+value);
		
		sendCommand("dw "+pin+" "+szValue);
		
	}
	
	
	public int analogRead(String pin) throws TimeoutException,FailureException
	{
		String resp=sendCommand("ar "+pin);
		int rst=Integer.parseInt(resp);
		return rst;
	}

	public int analogRead(int pin) throws TimeoutException,FailureException
	{
		String resp=sendCommand("ar "+pin).trim();
		int rst=Integer.parseInt(resp);
		return rst;
	}

	public void setup() throws TimeoutException,FailureException
	{
		System.out.println("TODO: override setup");
	}

	public void loop() throws TimeoutException,FailureException
	{
		System.out.println("TODO: override loop");
	}
	
	public void go(int nofLoop)
	{
		try {
			setup();
			int count=0;
			while(nofLoop>0 && count++<nofLoop)
				loop();
		} catch (TimeoutException e1) {
			e1.printStackTrace();
		} catch (FailureException e1) {
			e1.printStackTrace();
		}
		disconnect();
	}
}
