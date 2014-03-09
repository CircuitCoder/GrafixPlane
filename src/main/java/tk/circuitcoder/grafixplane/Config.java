package tk.circuitcoder.grafixplane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
	private Map<String,String> configs;
	
	public Config() {
		configs=new HashMap<String,String>();
	}
	
	public Config(Connection conn) {
		this.load(conn);
	}
	
	public String get(String key) {
		synchronized (configs) {
			return configs.get(key);
		}
	}
	
	public Integer getInt(String key) {
		String str=get(key);
		if(str==null) return null;
		return Integer.parseInt(str);
	}
	
	public Long getLong(String key) {
		String str=get(key);
		if(str==null) return null;
		return Long.parseLong(str);
	}
	
	public String set(String key,String value) {
		synchronized (configs) {
			return configs.put(key, value);
		}
	}
	
	public String setInt(String key,int value) {
		return configs.put(key, String.valueOf(value));
	}
	
	public String setLong(String key,long value) {
		return configs.put(key, String.valueOf(value));
	}
	
	public boolean load(Connection conn) {
		try {
			Statement stat=conn.createStatement();
			ResultSet entries=stat.executeQuery("SELECT * FROM GRAFIX");
			configs=new HashMap<String,String>(); //Create a new map
			entries.first();
			do {
				configs.put(entries.getString(1),entries.getString(2));
			} while(entries.next());
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean init(Connection conn,List<String> pairs) {
		GrafixPlane.getGP().getLogger().info("First run...");
		try {
			PreparedStatement stat=conn.prepareStatement("INSERT INTO GRAFIX VALUES (?,?)");
			String k,v;
			configs=new HashMap<String,String>(); //Create a new map
			for(String each:pairs) {
				k=each.split(":")[0];
				v=each.split(":")[1];
				GrafixPlane.getGP().getLogger().info("Initializing Key-Value pair: "+k+' '+v);
				stat.setString(1,k);
				stat.setString(2,v);
				stat.execute();
				configs.put(k, v);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean save(Connection conn) {
		synchronized (configs) {
			try {
				PreparedStatement stat=conn.prepareStatement("UPDATE GRAFIX SET Value = ? WHERE Entry = ?");
				PreparedStatement newEntry=conn.prepareStatement("INSERT INTO GRAFIX VALUES (?,?)");
				for(String k:configs.keySet()) {
					stat.setString(2,k);
					stat.setString(1,configs.get(k));
					if(stat.executeUpdate()==0) {
						newEntry.setString(1,k);
						newEntry.setString(2,configs.get(k));
						newEntry.execute();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			return false;
			}
			return true;
		}
	}
}
