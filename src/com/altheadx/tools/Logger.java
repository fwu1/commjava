package com.altheadx.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;


public class Logger {
	private static Logger logger=null;
	public List <Log> logs = new ArrayList <Log> ();
	private static List <String[]> packageShortNameList = new ArrayList <String[]> ();
	// list of filters. 
	private static List <LogFilter> labelFilters = new ArrayList <LogFilter> ();
	
	private static String logFileName=null;
	
	public static Logger logger() {
		if(logger==null) 
			logger=new Logger();
		return logger;
	}
	
	public static void addpackageShortName(String name,String shortName) {
		String[] pair= new String[2];
		pair[0]=name;
		pair[1]=shortName;
		packageShortNameList.add(pair);
	}
	
	public static String shortClassName(String className) {
		String shortName=className;
		for(String [] pair : packageShortNameList) {
			if(className.indexOf(pair[0]+".", 0)>=0) {
				// find the prefix
				shortName=className.substring(pair[0].length()+1,className.length());
				break;
			}
		}
		return shortName;
	}
	
	private static void addFilter_p(String className,String label,int type) {
		LogFilter filter= new LogFilter();
		filter.className=className;
		// get the name of caller class
		try {
			throw new Exception("");
		}
		catch( Exception e ) {
			StackTraceElement tr = e.getStackTrace()[2];
			if(className==null || className.equals(LogFilter.callerClassName)) {
				String codeClassName=tr.getClassName(); 
				filter.className=shortClassName(codeClassName);
			}
			else {
				filter.className=className;
			}
			
			if(label==null || label.equals(LogFilter.labelCallerMethodName))
				filter.label=tr.getMethodName();
			else
				filter.label=label;
		}
		filter.type=type;
		
		// don't duplicate a filter
		boolean exist=false;
		for(LogFilter a_filter:labelFilters) {
			if(a_filter.equals(filter))
				exist=true;
		}
		if(!exist)
			labelFilters.add(filter);
	}
	
	// filter with caller's class name, caller method as label and all log types
	public static void addFilter() {
		addFilter_p(LogFilter.callerClassName,LogFilter.labelCallerMethodName,LogFilter.allTypes);
	}
	
	
	// filter with caller's class name, caller method as label and type <type>
	public static void addFilter(int type) {
		addFilter_p(LogFilter.callerClassName,LogFilter.labelCallerMethodName,type);
	}

	// filter with caller's class name, label <label> and  all log types
	public static void addFilter(String label) {
		addFilter_p(LogFilter.callerClassName,label,LogFilter.allTypes);
	}
	
	// filter with  class name <className>, label <label> and  all log types
	public static void addFilter(String className, String label) {
		addFilter_p(className,label,LogFilter.allTypes);
	}

	// filter with  class name <className>, label <label> and  all log types
	public static void addFilter(String className, String label, int type) {
		addFilter_p(className,label,type);
	}

	public static void deleteFilter(String className, String label, int type) {
		if(className==null || label==null)
			return;
		//boolean doDelete=false;
		LogFilter filterToDelete=null;
		do { 
			for (LogFilter filter:labelFilters) {
				if(filter.className.equals(className) && filter.label.equals(label)) {
					if(filter.type==type || filter.type==LogFilter.allTypes) {
						filterToDelete=filter;
						break;
					}
				}
				if(filter.className.equals(className) && filter.type==type) {
					if(filter.label.equals(label) || filter.label.equals(LogFilter.allLabels)){
						filterToDelete=filter;
						break;
					}
				}
				if(filter.label.equals(label) && filter.type==LogFilter.allTypes) {
					if(filter.className.equals(className) || filter.className.equals(LogFilter.allClasses)) {
						filterToDelete=filter;
						break;
					}
				}
			}
			if (filterToDelete!=null) {
				labelFilters.remove(filterToDelete);
			}
			
		} while (filterToDelete!=null);
		
		addFilter_p(className,label,type);
	}
	
	public static void deleteAllFilters() 
	{
		labelFilters.removeAll(labelFilters);
	}
	
	public static void logc(String className,String label,int type, String errorText) {
		Log error= new Log();
		error.className=(className==null)?"":className;
		error.label=(label==null)?"":label;
		error.type=type;
		error.errorText=errorText;
		logger().logs.add(error);
	}

	public static void printf(String format, Object... args) {
		System.out.format(format, args);
	}

	public static void toFile(String fileName)
	{
		logFileName=fileName;
		File file =new File(logFileName);
		try {
			file.createNewFile();
			//true = append file
			FileWriter fileWritter = new FileWriter(file.getName(),false);
	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	        bufferWritter.write("");
	        bufferWritter.close();
		}
		catch (Exception ex) {
		
		}
	}
	
	public static void fprintf(String format, Object... args) {
		if(logFileName==null)
			return;
		String text = String.format(format, args);

		try {
			File file =new File(logFileName);
	
			//if file doesnt exists, then create it
			if(!file.exists()){
				file.createNewFile();
			}
	
			//true = append file
			FileWriter fileWritter = new FileWriter(file.getName(),true);
	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	        bufferWritter.write(text);
	        bufferWritter.close();
		}
		catch (Exception ex) {
		
		}
	}
		
	
	
	private static void log_p(String label,int type, String format, Object... args) {
			
		// get the name of caller class
		if(format==null)
			format="";
		
		try {
			throw new Exception("");
		}
		catch( Exception e ) {
			StackTraceElement tr = e.getStackTrace()[2];
			String className=shortClassName(tr.getClassName());
			
			if(label==null || label.equals(""))
				label=tr.getMethodName();
			
			boolean doFilting=false;
			for(LogFilter lf:labelFilters) {
				if(lf.className.equals(LogFilter.allClasses) || lf.className.equals(className)) {
					if(lf.label.equals(LogFilter.allLabels) || lf.label.equals(label)) {
						if(lf.type==LogFilter.allTypes || lf.type==type) {
							doFilting=true;
						}
					}
				}
			}
			
			if(!doFilting) {
				Log log= new Log();
				log.className=className+":"+tr.getLineNumber();
				log.label=label;
				log.type=type;
				log.errorText=String.format(format, args);
				logger().logs.add(log);
			}
		}
	}
	
	public static void log(String label,int type, String format, Object... args) {
		log_p(label, type, format, args);
	}
	
	public static void debug(String label, String format, Object... args) {
		log_p(label,Log.Debug,format,args);
	}
	
	public static void info(String label, String format, Object... args) {
		log_p(label,Log.Info,format,args);
	}

	public static void dataError(String label, String format, Object... args) {
		log_p(label,Log.DataError,format,args);
	}
	
	public static void dataWarning(String label, String format, Object... args) {
		log_p(label,Log.DataWarning,format,args);
	}

	public static void codeError(String label, String format, Object... args) {
		log_p(label,Log.CodeError,format,args);
	}
	
	public static void codeWarning(String label, String format, Object... args) {
		log_p(label,Log.CodeWarning,format,args);
	}

	public static void resError(String label, String format, Object... args) {
		log_p(label,Log.ResourceError,format,args);
	}
	
	public static void resWarning(String label, String format, Object... args) {
		log_p(label,Log.ResourceWarning,format,args);
	}
	
	public static void print() {
		
		for(Log error:logger().logs) {
			System.out.println(error);
		}
	}
	
	public static void print_inTypes() {
		//Show error messages
		System.out.println("\n ---- Error log messages ----");
		for(Log log:Logger.logger().logs) {
			if( log.type==Log.CodeError || log.type==Log.DataError || 
				log.type==Log.ResourceError || log.type==Log.AnalysisError ||
				log.type==Log.AlgorithmError) {
				System.out.println(log);
			}
		}
	
		//Show error messages
		System.out.println("\n ---- Warning log messages ----");
		for(Log log:Logger.logger().logs) {
			if( log.type==Log.CodeWarning || log.type==Log.DataWarning || 
				log.type==Log.ResourceWarning || log.type==Log.AnalysisWarning ) {
				System.out.println(log);
			}
		}
			
		//Show information messages
		System.out.println("\n ---- Information log messages ----");
		for(Log log:Logger.logger().logs) {
			if( log.type==Log.Info) {
				System.out.println(log);
			}
		}
			
			
		//Show debug messages
		System.out.println("\n ---- Debug log messages ----");
		for(Log log:Logger.logger().logs) {
			if( log.type==Log.Debug) {
				System.out.println(log);
			}
		}
	}
	
	public static void test()
	{
		Logger logger= new Logger();
		String className=logger.getClass().toString();
		Logger.logc(className,"Test",Log.CodeError,"Code error text");
		Logger.print();
	}
	
}
