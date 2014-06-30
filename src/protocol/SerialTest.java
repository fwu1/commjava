package protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class SerialTest implements SerialPortEventListener {
	SerialPort serialPort;
	int timeout=0;
	
        /** The port we're normally going to use. */
	private static final String PORT_NAMES[] = { 
//			"/dev/tty.usbserial-A9007UX1", // Mac OS X
//                        "/dev/ttyACM0", // Raspberry Pi
//			"/dev/ttyUSB0", // Linux
			"COM8", // Windows
	};
	/**
	* A BufferedReader which will be fed by a InputStreamReader 
	* converting the bytes into characters 
	* making the displayed results codepage independent
	*/
	private BufferedReader input;
	/** The output stream to the port */
	private OutputStream output;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 3000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;

	private List <Byte> buff= new ArrayList <Byte>();
	
	public void initialize() {
                // the next line is for Raspberry Pi and 
                // gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
                System.setProperty("gnu.io.rxtx.SerialPorts", "COM8");

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			System.out.println("Port id:"+currPortId.getName());
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	public  void buffClear() {
		buff.clear();
	}
	
	public  void buffWrite(byte d)
	{
		//System.out.printf("{%c}", (char)d);
		buff.add(d);
	}

	public  byte buffRead()
	{
		byte d=buff.get(0);
		buff.remove(0);
		return d;
	}

	public String buffReadln(int timeout)
	{
		StringBuilder response= new StringBuilder();
		int delay=50;
		int timeoutCount=0;
		while(timeoutCount<timeout) {
			if(buffDataSize()>0) {
				byte dread=buffRead();
				//System.out.printf("[%d]\n",dread);
				if(dread==(byte)'\r')
					break;
				if(dread==(byte)'\n')
					continue;
				else {
					response.append((char)dread);
				}
			}
			else {
				try {
					Thread.sleep(delay);
					timeoutCount+=delay;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
		if(timeoutCount>=timeout) {
			System.out.printf("-----------Timeout: '%s' \n",response);
			return null;
		}
		return response.toString();
		
	}
	
	public  int buffDataSize()
	{
		return buff.size();
	}
	
	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				//String inputLine=input.readLine();
				//for(int i=0;i<inputLine.length();i++)
				//	buffWrite((byte)inputLine.charAt(i));
				
				int c;
				while ((c = input.read()) != -1) {
				    //Since c is an integer, cast it to a char. If it isn't -1, it will be in the correct range of char.
					buffWrite((byte)c);//
					//System.out.printf("[%c]", (char)c);
				}
				
				
			} catch (Exception e) {
				//System.out.println("ERROR");
				//System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}

	public void writeln(String txt) {
		
		try {
			for(int i=0;i<txt.length();i++) {
				output.write(txt.charAt(i));
				output.flush();
			}
			output.write('\r');
			output.flush();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public String sendCommand(String command) throws TimeoutException
	{
		buffClear();
		//System.out.printf("send commads '%s'\n",command);
		writeln(command);
		String line1=buffReadln(timeout);
		if(line1!=null) {
			//System.out.printf("line1=%s\n", line1);
			if(!line1.equals(command)) {
				System.out.printf("Waring, command echoed not mache:'%s'!='%s'\n", command,line1);
			}
			String line2=buffReadln(timeout);
			if(line2!=null) {
				//System.out.printf("line2=%s\n", line2);
				return line2;
			}
			//else 
			//	System.out.printf("line2 timeout\n");
		}
		//else
		//	System.out.printf("line1 timeout\n");
		throw new TimeoutException();
	}
	
	public void pinMode(String pin, String mode) throws TimeoutException,FailureException
	{
		String resp=sendCommand("pm "+pin+" o");
		if (!resp.equals("OK"))
			throw new FailureException();
	}
	
	public int analogRead(String pin) throws TimeoutException,FailureException
	{
		String resp=sendCommand("ar "+pin);
		String[] values = resp.split(" ");
		if(values.length==2 && values[1].equals("OK")) {
			int rst=Integer.parseInt(values[0]);
			return rst;
		}
		throw new FailureException();
	}
	
	
	public int lightValue(int id) throws FailureException{
		int SIZE=2;
		String[] pins ={"a4","a5"};
		String resp;
		try {
			for(int i=0;i<SIZE;i++) {
				String command="dw "+pins[i]+" "+((i==id)?"h":"l");
					//System.out.printf("command: %s\n",command);
					resp=sendCommand(command);
					if(!resp.equals("OK")) {
						System.out.printf("command failed");
						throw new FailureException();
					}
			}
			return analogRead("a0");
			
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		throw new FailureException();
	}
	
	public void scan() throws InterruptedException
	{
		int count=0;
		Thread.sleep(500);
		try {
			pinMode("a4","o");
			pinMode("a5","o");
			//Thread.sleep(2000);
			while(count++<10) {
				int pos=analogRead("a1");
				int light1=lightValue(0);
				int light2=lightValue(1);
	
				System.out.printf("%d,%d,%d,%d\n",count,pos,light1,light2);
				Thread.sleep(1000);
			}
		}
		catch (FailureException ex) {
			System.out.printf("Timeout\n");
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void go() throws InterruptedException
	{
		byte d=0;
		Thread.sleep(500);
		
		try {
			timeout=500;
			sendCommand("test");
		}
		catch (TimeoutException ex) {
			System.out.printf("Timeout\n");
		}

		try {
			timeout=3000;
			scan();
		}
		catch (InterruptedException ex) {
			System.out.printf("Timeout\n");
		}
	}

	
	public static void main(String[] args) throws Exception {
		SerialTest task = new SerialTest();
		task.initialize();
		Thread t=new Thread() {
			public void run() {
				//the following line will keep this app alive for 1000 seconds,
				//waiting for events to occur and responding to them (printing incoming messages to console).
				try {
					Thread.sleep(1000000);
				} catch (InterruptedException ie) {}
			}
		};
		t.start();

		
		if(task.output!=null) {
			task.go();
			/*
			int timeout=3000;
			int count=0;
			byte d=0;
			Thread.sleep(500);
			String resp=task.sendCommand("test",500);
			System.out.printf("resp=%s\n",resp);

			
			resp=task.sendCommand("pm a4 o",timeout);
			System.out.printf("resp=%s\n",resp);

			resp=task.sendCommand("pm a5 o",timeout);
			System.out.printf("resp=%s\n",resp);

			while (count<60) {
				
				Thread.sleep(100);
				resp=task.sendCommand("dw a4 "+ ((count%2==0)?"h":"l"),timeout);
				if(resp==null || !resp.equals("OK")) {
					System.out.printf("resp=%s\n",resp);
					continue;
				}

				resp=task.sendCommand("dw a5 "+ ((count%2==0)?"l":"h"),timeout);
				if(resp==null || !resp.equals("OK")){
					System.out.printf("resp=%s\n",resp);
					continue;
				}
				//System.out.printf("resp=%s\n",resp);
				
				resp=task.sendCommand("ar a0",timeout);
				if(resp==null){
					System.out.printf("resp=%s\n",resp);
					continue;
				}
				System.out.printf("******* resp of %d is '%s'\n",count,resp);

				resp=task.sendCommand("ar a1",timeout);
				if(resp==null){
					System.out.printf("resp=%s\n",resp);
					continue;
				}
				System.out.printf("******* position: '%s'\n",resp);

				resp=task.sendCommand("st m -20",timeout);
				if(resp==null){
					System.out.printf("resp=%s\n",resp);
					continue;
				}
				System.out.printf("******* position: '%s'\n",resp);
				resp=task.sendCommand("dw 10 l",timeout);
				resp=task.sendCommand("dw 11 l",timeout);
				
				
				Thread.sleep(600);
				count++;
			}
			*/
		}
		task.close();
		System.out.println("Ended");
	}
}
