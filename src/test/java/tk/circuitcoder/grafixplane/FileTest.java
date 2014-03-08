package tk.circuitcoder.grafixplane;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import tk.circuitcoder.grafixplane.db.H2DatabaseManager;
import tk.circuitcoder.grafixplane.file.File;

public class FileTest {
	private static java.io.File tar;
	@BeforeClass
	public static void setup() throws ClassNotFoundException, SQLException {
		H2DatabaseManager manager=new H2DatabaseManager();
		manager.startDB(9750,false,false);
		Connection conn=manager.getConn("", "", "mem:TEST", false);
		Statement stat=conn.createStatement();
		stat.execute("CREATE TABLE FILE ("
				+ "FID int UNSIGNED,"
				+ "Dir varchar,"
				+ "Owner int,"
				+ "CTime bigint UNSIGNED"
				+ ");");
		
		tar=new java.io.File("13","target1.txt");
		stat.execute("INSERT INTO FILE VALUES(1,'"+tar.getAbsolutePath()+"',13,123456)");
		File.init(conn);
	}
	
	@Test
	public void basicFileTest() throws SQLException {
		File f=File.getFile(1);
		assertNotNull(f);
		assertEquals(1,f.FID());
		assertEquals(tar.getAbsolutePath(), f.dir());
		assertEquals(13,f.ownerID());
		assertEquals(123456,f.createTime());
		//TODO date/formatted time test
	}
}
