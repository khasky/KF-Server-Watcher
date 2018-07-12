package com.khasky;

/**
 * @author Khasky
 */
public class PlayerModel
{
	private String _name;
	private String _ip;
	private String _steamId;
	private long _joinTime;
	
	public PlayerModel(String name, String ip, String steamId)
	{
		_name = name;
		_ip = ip;
		_steamId = steamId;
		_joinTime = System.currentTimeMillis() / 1000L;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String getIp()
	{
		return _ip;
	}
	
	public String getSteamId()
	{
		return _steamId;
	}
	
	public long getJoinTime()
	{
		return _joinTime;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public void setIp(String ip)
	{
		_ip = ip;
	}
	
	public void setSteamId(String steamid)
	{
		_steamId = steamid;
	}
}