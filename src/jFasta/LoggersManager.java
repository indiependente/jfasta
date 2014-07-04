package jFasta;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggersManager {
	
	private static HashMap<String, Logger> loggers = new HashMap<String, Logger>();
	private static Level currentLevel = Level.ALL;
	
	public static Logger getLogger(String name)
	{
		if (!loggers.containsKey(name)) {
			Logger log = Logger.getLogger(name);
			log.setLevel(currentLevel);
			loggers.put(name, log);
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
