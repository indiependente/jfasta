package jFasta;

import java.util.logging.Logger;

public class Profiler 
{
	private static final int KB_SIZE = 1 << 10;
	
	private static Logger logger = LoggersManager.getLogger(Profiler.class.getName());
	
	private String name;
	private long startTime;
	private long startMemory;
	
	public Profiler(String name)
	{
		Runtime runtime = Runtime.getRuntime();
		this.name = name;
		this.startTime = System.currentTimeMillis();
		this.startMemory = runtime.totalMemory() - runtime.freeMemory();
	}
	
	public long end()
	{
		Runtime runtime = Runtime.getRuntime();
		long endTime = System.currentTimeMillis() - startTime;
		long usedMemory = runtime.totalMemory() - runtime.freeMemory();
		logger.info(name + " :: elapsed time=[" + endTime + " ms] :: current used memory=[" + 
					(usedMemory / KB_SIZE) + " kb] memory increase=[" + ((usedMemory - startMemory) / KB_SIZE) + " kb]");
		return endTime;
	}
}
