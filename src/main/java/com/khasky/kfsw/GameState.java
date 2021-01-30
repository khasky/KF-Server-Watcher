package com.khasky.kfsw;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Khasky
 */
public class GameState
{
	private static GameState _instance;

	public static GameState getInstance()
	{
		if (_instance == null)
		{
			_instance = new GameState();
		}

		return _instance;
	}

	private List<PlayerModel> _players;
	private List<String> _incomingIPs;

	private String _lastIncomingIp;
	private String _previousMap;
	private String _currentMap = "";

	private long _mapStartMillis;
	private long _mapEndMillis;

	private boolean _isRunning = false;
	private boolean _isMapInit = true;
	private boolean _isMapChanging = false;
	private boolean _isGameStarted = false;

	private long _startMillis = 0;
	private int _serverLoadedIn = 0;
	private int _runState = 1;

	public GameState()
	{
		_players = new ArrayList<PlayerModel>();
		_incomingIPs = new ArrayList<String>();
	}

	public void startServerLoad()
	{
		if (_startMillis == 0)
			_startMillis = System.currentTimeMillis();
	}

	public boolean endServerLoad()
	{
		if (_isRunning)
			return false;

		if (_runState == 0)
		{
			_serverLoadedIn = (int) (System.currentTimeMillis() - _startMillis) / 1000;
			_isRunning = true;
			return true;
		}

		_runState--;

		return false;
	}

	public int getServerLoadSeconds()
	{
		return _serverLoadedIn;
	}

	public void matchStart()
	{
		_mapStartMillis = System.currentTimeMillis();
		_isGameStarted = true;
	}

	public void matchEnd()
	{
		_mapEndMillis = System.currentTimeMillis();
	}

	public boolean isGameStarted()
	{
		return _isGameStarted;
	}

	public void setMapInit(boolean value)
	{
		_isMapInit = value;
	}

	public boolean isMapInit()
	{
		return _isMapInit;
	}

	public void setMapChanging(boolean value)
	{
		_isMapChanging = value;

		if (Config.SAVE_MAP_STATS_TO_DB)
		{
			String pm = getPreviousMap();
			String cm = getCurrentMap();

			if (pm == null || pm.isEmpty() || pm.equalsIgnoreCase((cm)))
				return;

			long duration = 0;

			if (_mapStartMillis > 0 && _mapEndMillis > 0 && _mapEndMillis < _mapStartMillis)
				duration = _mapEndMillis - _mapStartMillis;

			DatabaseLogger.getInstance().saveMapStat(getCurrentMap(), duration, getOnlineCount());
		}
	}

	public boolean isMapChanging()
	{
		return _isMapChanging;
	}

	public String getCurrentMap()
	{
		return _currentMap;
	}

	public String getPreviousMap()
	{
		return _previousMap;
	}

	public void setCurrentMap(String name, boolean clear)
	{
		_previousMap = _currentMap;
		_currentMap = name;

		if (clear) {
			_incomingIPs.clear();
			_players.clear();
		}
	}

	public int getOnlineCount()
	{
		return _players.size();
	}

	public List<PlayerModel> getOnlinePlayers()
	{
		return _players;
	}

	private PlayerModel removePlayerByIp(String ip)
	{
		if (_players.size() == 0)
			return null;

		PlayerModel removedPlayer = null;
		PlayerModel playerObject = null;

		for (PlayerModel player : _players)
		{
			if (player.getIp().equalsIgnoreCase(ip)) {
				removedPlayer = new PlayerModel(player.getName(), player.getIp(), player.getSteamId());
				playerObject = player;
				break;
			}
		}

		if (playerObject != null) {
			_players.remove(playerObject);
		}

		return removedPlayer;
	}

	public boolean isIncomingIpExists(String ip)
	{
		for (String incIp : _incomingIPs)
		{
			if (incIp.equalsIgnoreCase(ip)) {
				return true;
			}
		}

		return false;
	}

	public void addIncomingIp(String ip)
	{
		if (!ip.isEmpty() && !isIncomingIpExists(ip))
			_incomingIPs.add(ip);
	}

	public boolean removeIncomingIp(String ip)
	{
		String incomingIp = null;

		for (String incIp : _incomingIPs)
		{
			if (incIp.equalsIgnoreCase(ip)) {
				incomingIp = incIp;
				break;
			}
		}

		if (incomingIp != null) {
			_incomingIPs.remove(incomingIp);
			return true;
		}

		return false;
	}

	public int getIncomingIpsCount()
	{
		return _incomingIPs.size();
	}

	/**
	 * Events
	 */
	public PlayerModel onPlayerConnect(String name, String steamId)
	{
		String ip = _incomingIPs.get(0);
		PlayerModel result = new PlayerModel(name, ip, steamId);

		if (result != null) {
			_players.add(result);
		}

		_incomingIPs.remove(0);

		return result;
	}

	public PlayerModel onPlayerDisconnect(String ip)
	{
		PlayerModel result = removePlayerByIp(ip);
		_lastIncomingIp = ip;
		removeIncomingIp(ip);

		return result;
	}
}
