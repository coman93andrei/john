package aidhunger.services;

import javax.jws.WebService;

import org.apache.tomcat.util.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@WebService(endpointInterface = "aidhunger.services.AidHunger")
public class MyAidHunger implements AidHunger {

	// Generating Salt with Random Object and Hashing
	private Random r = new SecureRandom();
	private byte[] salt = new byte[32];
	private MessageDigest digest;
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://athena01.fhict.local/dbi270246";

	// Database credentials
	static final String USER = "dbi270246";
	static final String PASS = "6ky8PnYTdu";
	
	// Database Connection
	private Connection conn;
	private Statement stmt;

	@Override
	public boolean login(String username, String password) {
		// TODO Auto-generated method stub
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();
			digest = MessageDigest.getInstance("SHA-256");
			String sql = "SELECT Password, Salt FROM users WHERE UserName='" + username+"'";
			ResultSet rs = stmt.executeQuery(sql);
			String encodedSalt = "";
			String encryptedPassword = "";
			while (rs.next()) {
				encryptedPassword = rs.getString("Password");
				encodedSalt = rs.getString("Salt");
			}
			rs.close();
			String comparePassword = bytesToHex(digest.digest((password + encodedSalt).getBytes("UTF-8")));
			return encryptedPassword.equals(comparePassword);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean register(String username, String password) {
		// TODO Auto-generated method stub
		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");
			// STEP 3: Open a connection
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			// STEP 4: Execute a query
			stmt = conn.createStatement();
			digest = MessageDigest.getInstance("SHA-256");
			r.nextBytes(salt);
			String encodedSalt = Base64.encodeBase64String(salt);
			String sql = "INSERT INTO users(UserName, Password, Salt) " + "VALUES ('" + username + "', '"
					+ bytesToHex(digest.digest((password + encodedSalt).getBytes("UTF-8"))) + "', '" + encodedSalt + "')";
			stmt.executeUpdate(sql);
			return true;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}
