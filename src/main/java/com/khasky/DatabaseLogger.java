package com.khasky;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

public class DatabaseLogger
{
	private static DatabaseLogger _instance;
	
	public static DatabaseLogger getInstance()
	{
		if (_instance == null) {
			_instance = new DatabaseLogger();
		}
		return _instance;
	}
	
	public DatabaseLogger()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("JDBC driver is not allowed");
			e.printStackTrace();
		}
	}

	private String getUrl()
	{
		return Config.MYSQL_URL;
	}

	private String getUser()
	{
		return Config.MYSQL_USER;
	}

	private String getPass()
	{
		return Config.MYSQL_PASSWORD;
	}
	
	private void closeConnection(Connection con)
	{
		if (con == null)
			return;
		
		try
		{
			con.close();
			con = null;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveMapStat(String mapName, long gameDuration, int onlinePlayers)
	{
		Connection con = null;
		
		int db_times_played = 0;
		long db_max_game_duration = 0;
		int db_max_online_players = 0;
		
		boolean isUpdate = false;
		
		try
		{
			con = DriverManager.getConnection(getUrl(), getUser(), getPass());
			
			PreparedStatement ps = con.prepareStatement("SELECT times_played, max_game_duration, max_online_players FROM map_stats WHERE name=?");
			ps.setString(1, mapName);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next())
			{
				db_times_played = rs.getInt("times_played");
				db_max_game_duration = rs.getLong("max_game_duration");
				db_max_online_players = rs.getInt("max_online_players");
				
				isUpdate = true;
			}

			rs.close();
			ps.close();
			ps = null;
			rs = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			closeConnection(con);
		}
		
		if (isUpdate)
		{
			try {
				con = DriverManager.getConnection(getUrl(), getUser(), getPass());
				
				String times = String.valueOf(db_times_played + 1);
				String duration = String.valueOf(gameDuration > db_max_game_duration ? gameDuration : db_max_game_duration);
				String players = String.valueOf(onlinePlayers > db_max_online_players ? onlinePlayers : db_max_online_players);
				
				String psSetQuery = "max_game_duration=" + duration + ", max_online_players=" + players + ", times_played=" + times;
				
				PreparedStatement ps = con.prepareStatement("UPDATE map_stats SET " + psSetQuery + " WHERE name=?;");
				ps.setString(1, mapName);
				ps.execute();
				
				ps.close();
				ps = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				closeConnection(con);
			}
		}
		else
		{
			try
			{
				con = DriverManager.getConnection(getUrl(), getUser(), getPass());
				
				PreparedStatement ps = con.prepareStatement("INSERT INTO map_stats (name, times_played, max_game_duration, max_online_players, last_game_date) VALUES (?,?,?,?,?)");
				
				ps.setString(1, mapName);
				ps.setInt(2, 1);
				ps.setLong(3, gameDuration);
				ps.setInt(4, onlinePlayers);
				ps.setLong(5, System.currentTimeMillis());
				
				ps.execute();
				ps.close();
				ps = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				closeConnection(con);
			}
		}
	}
}