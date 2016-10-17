package com.khasky;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Khasky
 */
public class OutputHandler
{
	private static OutputHandler _instance;
	
	public static OutputHandler getInstance()
	{
		if (_instance == null) {
			_instance = new OutputHandler();
		}
		return _instance;
	}
	
	private SimpleDateFormat _formatter;
	
	private boolean _isClearOutput = true;
	
	private String ACTIVITY_START = " @ ";
	private String ACTIVITY_NEXT = " | ";
	private String TIME_SPACER = " ";
	private String SINGLE_SPACE = "  ";
	
	public OutputHandler()
	{
		_formatter = new SimpleDateFormat(Config.CONSOLE_OUTPUT_DATE_FORMAT);
		
		for (int i = 0; i < Config.CONSOLE_OUTPUT_DATE_FORMAT.length(); i++) {
			TIME_SPACER += " ";
		}
	}
	
	public String[] concat(String[] a, String[] b)
	{
		int aLen = a.length;
		int bLen = b.length;
		
		String[] c = new String[aLen + bLen];
		
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);
		
		return c;
	}
	
	private String getTime()
	{
		return _formatter.format(Calendar.getInstance().getTime());
	}
	
	private void out(String s)
	{
		System.out.println(getTime() + " " + s);
	}
	
	private void printLines(String[] lines, boolean bTime)
	{
		if (!_isClearOutput) {
			System.out.println(TIME_SPACER + ACTIVITY_NEXT);
		}
		else {
			_isClearOutput = false;
		}
		
		if (bTime)
			System.out.println(" " + getTime() + ACTIVITY_START);
		
		for (int i = 0; i < lines.length; i++)
		{
			//if (i == 0 && !bTime)
			//	System.out.println(TIME_SPACER + ACTIVITY_START + lines[i]);
			//else
			System.out.println(TIME_SPACER + ACTIVITY_NEXT + lines[i]);
		}
	}
	
	private void printSingle(String line, boolean bTime)
	{
		if (!_isClearOutput) {
			System.out.println(TIME_SPACER + ACTIVITY_NEXT);
		}
		else {
			_isClearOutput = false;
		}
		
		if (bTime)
			System.out.println(" " + getTime() + ACTIVITY_START);
		
		//System.out.println((bTime ? TIME_SPACER + ACTIVITY_NEXT : TIME_SPACER + ACTIVITY_START) + line);
		System.out.println(TIME_SPACER + ACTIVITY_NEXT + line);
	}
	
	private String[] getOnlinePlayersLines()
	{
		int online = GameState.getInstance().getOnlineCount();
		
		String[] lines = new String[online + 1];
		
		if (online == 0)
			lines[0] = "No players online";
		else if (online == 1)
			lines[0] = "1 player online:";
		else
			lines[0] = online + " players online:";
		
		if (online > 0)
		{
			int i = 0;
			
			for (PlayerModel pm : GameState.getInstance().getOnlinePlayers())
			{
				int pos = i + 1;
				int ipMaxLen = 15;
				
				String ip = pm.getIp();
				
				if (ip.length() < ipMaxLen) {
					for (int k = 0; k < ipMaxLen - ip.length(); k++) {
						ip += " ";
					}
				}
				
				lines[pos] = SINGLE_SPACE + String.valueOf(pos) + "." + SINGLE_SPACE + pm.getSteamId() + SINGLE_SPACE + ip + SINGLE_SPACE + pm.getName();
				
				i++;
			}
		}
		
		return lines;
	}
	
	private void showConnection(PlayerModel player, boolean isConnected, boolean showOnline)
	{
		if (player == null || GameState.getInstance().isMapChanging())
			return;
		
		String map = GameState.getInstance().getCurrentMap();
		int online = GameState.getInstance().getOnlineCount();
		
		String[] connected = {
			player.getName() + (isConnected ? " connected" : " disconnected"),
			"Map: " + map
		};
		
		String[] list = showOnline ? getOnlinePlayersLines() : new String[0];
		
		printLines(concat(connected, list), true);
		
		logToFile((isConnected ? "Connected: " : "Disconnected: ") + player.getName() + " " + player.getSteamId() + " " + player.getIp());
		logToFile("Map: " + map);
		logToFile("Online: " + online);
	}
	
	private void showOnlinePlayers()
	{
		if (GameState.getInstance().isMapChanging())
			return;
		
		printLines(getOnlinePlayersLines(), true);
	}
	
	private String pullIpPort(String input)
	{
		Pattern p = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d{1,5})");
		Matcher m = p.matcher(input);
		return m.find() ? (m.group(1) /*+ ":" + m.group(2)*/) : "";
	}
	
	private String pullName(String input)
	{
		//Pattern p = Pattern.compile("New Player\\s(.+)\\sid");
		//Matcher m = p.matcher(input);
		//return m.find() ? m.group() : "";
		return input.split("New Player")[1].split("id=")[0].trim();
	}
	
	private String pullSteamId(String input)
	{
		Pattern p = Pattern.compile("\\d{17}");
		Matcher m = p.matcher(input);
		return m.find() ? m.group() : "";
	}
	
	public void parseLine(String line)
	{
		GameState.getInstance().startServerLoad();
		
		// Show specified UCC logs
		if (!Config.SHOW_UCC_LOGS_STARTS_WITH.isEmpty() && line.startsWith(Config.SHOW_UCC_LOGS_STARTS_WITH))
		{
			String message = "UCC: " + line;
			out(message);
			logToFile(message);
		}
		
		// Server started
		if (line.startsWith("STEAMAUTH : Sending updated server details"))
		{
			boolean isLoaded = GameState.getInstance().endServerLoad();
			
			if (isLoaded) {
				String message = "Server successfully started in " + GameState.getInstance().getServerLoadSeconds() + " seconds";
				printSingle(message, true);
				logToFile(message);
			}
		}
		// Map change started
		else if (line.startsWith("PreClientTravel"))
		{
			GameState.getInstance().setMapChanging(true);
			
			String message = "Map changing in progress...";
			printSingle(message, true);
			logToFile(message);
		}
		// Map change ended
		else if (line.startsWith("Bringing Level"))
		{
			String map = line.split("Bringing Level")[1].split(".myLevel")[0].trim();
			
			GameState.getInstance().setCurrentMap(map, true);
			
			if (!GameState.getInstance().isMapInit())
			{
				String message = "Changed map to: " + GameState.getInstance().getCurrentMap();
				printSingle(message, true);
				logToFile(message);
			}
			else
			{
				GameState.getInstance().setMapInit(false);
			}
			
			GameState.getInstance().setMapChanging(false);
		}
		// First wave started
		else if (line.startsWith("START MATCH"))
		{
			GameState.getInstance().setGameStarted(true);
			
			String message = "Game started";
			printSingle(message, true);
			logToFile(message);
			
			showOnlinePlayers();
		}
		// Connection
		else if (line.startsWith("Open myLevel"))
		{
			String ip = pullIpPort(line);
			
			logToFile("Incoming connection: " + ip);
			
			GameState.getInstance().addIncomingIp(ip);
		}
		else if (line.startsWith("New Player"))
		{
			int incomingCount = GameState.getInstance().getIncomingIpsCount();
			
			if (incomingCount > 0)
			{
				logToFile("Incoming IPs: " + incomingCount);
				
				String name = pullName(line);
				String steamId = pullSteamId(line);
				
				PlayerModel player = GameState.getInstance().onPlayerConnect(name, steamId);
				
				if (player != null)
				{
					showConnection(player, true, true);
				}
			}
			else
			{
				logToFile("Warning: Incoming IPs = 0, but player connected");
				logToFile(line);
			}
		}
		// Disconnect
		else if (line.startsWith("Close TcpipConnection"))
		{
			String ip = pullIpPort(line);
			
			logToFile("Disconnected: " + ip);
			
			PlayerModel player = GameState.getInstance().onPlayerDisconnect(ip);
			
			if (player != null)
			{
				showConnection(player, false, true);
			}
			
			logToFile("Incoming IPs after disconnect: " + GameState.getInstance().getIncomingIpsCount());
		}
	}
	
	private void logToFile(String line)
	{
		if (Config.FILE_OUTPUT_LOGGING_ENABLED)
			FileLogger.getInstance().write(Config.FILE_OUTPUT_LOGGING_PATH, Config.FILE_OUTPUT_LOGGING_MAX_SIZE, line);
	}
}