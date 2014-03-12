package tk.circuitcoder.grafixplane;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import tk.circuitcoder.grafixplane.db.H2DatabaseManager;
import tk.circuitcoder.grafixplane.file.File;

public class FileTest {
	private static Statement stat;
	
	@BeforeClass
	public static void setup() throws ClassNotFoundException, SQLException {
		H2DatabaseManager manager=new H2DatabaseManager();
		manager.startDB(9750,false,false);
		Connection conn=manager.getConn("", "", "mem:TEST", false);
		stat=conn.createStatement();
		stat.execute("CREATE TABLE FILE ("
				+ "FID int UNSIGNED,"
				+ "Dir varchar,"
				+ "Owner int,"
				+ "CTime bigint UNSIGNED,"
				+ "Accable varchar"
				+ ");");
		
		stat.execute("INSERT INTO FILE VALUES(1,'"+File.formatDir("/hello/target1.txt")+"',13,123456,'|13|17|')");
		stat.execute("INSERT INTO FILE VALUES(2,'"+File.formatDir("/hello/target2.png")+"',13,567890,'|13|15|')");
		stat.execute("INSERT INTO FILE VALUES(3,'"+File.formatDir("/muhaha")+"',13,111111,'|13|')");
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
	
	@Test
	public void accessibleTest() throws SQLException {
		stat.execute("INSERT INTO FILE VALUES(4,'"+File.formatDir("/111")+"',13,111111,'|13|17|')");
		File f=File.getFile(4);
		f.addAccessible(123);
		assertTrue(f.isAccessible(123));
		assertTrue(File.getFile(4).isAccessible(123));
		
		f.addAccessible(Arrays.asList(4,5,6));
		assertTrue(f.isAccessible(4));
		assertTrue(File.getFile(4).isAccessible(4));
		assertTrue(f.isAccessible(5));
		assertTrue(File.getFile(4).isAccessible(5));
		assertTrue(f.isAccessible(6));
		assertTrue(File.getFile(4).isAccessible(6));
		
		f.removeAccessible(17);
		assertFalse(f.isAccessible(17));
		assertFalse(File.getFile(4).isAccessible(17));
		
		f.removeAccessible(Arrays.asList(123,5));
		assertFalse(f.isAccessible(123));
		assertFalse(File.getFile(4).isAccessible(123));
		assertFalse(f.isAccessible(5));
		assertFalse(File.getFile(4).isAccessible(5));
		
		stat.execute("DELETE FROM FILE WHERE FID = 4");
	}
	
	@Test
	public void getAllTest() throws SQLException {
		System.out.println("File in the /");
		Set<File> s1=File.getAllFile(13,"");
		assertEquals(3,s1.size());
		for(File f:s1) System.out.println(f.dir());
		
		System.out.println("File in the /hello");
		Set<File> s2=File.getAllFile(13,"/hello");
		assertEquals(2,s2.size());
		for(File f:s2) System.out.println(f.dir());
	}
}
