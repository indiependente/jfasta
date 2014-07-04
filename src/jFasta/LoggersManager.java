package jFasta;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggersManager {
	
	public static class LogFormatter extends Formatter 
	{
		//
	    // Create a DateFormat to format the logger timestamp.
	    //
	    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
	 
	    public String format(LogRecord record) {
	        StringBuilder builder = new StringBuilder(1000);
	        builder.append(df.format(new Date(record.getMillis()))).append(" - ");
	        builder.append("[").append(record.getSourceClassName()).append(".");
	        builder.append(record.getSourceMethodName()).append("] - ");
	        builder.append("[").append(record.getLevel()).append("] - ");
	        builder.append(formatMessage(record));
	        builder.append("\n");
	        return builder.toString();
	    }
	 
	    public String getHead(Handler h) {
	        return super.getHead(h);
	    }
	 
	    public String getTail(Handler h) {
	        return super.getTail(h);
	    }
	}
	
	
	
	
	private static HashMap<String, Logger> loggers = new HashMap<String, Logger>();
	private static Level currentLevel = Level.ALL;
	
	public static Logger getLogger(String name)
	{
		if (!loggers.containsKey(name)) {
			Logger logger = Logger.getLogger(name);
//			logger.setUseParentHandlers(false);
//			for(Handler handler : logger.getParent().getHandlers())
//			{
//				logger.getParent().removeHandler(handler);
//			}
			logger.setLevel(currentLevel);
//			LogFormatter formatter = new LogFormatter();
//	        ConsoleHandler handler = new ConsoleHandler();
//	        handler.setFormatter(formatter);
	        
//	        logger.getParent().addHandler(handler);
	        
			loggers.put(name, logger);
		}
		return loggers.get(name);
	}
	
	public static void setLevel(String name, Level levelId)
	{
		if (loggers.containsKey(name))
			loggers.get(name).setLevel(levelId);
	}
	
	public static void setLevelAll(Level levelId)
	{
		for (Logger log : loggers.values())
			log.setLevel(levelId);
	}

	public static void disableAll()
	{
		currentLevel = Level.OFF;
		setLevelAll(Level.OFF);
	}
	
	public static void enableAll()
	{
		currentLevel = Level.ALL;
		setLevelAll(Level.ALL);
	}
}
