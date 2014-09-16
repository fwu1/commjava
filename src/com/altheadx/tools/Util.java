package com.altheadx.tools;

import java.io.*;
import java.util.Hashtable;
import java.util.List;

public class Util {

	// return a BufferedReader for a file in disk file system or inside a Jar file	if the first character of filename is '/'.
	// It the first character is not '/', only search in disk file system.
	
	public static BufferedReader getBufferedReaderFromDiskAndJar(String fileName) throws IOException {
		
    	BufferedReader br=null;
    	if(fileName.charAt(0)=='/') {
    		// get a inputstream for the file in Jar
    		InputStream in = (new Util()).getClass().getResourceAsStream(fileName);
			// if failed to read from jar file, remove the first character of file name and read directly from disk
    		if (in==null) 
    			fileName=fileName.substring(1);
    		else {
    			br = new BufferedReader(new InputStreamReader(in));
    			// set file name null, so there is no try from disk file
    			fileName=null;
    		}
    		
    	}
		// read file from disk 
		if (fileName!=null) {
			Logger.debug("Load data","read disk file '%s'",fileName);
			br = new BufferedReader(new FileReader(fileName));
		}
		
		return br;
	}

	public static BufferedReader getBufferedReaderFromDiskAndJar_old(String fileName) throws IOException {
		
    	BufferedReader br;
		// read from file in jar
		InputStream in = (new Util()).getClass().getResourceAsStream(fileName);
		if (in==null) {
			//Logger.debug("Load data","got error for reading resource file '%s'",fileName);
			// remove the first character and read directly from disk
			fileName=fileName.substring(1);
			Logger.debug("Load data","read disk file '%s'",fileName);
			br = new BufferedReader(new FileReader(fileName));
		}
		else
			br = new BufferedReader(new InputStreamReader(in));
		
		return br;
	}
	

     public static InputStream getInputStreamFromDiskAndJar(String fileName) throws IOException {
    	Util jarpackage=new Util();
		InputStream in = jarpackage.getClass().getResourceAsStream(fileName);
		if (in==null) {
			//Logger.debug("Load data","got error for reading resource file '%s'",fileName);
			// remove the first character and read directly from disk
			fileName=fileName.substring(1);
			Logger.debug("Load data","read disk file '%s'",fileName);
			in = new FileInputStream(fileName);
		}
		return in;
    }
    
     
    public static byte[] readFile(String fileName) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(new File(fileName), "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }
    
 
    
    public static byte[] readFileFromDiskAndJar(String fileName) throws IOException {
    	InputStream inStream=getInputStreamFromDiskAndJar(fileName);
		// Get the size of the file
		long streamLength = inStream.available();
	
		if (streamLength > Integer.MAX_VALUE) {
			// File is too large
			return null;
		}
	
		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) streamLength];
	
		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = inStream.read(bytes,
						offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}
	
		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file ");
		}
	
		// Close the input stream and return bytes
   		inStream.close();
   		return bytes;
   }
    
    public static String list2String(String [] list,String delimiter)
    {
		String str="";
		boolean first=true;
		for(String name:list) {
			if (first) 
				first=false;
			else
				str+=delimiter;
			str+=name;
		}
    	return str;
    }
    
    public static String list2String(List <String> list,String delimiter)
    {
		String str="";
		boolean first=true;
		for(String name:list) {
			if (first) 
				first=false;
			else
				str+=delimiter;
			str+=name;
		}
    	return str;
    }
    
	static Hashtable <String,Integer> monthnumber=null;
	static int getMonthNumber(String month) throws Exception
	{
		String [] monthNames={"jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec"};
		if(monthnumber==null) {
			monthnumber =  new Hashtable <String,Integer>();
			int cnt=1;
			for(String amonth:monthNames) 
				monthnumber.put(amonth.toLowerCase(), cnt++);
		}
		Integer im=monthnumber.get(month.toLowerCase());
		if(im!=null)
			return im;
		else
			throw new Exception("wrong month name");
	}

	// Convert date in format "Nov 14, 2013" to "11/24/2013"
	static String convertDate1(String date) throws Exception{
		String [] md_y=date.split(", ");
		if(md_y.length!=2) {
			throw new Exception("Wrong date format: "+date);
		}
		String [] md=md_y[0].split(" ");
		
		if(md.length!=2) {
			throw new Exception("Wrong date format: "+date);
		}
		
		int im=	getMonthNumber(md[0].toLowerCase());
		return ""+im+"/"+md[1]+"/"+md_y[1];
	}
	
	// Convert date in format "20-Oct-55" to "10/20/1955"
	static String convertDate0(String date) throws Exception{
		String [] dmy=date.split("-");
		if(dmy.length!=3) {
			throw new Exception("Wrong date format: "+date);
		}
		int im=	getMonthNumber(dmy[1].toLowerCase());
		int iy= Integer.parseInt(dmy[2]);
		if(iy<30)
			iy=2000+iy;
		else
			iy=1900+iy;
		return ""+im+"/"+dmy[0]+"/"+iy;
	}
    
	public static String convertDate(String date) throws Exception{
		try {
			return convertDate0(date);
		}
		catch (Exception ex) {
			return convertDate1(date);
		}
	}
	
	/*  When a decimal number has character '0' the end, Excel will remove the chacracter '0' since it's data type is number 
	 *  To avoid the character '0' to be removed, we need to insert a non-digital number in the front of number, so Excel takes
	 *  it as text, instead of a number. The character we selected to insert is '~'
	 *  
	 *  This function removes character '~' in the front 
	 */
    public static String digitalString(String text)
    {
		if(text.length()>1 && text.charAt(0)=='~')
			return text.substring(1);
		else
			return text;
    }
    
    // Get a name form full name, 
    // return 
    // first name when type=1;
    // last name when type=2;
    // middle name when type=3;
    
    public static String getAName(String fullName,int type) {
    	String [] names=fullName.split(" ");
    	if(type==1)
    		return names[0];
    	else if(type==2 && names.length>=2) 
    		return names[names.length-1];
    	else if (type==3 && names.length>=3) {
    		String middleName="";
    		for(int i=1;i<names.length-1;i++) {
    			if(names[i].length()==0)
    				continue;
    			if (middleName.length()==0)
    				middleName=names[i].replace(" ", "");
    			else
    				middleName+=" "+names[i].replace(" ", "");
    		}
    		return middleName;
    	}
    	return "";
    }
    
    public static void copyFile(File source, File dest) throws IOException {
    	InputStream input = null;
    	OutputStream output = null;
    	try {
    		input = new FileInputStream(source);
    		output = new FileOutputStream(dest);
    		byte[] buf = new byte[1024];
    		int bytesRead;
    		while ((bytesRead = input.read(buf)) > 0) {
    			output.write(buf, 0, bytesRead);
    		}
    	} finally {
    		input.close();
    		output.close();
    	}
    }    
}