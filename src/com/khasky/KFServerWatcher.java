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
	private static final int ERRORCODE_PROCESS_LOST = 1; // steam error
	
	private static long _startMillis = 0;
	
    public static void main(String[] args)
    {
		if (Config.load(args[0])) {
			System.out.println("KFServerWatcher version " + Config.VERSION);
			launch();
		}
		else {
			fail(null);
		}
    }
	
	private static void fail(String reason)
	{
		if (reason != null && !reason.isEmpty())
			System.out.println(reason);
		System.out.println("KFServerWatcher failed to start");
	}
	
	private static void launch()
	{
		_startMillis = System.currentTimeMillis();
		
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
		
		System.out.println("System path: " + systemPath);
		
		// Get ini path
		
		String iniPath = systemPath + Config.SERVER_INI_FILE;
		
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
			String line = null;
			
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
		
		System.out.println("Readed " + maps.size() + " maps from server ini file");
		
		// Set random map if not selected
		
		String map = Config.STARTING_MAP;
		
		if (map.isEmpty())
		{
			Random rand = new Random();
			map = maps.get(rand.nextInt(maps.size()));
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
			
			System.out.println("Added " + Config.MUTATORS.size() + " mutators to the server");
		}
		else
		{
			System.out.println("There are no server mutators");
		}
		
		// Create params list
		
		String params = map + ".rom" 
			+ "?game=" + Config.GAME_TYPE 
			+ "?VACSecured=" + (Config.VAC_SECURED ? "true" : "false") 
			+ "?MaxPlayers=" + String.valueOf(Config.MAX_PLAYERS) 
			+ mutators 
			+ "?AdminName=" + Config.ADMIN_NAME 
			+ "?AdminPassword=" + Config.ADMIN_PASSWORD 
			+ "?ini=" + Config.SERVER_INI_FILE;
		
		String[] args = {
			Config.SERVER_UCC_PATH,
			"server",
			params,
			"-log=" + Config.SERVER_LOG_FILE
		};
		
		System.out.println("Params:");
		System.out.println(params);
		
		// Start ucc
		
		System.out.println("Initializing...\n");
		
		Process proc = null;
		int exitCode = ERRORCODE_NORMAL;
		boolean failed = false;
		
		try {
			ProcessBuilder pb = new ProcessBuilder(args[0], args[1], args[2], args[3]);
			proc = pb.start();
			
			try {
				InputStreamReader is = new InputStreamReader(proc.getInputStream());
				BufferedReader br = new BufferedReader(is);
				ProcessReader reader = new ProcessReader(br);
				
				reader.start();
				
				try {
					exitCode = proc.waitFor();
				}
				catch (InterruptedException e) {
					System.out.println("Reader thread was interrupted");
					exitCode = ERRORCODE_INTERRUPT;
				}
			}
			catch (Exception e) {
				failed = true;
				System.out.println("Error on start reading process thread");
				e.printStackTrace();
			}
		}
		catch (Exception e) {
			failed = true;
			System.out.println("Error on process start: " + ucc);
			e.printStackTrace();
		}
		
		if (failed)
		{
			fail(null);
		}
		else if (exitCode != ERRORCODE_NORMAL)
		{
			if (Config.CRASH_LOG_TO_FILE_ENABLED)
			{
				String currentMap = GameState.getInstance().getCurrentMap();
				
				int online = GameState.getInstance().getOnlineCount();
				
				String uptime = getUpTime(System.currentTimeMillis() - _startMillis);
				
				String message = Config.SERVER_UCC_PATH + " crashed with code " + exitCode + " (map: " + currentMap + ", online: " + online + ", uptime: " + uptime + ")";
				
				FileLogger.getInstance().write(Config.CRASH_LOG_TO_FILE_PATH, Config.CRASH_LOG_FILE_MAX_SIZE_KB, message);
			}
			
			launch();
		}
	}
	
	private static String getUpTime(long millis)
	{
		int seconds = (int)(millis / 1000) % 60;
		int minutes = (int)((millis / (1000*60)) % 60);
		int hours = (int)((millis / (1000*60*60)) % 24);
		int days = (int)((millis / (1000*60*60*24)) % 365);
		int years = (int)(millis / 1000*60*60*24*365);
		
		ArrayList<String> timeArray = new ArrayList<String>();
		
		if (years > 0)
			timeArray.add(String.valueOf(years)   + "y");
		
		if (days > 0)
			timeArray.add(String.valueOf(days) + "d");
		
		if (hours > 0)
			timeArray.add(String.valueOf(hours) + " h");
		
		if (minutes > 0)
			timeArray.add(String.valueOf(minutes) + "m");
		
		if (seconds > 0)
			timeArray.add(String.valueOf(seconds) + "s");
		
		String time = "";
		
		for (int i = 0; i < timeArray.size(); i++)
		{
			time = time + timeArray.get(i);
			
			if (i != timeArray.size() - 1)
				time = time + ", ";
		}
		
		if (time.isEmpty())
			time = "0 sec";
		
		return time;
	}
}