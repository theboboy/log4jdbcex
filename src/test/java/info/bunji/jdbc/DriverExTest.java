package info.bunji.jdbc;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

public class DriverExTest {

	public static String acceptUrl = "jdbc:log4jdbcex:h2:~/test;SCHEMA=INFORMATION_SCHEMA";
	public static String realUrl   = "jdbc:h2:~/test;SCHEMA=INFORMATION_SCHEMA";

	private static Driver driver;
	private static Properties prop = new Properties();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		driver = DriverManager.getDriver(acceptUrl);
		prop.setProperty("user", "sa");
		prop.setProperty("password", "");
	}

//	@Test
//	public void testExtractSettingPath() {
//		fail("まだ実装されていません");
//	}

	@Test
	public void testAcceptsURL() throws Exception {
		assertThat(driver.acceptsURL(acceptUrl), is(true));
		assertThat(driver.acceptsURL(realUrl), is(false));
	}

	@Test
	public void testConnect() throws Exception {
		assertThat(driver.connect(acceptUrl, prop), is(notNullValue()));
		assertThat(driver.connect(realUrl, prop), is(nullValue()));
		assertThat(driver.connect(realUrl, new Properties()), is(nullValue()));
	}

	@Test
	public void testGetMajorVersion() {
		Driver d = new DriverEx();
		assertThat(d.getMajorVersion(), is(1));
		Driver h2 = new org.h2.Driver();
		assertThat(driver.getMajorVersion(), is(h2.getMajorVersion()));
	}

	@Test
	public void testGetMinorVersion() {
		Driver d = new DriverEx();
		assertThat(d.getMinorVersion(), is(0));
		Driver h2 = new org.h2.Driver();
		assertThat(driver.getMinorVersion(), is(h2.getMinorVersion()));
	}

	@Test
	public void testGetPropertyInfo() throws Exception {
		Driver realDriver = DriverManager.getDriver(realUrl);
		realDriver.getPropertyInfo(realUrl, prop);
		assertThat(driver.getPropertyInfo(acceptUrl, prop), is(realDriver.getPropertyInfo(realUrl, prop)));

		Driver d = new DriverEx();
		assertThat(d.getPropertyInfo("", prop), is(new DriverPropertyInfo[0]));
	}

	@Test
	public void testJdbcCompliant() {
		Driver h2 = new org.h2.Driver();
		assertThat(driver.jdbcCompliant(), is(h2.jdbcCompliant()));
	}
}
