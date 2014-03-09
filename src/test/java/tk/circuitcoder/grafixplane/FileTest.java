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
				+ "CTime bigint UNSIGNED,"
				+ "Accable varchar"
				+ ");");
		
		stat.execute("INSERT INTO FILE VALUES(1,'"+File.formatDir("/hello/target1.txt")+"',13,123456,'|13|17|')");
		File.init(conn,2);
	}
	
	@Test
	public void basicFileTest() throws SQLException {
		File f=File.getFile(1);
		assertNotNull(f);
		assertEquals(1,f.FID());
		assertEquals("/hello/target1.txt", f.dir());
		assertEquals(13,f.ownerID());
		assertEquals(123456,f.createTime());
		
		System.out.println(f.fileName());
		assertEquals("target1.txt",f.fileName());
		assertEquals("target1",f.baseName());
		assertEquals("txt",f.extendName());
		
		assertTrue(File.hasFileIn("/hello/target1.txt",13));
		assertTrue(f.isAccessible(13));
		assertTrue(f.isAccessible(17));
		//TODO date/formatted time test
	}
	
	@Test
	public void dirFormatTest() {
		assertEquals("/1/2/3",File.formatDir("//5/../2/6/../.././1/2//3/"));
	}
}
