package serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
 
public class CommPort implements SerialPortEventListener{
	String port;
	
	int baudRate;
	int dataBits = SerialPort.DATABITS_8;
	int stopBits = SerialPort.STOPBITS_1;
	int parity = SerialPort.PARITY_NONE;
	
	private static final int TIME_OUT = 3000;
	SerialPort serialPort;
	int timeout=5000;
	
	private InputStream input;
	private OutputStream output;
	
	private final Queue<Byte> receivedBuff = new ConcurrentLinkedQueue<Byte>();

	CommPort(int baudRate)
	{
		this.baudRate=baudRate;
	}
	
	boolean connect(String portName) 
	{
        // the next line is for Raspberry Pi and 
        // gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
        System.setProperty("gnu.io.rxtx.SerialPorts", "COM8");

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
		
		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			System.out.println("Port id:"+currPortId.getName());
			if (currPortId.getName().equals(portName)) {
				portId = currPortId;
				break;
			}
		}
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return false;
		}
		
		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);
		
			// set port parameters
			serialPort.setSerialPortParams(baudRate,dataBits,stopBits,parity);
		
			// open the streams
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();

		
			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
			return false;
		}
		
		return true;
	}
	
	boolean disconnect() 
	{
        try
        {

            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();
        }
        catch (Exception e)
        {
        	return false;
        }
		
		return true;
	}

	public void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				int c;
				synchronized (receivedBuff) {
					while ((c = input.read()) != -1) {
						receivedBuff.add((byte)c);
						//System.out.printf("[%c]", (char)c);
					}
				}
			} catch (Exception e) {
			}
		}
		
	}
	
	public void send(String txt) {
		try {
			for(int i=0;i<txt.length();i++) {
				output.write(txt.charAt(i));
			}
			output.flush();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void clearReceived() {
		receivedBuff.clear();
	}
	
	public String getReceived() throws TimeoutException{
		return getReceived(timeout);
	}
	
	public String getReceived(int timeout) throws TimeoutException{
		int time=0;
		int delay=50;
		
		StringBuilder response= new StringBuilder();
	
		while (time<timeout) {
			if(!receivedBuff.isEmpty()) {
				synchronized (receivedBuff) {
					while(!receivedBuff.isEmpty()) {
						byte ch=receivedBuff.poll();
						char c=(char)ch;
						//System.out.printf("[%c]", ch);
						response.append(c);
					}
				}
				return response.toString();
			}
			else {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				time+=delay;
			}
		}
		return "";
	}
	
/*	
	public static void main(String[] args) {
		CommPort port = new CommPort(9600);
		if(port.connect("COM8")) {
			int count=0;
			while(count++<30) {
				port.send("t\r");
				try {
					String resp=port.getReceived(100);
					if(resp.length()>0) {
						break;
					}
					else {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					//	System.out.println("init received :"+resp);
					}
				} catch (TimeoutException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			System.out.println("Start");
			count=0;
			while(count++<14) {
				port.send("ar a0\r");
				
				try {
					String resp=port.getReceived();
					System.out.println("received :"+resp);
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			port.disconnect();
		}
		
	}
*/	
}
