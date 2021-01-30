package com.khasky.kfsw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.text.SimpleDateFormat;

/**
 * @author Khasky
 */
public class ServerWatcher
{
	private static final int ERRORCODE_NORMAL = 0;
	private static final int ERRORCODE_INTERRUPT = -1;
	private static final int ERRORCODE_PROCESS_LOST = 1; // probably steam error

	private static final String OS = System.getProperty("os.name").toLowerCase();

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

		// DEBUG
		System.out.println("Available processors (cores): " + Runtime.getRuntime().availableProcessors());
		System.out.println("CPU cores num: " + Util.getNumberOfCPUCores());
		System.out.println("Free memory (bytes): " + Runtime.getRuntime().freeMemory());
		long maxMemory = Runtime.getRuntime().maxMemory();
		System.out.println("Maximum memory (bytes): " + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));
		System.out.println("Total memory (bytes): " + Runtime.getRuntime().totalMemory());

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
			try
			{
				systemPath = Config.SERVER_UCC_PATH.split(ucc)[0];
			}
			catch (ArrayIndexOutOfBoundsException e) {}
		}

		if (!afterCrash)
		{
			System.out.println("System path: " + systemPath);
		}

		// Get ini path
		String iniPath = systemPath + Config.SERVER_INI_FILE;

		if (!afterCrash)
		{
			System.out.println("ini path: " + iniPath);
		}

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
		List<String> maps = readMapsList(iniPath);

		if (maps.size() == 0)
		{
			fail("Error: Could not load maps list");
		}

		if (!afterCrash)
		{
			System.out.println("Readed " + maps.size() + " maps from server ini file");
		}

		// Set random map if not selected
		String map = Config.STARTING_MAP;

		if (map.isEmpty())
		{
			Random rand = new Random();
			map = maps.get(rand.nextInt(maps.size()));

			if (!afterCrash)
			{
				System.out.println("Setting map to: " + map);
			}

			rand = null;
		}

		if (Config.MAX_PLAYERS > 0)
		{
			boolean isMaxPlayersSet = setMaxPlayers(iniPath, Config.MAX_PLAYERS);

			if (isMaxPlayersSet)
			{
				System.out.println("Set config MaxPlayers to " + Config.MAX_PLAYERS);
			}
		}

		// Parse mutators
		String mutators = "";

		int mutsCount = Config.MUTATORS_LIST.size();

		if (mutsCount != 0)
		{
			mutators = "?Mutator=";

			for (int i = 0; i < mutsCount; i++)
			{
				mutators += Config.MUTATORS_LIST.get(i);

				if (i != mutsCount - 1)
				{
					mutators += ",";
				}
			}

			if (!afterCrash)
			{
				System.out.println("Added " + mutsCount + " mutators to the server");
			}
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

		if (!Config.SERVER_LOG_FILE.isEmpty())
		{
			procArgs.add("-log=" + Config.SERVER_LOG_FILE);
		}

		if (isUnix(OS))
		{
			procArgs.add("-nohomedir");
		}

		if (!afterCrash)
		{
			System.out.println("Params:");
			System.out.println(params);
		}

		// Start ucc
		if (!afterCrash)
		{
			System.out.println("Initializing...\n");
		}

		Process uccProcess;
		int exitCode = ERRORCODE_NORMAL;

		try
		{
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

		// Reload server on critical error
		if (exitCode != ERRORCODE_NORMAL)
		{
			// Log crash to file
			if (Config.CRASH_LOG_TO_FILE_ENABLED)
			{
				String message = Config.SERVER_UCC_PATH + " crashed with code " + exitCode;

				message += ", map: " + GameState.getInstance().getCurrentMap();
				message += ", online: " + GameState.getInstance().getOnlineCount();
				message += ", uptime: " + Util.getUpTime(System.currentTimeMillis() - _startMillis);

				OutputHandler.getInstance().printSingle(message, true);

				FileLogger.write(Config.CRASH_LOG_TO_FILE_PATH, Config.CRASH_LOG_FILE_MAX_SIZE_KB, message);
			}

			// Save recent UCC log on server crash
			if (Config.UCC_RECENT_LOG_ENABLED && LogStore.getInstance().getCount() != 0)
			{
				final String path = Config.UCC_RECENT_LOG_FILE_PATH;
				final String dateTime = new SimpleDateFormat(Config.FILE_NAME_DATE_FORMAT).format(Calendar.getInstance().getTime());

				String logFileName = "";

				int i = path.lastIndexOf('.');

				if (i > 0)
				{
					final String extension = path.substring(i + 1);

					logFileName = path.substring(0, i) + "_" + dateTime + "." + extension;
				}
				else
				{
					logFileName = path + "_" + dateTime;
				}

				for (String line : LogStore.getInstance().getAll())
				{
					FileLogger.write(logFileName, line);
				}
			}

			launch(true);
		}
	}

	private static boolean isUnix(String os)
	{
        return (os.contains("nix") || os.contains("nux") || os.contains("aix"));
    }

	private static ArrayList<String> readMapsList(String iniPath)
	{
		ArrayList<String> mapsList = new ArrayList<>();

		boolean startRead = false;

		try
		{
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

				if (startRead)
				{
					if (line.startsWith(";") || line.isEmpty())
					{
						continue;
					}

					if (line.startsWith("["))
					{
						break;
					}

					if (line.startsWith("Maps="))
					{
						String name = line.split("Maps=")[1];

						if (!name.isEmpty())
						{
							mapsList.add(name);
						}
					}
				}
			}

			br.close();
			is.close();
			br = null;
			is = null;
		}
		catch (Exception e)
		{
			System.out.println("Error on reading server ini file: " + e.getMessage());
			e.printStackTrace();
		}

		return mapsList;
	}

	private static boolean setMaxPlayers(String iniPath, int maxPlayers)
	{
		if (iniPath == null || iniPath.isEmpty() || maxPlayers < 1)
		{
			return false;
		}

		try
		{
			Path path = Paths.get(iniPath);
			List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

			int blockIndex = -1;
			int replaceIndex = -1;
			int count = 0;
			boolean startRead = false;

			for (String line : lines)
			{
				count++;

				if (line.contains("[Engine.GameInfo]"))
				{
					blockIndex = count - 1;
					startRead = true;
					continue;
				}

				if (startRead)
				{
					if (line.startsWith(";") || line.isEmpty())
					{
						continue;
					}

					if (line.startsWith("["))
					{
						break;
					}

					if (line.startsWith("MaxPlayers="))
					{
						replaceIndex = count - 1;
						break;
					}
				}
			}

			final String valueToSet = "MaxPlayers=" + String.valueOf(maxPlayers);

			// Replace by found line index
			if (replaceIndex != -1)
			{
				lines.set(replaceIndex, valueToSet);
				Files.write(path, lines, StandardCharsets.UTF_8);

				return true;
			}
			// Add by block index
			else if (blockIndex != -1)
			{
				lines.add(blockIndex + 1, valueToSet);
				Files.write(path, lines, StandardCharsets.UTF_8);

				return true;
			}
		}
		catch (Exception e)
		{
			System.out.println("Error on reading or writing to server ini file: " + e.getMessage());
			e.printStackTrace();
		}

		return false;
	}
}
