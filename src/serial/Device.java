package serial;

import serial.TimeoutException;

public class Device extends CommPort{

	Device(int baudRate) {
		super(baudRate);
	}
	
	boolean connect(String portName) 
	{
		super.connect(portName);
		int count=0;
		int maxCount=30;
		while(count++<maxCount) {
			send("t\r");
			try {
				String resp;
				resp = getReceived();
				
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
		if(count==maxCount) {
			System.out.println("No echo from the device");
			return false;
		}
		else 
			System.out.println("Started");
		return true;
	}
	
	
	public String sendCommand(String command) throws TimeoutException,FailureException
	{
		clearReceived();
		send(command+"\r");
		
		String resp=getReceived();
		int timeCount=0;
		int delay=10;
		while (true) {
			int len=resp.length();
			if(len>=4) {
				String ending=resp.substring(len-4);
				if(ending.equals("OK\r\n")) {
					int pos=resp.indexOf(command);
					if(pos>=0) {
						int cmdlen=command.length();
						if(pos+cmdlen+1<len) {
							char c1=resp.charAt(pos+cmdlen);
							char c2=resp.charAt(pos+cmdlen+1);
							if(c1=='\r' && c2=='\n') {
								String rtn=resp.substring(pos+cmdlen+2,len-4);
								return rtn;
							}
							else {
								throw new FailureException();
							}
						}
					}
				}
				else if(ending.equals("!!\r\n")) {
					throw new FailureException(resp.substring(0, len-4));
				}
				else {
					if(timeCount>timeout) {
						throw new TimeoutException();
					}
					timeCount+=delay;
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					resp+=getReceived();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		Device device = new Device(9600);
		if(device.connect("COM8")) {
			int count=0;
			while(count++<30) {
				try {
					String resp = device.sendCommand("pm 18 o");
					System.out.println("a0="+resp);
					resp = device.sendCommand("ar a1");
					System.out.println("a1="+resp);
				} catch (TimeoutException e1) {
					e1.printStackTrace();
				} catch (FailureException e1) {
					e1.printStackTrace();
				}
			}
			device.disconnect();
		}
			
	}
}
