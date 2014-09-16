package simu;

import com.altheadx.tools.DataTable;
import serial.FailureException;
import serial.TimeoutException;

public class Arduino  {

	public static int HIGH = 0x1;
	public static int LOW  = 0x0;

	public static int INPUT = 0x0;
	public static int OUTPUT = 0x1;
	public static int INPUT_PULLUP = 0x2;

	public static int SS   = 10;
	public static int MOSI = 11;
	public static int MISO = 12;
	public static int SCK  = 13;

	public static int SDA = 18;
	public static int SCL = 19;
	public static int LED_BUILTIN = 13;

	public static int A0 = 14;
	public static int A1 = 15;
	public static int A2 = 16;
	public static int A3 = 17;
	public static int A4 = 18;
	public static int A5 = 19;
	public static int A6 = 20;
	public static int A7 = 21;
	
	DataTable table;
	
	class Rec {
		int pos;
		int l1;
		int l2;
		int psen;
	}
	
	Rec[] data;
	
	int pos=0;  // set the initial stepper position
	int posMax;
	
	int nofReg=100;
	int[] regValues=new int[nofReg];
	
	public Arduino(int baudRate) {
		table = new DataTable();
		table.LoadFromCSV("sample1.csv");
		data = new Rec[table.rowSize];
		for(int row=0;row<table.rowSize;row++) {
			Rec rec = new Rec();
			rec.pos=Integer.parseInt(table.GetItem(row, 0));
			rec.l1=Integer.parseInt(table.GetItem(row, 1));
			rec.l2=Integer.parseInt(table.GetItem(row, 2));
			rec.psen=Integer.parseInt(table.GetItem(row, 3));
			posMax=rec.pos;
			data[row] = rec;
		}
		return;
	}

	public void pinMode(String pin, String mode) throws TimeoutException,FailureException
	{
		//sendCommand("pm "+pin+" "+mode);
	}

	public void pinMode(int pin, int mode) throws TimeoutException,FailureException
	{
		String szMode;
		if(mode==OUTPUT) szMode="o";
		else if(mode==INPUT) szMode="i";
		else throw new FailureException("Wrong pin mode:"+mode);
		
		//sendCommand("pm "+pin+" "+szMode);
	}
	
	public void digitalWrite(int pin, int value) throws TimeoutException,FailureException
	{
		regValues[pin]=value;
	}
	
	
	public int analogRead(String pin) throws TimeoutException,FailureException
	{
//		String resp=sendCommand("ar "+pin);
//		int rst=Integer.parseInt(resp);
		return 0;
	}

	public int analogRead(int pin) throws TimeoutException,FailureException
	{
		int idx=-1;
		for(int i=0;i<data.length;i++) {
			Rec r=data[i];
			if(pos==r.pos) {
				idx=i;
				break;
			}
			if(pos<r.pos)
				continue;
		}
		
		if(pin==A0) {
			
			if(idx>=0 && idx<data.length) {
				if(regValues[A4]==0)
					return data[idx].l1;
				else
					return data[idx].l2;
			}
		}
		else if(pin==A1) {
			if(idx>=0 && idx<data.length) {
				return data[idx].psen;
			}
		}
		return 0;
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
	}
	
	// Simue the Device class
	public String sendCommand(String cmd) {
		return cmd;
	}
	
	public boolean connect(String cmd) {
		return true;
	}
	
	public void stepperMove(int steps)
	{
		pos+=steps;
	}
	
}
