package com.khasky;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author Khasky
 */
public final class Config
{
	public static String VERSION = "0.3.8";
	
	public static String ADMIN_NAME;
	public static String ADMIN_PASSWORD;
	
	public static String SERVER_UCC_PATH;
	public static String SERVER_INI_FILE;
	public static String SERVER_LOG_FILE;
	
	public static String STARTING_MAP;
	public static List<String> MUTATORS = new ArrayList<String>();
	public static int MAX_PLAYERS;
	public static String GAME_TYPE;
	public static boolean VAC_SECURED;
	
	public static String CONSOLE_OUTPUT_DATE_FORMAT;
	
	public static String SHOW_UCC_LOGS_STARTS_WITH;
	
	public static String FILE_OUTPUT_DATE_FORMAT;
	public static boolean FILE_OUTPUT_LOGGING_ENABLED;
	public static String FILE_OUTPUT_LOGGING_PATH;
	public static int FILE_OUTPUT_LOGGING_MAX_SIZE;
	
	public static boolean CRASH_LOG_TO_FILE_ENABLED;
	public static String CRASH_LOG_TO_FILE_PATH;
	public static int CRASH_LOG_FILE_MAX_SIZE_KB;
	
	public static boolean load(String configPath)
	{
		if (configPath == null || configPath.isEmpty())
			return false;
		
		try {
			Properties p = new Properties();
			InputStream is = new FileInputStream(new File(configPath));
			p.load(is);
			is.close();
			
			FileInputStream fis = new FileInputStream(configPath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			
			while ((line = br.readLine()) != null)
			{
				if (line.toLowerCase().startsWith("mutator")) {
					String mutator = line.split("=")[1].trim();
					if (!mutator.isEmpty())
						MUTATORS.add(mutator);
				}
			}
			
			br.close();
			fis.close();
			
			ADMIN_NAME = p.getProperty("AdminName", "Admin");
			ADMIN_PASSWORD = p.getProperty("AdminPassword", "12345");
			
			SERVER_UCC_PATH = p.getProperty("ServerUccPath", "System/ucc.exe");
			SERVER_INI_FILE = p.getProperty("ServerIniFile", "server.ini");
			SERVER_LOG_FILE = p.getProperty("ServerLogFile", "server.log");
			
			STARTING_MAP = p.getProperty("Map", "");
			MAX_PLAYERS = Integer.parseInt(p.getProperty("MaxPlayers", "6"));
			GAME_TYPE = p.getProperty("GameType", "KFmod.KFGameType");
			VAC_SECURED = Boolean.parseBoolean(p.getProperty("VacSecured", "true"));
			
			CONSOLE_OUTPUT_DATE_FORMAT = p.getProperty("ConsoleOutputDateFormat", "HH:mm:ss");
			
			SHOW_UCC_LOGS_STARTS_WITH = p.getProperty("ShowUccLogsStartsWith", "");
			
			FILE_OUTPUT_DATE_FORMAT = p.getProperty("FileOutputDateFormat", "yyyy-MM-dd HH:mm:ss");
			FILE_OUTPUT_LOGGING_ENABLED = Boolean.parseBoolean(p.getProperty("FileOutputLoggingEnabled", "true"));
			FILE_OUTPUT_LOGGING_PATH = p.getProperty("FileOutputLoggingPath", "kfsw_console.log");
			FILE_OUTPUT_LOGGING_MAX_SIZE = Integer.parseInt(p.getProperty("FileOutputLoggingMaxSizeKb", "512"));
			
			CRASH_LOG_TO_FILE_ENABLED = Boolean.parseBoolean(p.getProperty("CrashFileLogEnabled", "true"));
			CRASH_LOG_TO_FILE_PATH = p.getProperty("CrashFileLogPath", "kfsw_crash.log");
			CRASH_LOG_FILE_MAX_SIZE_KB = Integer.parseInt(p.getProperty("CrashFileLogMaxSizeKb", "512"));
			
			return true;
		}
		catch (Exception e) {
			System.out.println("Failed to load config: " + e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}
}