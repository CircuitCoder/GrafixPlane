package tk.circuitcoder.grafixplane;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.BeforeClass;
import org.junit.Test;

import tk.circuitcoder.grafixplane.Mailbox.WrappedMail;
import tk.circuitcoder.grafixplane.User.AccessLevel;
import tk.circuitcoder.grafixplane.User.NameExistsException;
import tk.circuitcoder.grafixplane.User.UIDExistsException;
import tk.circuitcoder.grafixplane.db.H2DatabaseManager;

public class MailTest {
	@BeforeClass
	public static void initDB() throws ClassNotFoundException, SQLException, NameExistsException, UIDExistsException {
		H2DatabaseManager manager=new H2DatabaseManager();
		manager.startDB(9750,false,false);
		Connection conn=manager.getConn("", "", "mem:TEST", false);
		Statement stat=conn.createStatement();
		stat.execute("CREATE TABLE USER ("
				+ "UID int(10) UNSIGNED,"
				+ "Username varchar,"
				+ "Passwd varchar,"
				+ "AccessLevel tinyint(3) UNSIGNED,"
				+ "BoxCount tinyint UNSIGNED"
				+ ");");
		
		stat.execute("CREATE TABLE MAIL ("
				+ "MID int(10) UNSIGNED,"
				+ "Sender int(10) UNSIGNED,"
				+ "Receiver varchar,"
				+ "Content varchar,"
				+ "Attachment varchar,"
				+ "ReplyTo int(10) UNSIGNED"
				+ ");");
		stat.execute("CREATE TABLE MAILBOX ("
				+ "UID int(10) UNSIGNED,"
				+ "Mails varchar,"
				+ "BoxCount smallint,"
				+ "MailCount smallint,"
				+ ");");
		
		conn.prepareStatement("INSERT INTO MAIL VALUES (1234,1,'2|3','Nothing','1',0)").execute();
		Mail.init(conn);
		User.init(conn);
		User.newUser(1, "Hi", "1", AccessLevel.ADMIN);
		User.newUser(2, "Hello", "2", AccessLevel.ADMIN);
		User.newUser(3, "Nice", "3", AccessLevel.ADMIN);
		User.newUser(4, "Jone", "4", AccessLevel.ADMIN);
	}
	
	@Test
	public void getMailTest() throws SQLException {
		Mail mail=Mail.getMail(1234);
		assertNotNull(mail);
		assertTrue(mail.receivers.contains(User.getUser(2)));
		assertTrue(mail.receivers.contains(User.getUser(3)));
		assertEquals(User.getUser(1),mail.sender);
		assertEquals("Nothing", mail.content);
		assertEquals(0, mail.replyTo);
	}

	@Test
	public void parseMailTest() {
		WrappedMail mail=Mailbox.parseMail("1234,UF'F4@3|");
		assertNotNull(mail);
		assertTrue(mail.unread());
		assertTrue(mail.flagged());
		assertFalse(mail.deleted());
		assertEquals(3, mail.getBox());
		assertEquals('F', mail.getType());
		assertEquals(4, mail.getTData());
		assertEquals(mail.getMail().MID, 1234);
	}

	@Test
	public void genMailStrTest() {
		WrappedMail mail=Mailbox.parseMail("1234,UF'F4@3|");
		assertEquals("1234,UF'F4@3|",Mailbox.genMailStr(mail));
	}
}
