package tk.circuitcoder.grafixplane;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigTest {
	Config subject;
	
	@Before
	public void setup() {
		subject=new Config();
		subject.set("Entry1", "Value");
		subject.setInt("Entry2", 100);
	}
	
	@Test
	public void getTest() {
		assertEquals("Value",subject.get("Entry1"));
	}
	
	@Test
	public void getIntTest() {
		assertEquals(100,subject.getInt("Entry2").intValue());
	}
	
	@Test
	public void getNull() {
		assertNull(subject.get("Entry3"));
	}
	
	@Test
	public void getIntNull() {
		assertNull(subject.get("Entry3"));
	}
	
	@Test
	public void setIntTest() {
		Random rand=new Random();
		int value=rand.nextInt();
		assertEquals("100",subject.setInt("Entry2",value));
		assertEquals(value,subject.getInt("Entry2").intValue());
	}
}
