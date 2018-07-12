package com.khasky;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author Khasky
 */
public class KFServerWatcher
{
	private static final int ERRORCODE_NORMAL = 0;
	private static final int ERRORCODE_INTERRUPT = -1;
	private static final int ERRORCODE_PROCESS_LOST = 1; // probably steam error
	
	private static long _startMillis = 0;
	
    public static void main(String[] args)
    {
		if (args[0] == null || args[0].isEmpty())
		{
			fail("Cannot start without commandline argument: config path");
		}
		else if (Config.load(args[0]))
		{
			launch(false);
		}
		else
		{
			fail("Config load error");
		}
    }
	
	private static void fail(String reason)
	{
		if (reason != null && !reason.isEmpty())
			System.out.println(reason);
		
		System.out.println("KFServerWatcher failed to start");
	}
	
	private static void launch(boolean afterCrash)
	{
		_startMillis = System.currentTimeMillis();
		
		// <!--
		// DEBUG
		/* Total number of processors or cores available to the JVM */
		System.out.println("Available processors (cores): " + Runtime.getRuntime().availableProcessors());
		
		System.out.println("CPU cores num: " + Util.getNumberOfCPUCores());
		
		/* Total amount of free memory available to the JVM */
		System.out.println("Free memory (bytes): " + Runtime.getRuntime().freeMemory());
		
		/* This will return Long.MAX_VALUE if there is no preset limit */
		long maxMemory = Runtime.getRuntime().maxMemory();
		/* Maximum amount of memory the JVM will attempt to use */
		System.out.println("Maximum memory (bytes): " + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));
		
		/* Total memory currently in use by the JVM */
		System.out.println("Total memory (bytes): " + Runtime.getRuntime().totalMemory());
		// -->
		
		// Check for correct paths from config
		
		File file = new File(Config.SERVER_UCC_PATH);
		
		if (!file.exists())
		{
			fail("Error: UCC executable could not be found at path " + Config.SERVER_UCC_PATH);
			return;
		}
		
		// Get System path
		
		String ucc = Config.SERVER_UCC_PATH.substring(Config.SERVER_UCC_PATH.lastIndexOf("/") + 1);
		String systemPath = "";
		
		if (!ucc.isEmpty())
		{
			try {
				systemPath = Config.SERVER_UCC_PATH.split(ucc)[0];
			}
			catch (ArrayIndexOutOfBoundsException e) {}
		}
		
		if (!afterCrash)
			System.out.println("System path: " + systemPath);
		
		// Get ini path
		
		String iniPath = systemPath + Config.SERVER_INI_FILE;
		
		if (!afterCrash)
			System.out.println("ini path: " + iniPath);
		
		file = new File(iniPath);
		
		if (!file.exists())
		{
			fail("Error: Server ini file could not be found (" + iniPath + ")");
			return;
		}
		else if (file.length() < 1)
		{
			fail("Error: Server ini file is empty (" + iniPath + ")");
			return;
		}
		
		// Read maps list from ini file
		
		List<String> maps = new ArrayList<String>();
		
		boolean startRead = false;
		
		try {
			FileInputStream is = new FileInputStream(iniPath);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			
			while ((line = br.readLine()) != null)
			{
				if (line.contains("[KFMod.KFMaplist]"))
				{
					startRead = true;
					continue;
				}
				
				if (startRead) {
					if (line.startsWith(";") || line.isEmpty())
						continue;
					
					if (line.startsWith("["))
						break;
					
					if (line.startsWith("Maps=")) {
						String name = line.split("Maps=")[1];
						if (!name.isEmpty())
							maps.add(name);
					}
				}
			}
			
			br.close();
			is.close();
			br = null;
			is = null;
		}
		catch (Exception e) {
			System.out.println("Error on reading server ini file: " + e.getMessage());
			e.printStackTrace();
		}
		
		if (!afterCrash)
			System.out.println("Readed " + maps.size() + " maps from server ini file");
		
		// Set random map if not selected
		
		String map = Config.STARTING_MAP;
		
		if (map.isEmpty())
		{
			Random rand = new Random();
			map = maps.get(rand.nextInt(maps.size()));
			
			if (!afterCrash)
				System.out.println("Setting map to: " + map);
			
			rand = null;
		}
		
		// Parse mutators
		
		String mutators = "";
		
		int mutsCount = Config.MUTATORS.size();
		
		if (mutsCount > 0)
		{
			mutators = "?Mutator=";
			
			for (int i = 0; i < mutsCount; i++)
			{
				mutators += Config.MUTATORS.get(i);
				
				if (i != mutsCount - 1)
					mutators += ",";
			}
			
			if (!afterCrash)
				System.out.println("Added " + Config.MUTATORS.size() + " mutators to the server");
		}
		
		// Create params list

		List<String> procArgs = new ArrayList<>();

		String params = map + ".rom" 
			+ "?game=" + Config.GAME_TYPE 
			+ "?VACSecured=" + (Config.VAC_SECURED ? "true" : "false") 
			+ "?MaxPlayers=" + String.valueOf(Config.MAX_PLAYERS) 
			+ mutators 
			+ "?AdminName=" + Config.ADMIN_NAME 
			+ "?AdminPassword=" + Config.ADMIN_PASSWORD 
			+ "?ini=" + Config.SERVER_INI_FILE;

		procArgs.add(Config.SERVER_UCC_PATH);
		procArgs.add("server");
		procArgs.add(params);
		procArgs.add("-log=" + Config.SERVER_LOG_FILE);

		/*
		String[] args =
		{
			Config.SERVER_UCC_PATH,
			"server",
			params,
			"-log=" + Config.SERVER_LOG_FILE,
			priority
		};
		*/
		
		if (!afterCrash)
		{
			System.out.println("Params:");
			System.out.println(params);
		}
		
		// Start ucc
		
		if (!afterCrash)
			System.out.println("Initializing...\n");
		
		Process uccProcess;
		int exitCode = ERRORCODE_NORMAL;
		
		try
		{
			//String procCommand = String.join(" ", procArgs);
			//ProcessBuilder pb = new ProcessBuilder(args[0], args[1], args[2], args[3], args[4]);
			ProcessBuilder pb = new ProcessBuilder(procArgs);
			uccProcess = pb.start();

			try
			{
				InputStreamReader is = new InputStreamReader(uccProcess.getInputStream());
				BufferedReader br = new BufferedReader(is);
				ProcessReader reader = new ProcessReader(br);
				
				reader.start();
				
				try
				{
					exitCode = uccProcess.waitFor();
				}
				catch (InterruptedException e3)
				{
					System.out.println("Reader thread was interrupted");
					e3.printStackTrace();
					
					exitCode = ERRORCODE_INTERRUPT;
				}
			}
			catch (Exception e2)
			{
				System.out.println("Error on start reading process thread");
				e2.printStackTrace();
			}
		}
		catch (Exception e1)
		{
			System.out.println("Error on process start: " + ucc);
			e1.printStackTrace();
		}
		
		if (exitCode != ERRORCODE_NORMAL)
		{
			if (Config.CRASH_LOG_TO_FILE_ENABLED)
			{
				String message = Config.SERVER_UCC_PATH + " crashed with code " + exitCode;
				
				message += ", map: " + GameState.getInstance().getCurrentMap();
				message += ", online: " + GameState.getInstance().getOnlineCount();
				message += ", uptime: " + getUpTime(System.currentTimeMillis() - _startMillis);
				
				OutputHandler.getInstance().printSingle(message, true);
				
				FileLogger.getInstance().write(Config.CRASH_LOG_TO_FILE_PATH, Config.CRASH_LOG_FILE_MAX_SIZE_KB, message);
			}
			
			launch(true);
		}
	}
	
	private static String getUpTime(long millis)
	{
		int secondsInMinute = 60;
		int secondsInHour = 60 * secondsInMinute;
		int secondsInDay = 24 * secondsInHour;
		
		double seconds = millis / 1000.0;
		
		String out = "";
		
		double days = seconds / secondsInDay;
		
		if (days > 0)
			out += (int) days + "d ";
		
		double hourSeconds = seconds % secondsInDay;
		double hours = Math.floor(hourSeconds / secondsInHour);
		
		if (hours > 0)
			out += (int) hours + "h ";
		
		double minuteSeconds = hourSeconds % secondsInHour;
		double minutes = Math.floor(minuteSeconds / secondsInMinute);
		
		out += (int) minutes + "m";
		
		return out;
	}
}