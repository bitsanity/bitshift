package bitshift.registrar;

import java.sql.*;
import java.util.*;

public class KeyStore
{
  private static Connection cx_;

  // BTCKey assumed encrypted and in Base64 format
  // ETHAddress is stored in lower-case form only

  private static final String CREATE_TABLE_CMD =
    "CREATE TABLE IF NOT EXISTS KeyStore " +
    "( BTCKey VARCHAR(64) PRIMARY KEY, " +
      "ETHAddress VARCHAR(42), " +
      "Sent BOOLEAN )";

  static
  {
    try {
      Class.forName( "org.hsqldb.jdbc.JDBCDriver" );

      cx_ = DriverManager.getConnection( "jdbc:hsqldb:file:KeyStore",
                                         "SA", "kstore" );

      Statement stmt = cx_.createStatement();
      stmt.executeUpdate( CREATE_TABLE_CMD );
      stmt.close();
    }
    catch( Exception e ) {
      System.err.println( "KeyStore: " + e.getMessage() );
      System.exit( 1 );
    }
  }

  public KeyStore() {}

  public void register( String btckey, String ethaddr ) throws Exception
  {
    if (    null == ethaddr
         || 42 > ethaddr.length()
         || !ethaddr.toLowerCase().startsWith("0x") )
    {
      System.out.println( "register: invalid address " + ethaddr );
      return;
    }

    if (hasAlreadyRegistered(ethaddr))
    {
      System.out.println( "register: already registered " + ethaddr );
      return;
    }

    Statement stmt = cx_.createStatement();
    String sql = "INSERT INTO KeyStore (BTCKey,ETHAddress,Sent) VALUES ('" +
                 btckey + "','" + ethaddr + "', FALSE )";

    stmt.executeUpdate( sql );
    stmt.close();
  }

  public boolean hasAlreadyRegistered( String ethaddr ) throws Exception
  {
    int result = 0;

    String query = "SELECT COUNT(*) FROM KeyStore " +
                   "WHERE ETHAddress='" + ethaddr.toLowerCase() + "'";

    Statement stmt = cx_.createStatement();
    ResultSet rs = stmt.executeQuery( query );
    while (rs.next())
      result = rs.getInt( 1 );
    rs.close();
    stmt.close();

    return 0 < result;
  }

  public String[] allUnsent() throws Exception
  {
    List<String> list = new ArrayList<String>();

    String query = "SELECT ETHAddress FROM KeyStore WHERE Sent=FALSE";
    Statement stmt = cx_.createStatement();
    ResultSet rs = stmt.executeQuery( query );
    while (rs.next())
      list.add( rs.getString(1) );

    rs.close();
    stmt.close();

    return list.toArray( new String[0] );
  }

  public String btcKey( String ethaddr ) throws Exception
  {
    String result = null;

    String query =
      "SELECT BTCKey FROM KeyStore WHERE ETHAddress='" + ethaddr + "'";

    Statement stmt = cx_.createStatement();
    ResultSet rs = stmt.executeQuery( query );
    while (rs.next())
      result = rs.getString( 1 );

    rs.close();
    stmt.close();

    return result;
  }

  public boolean sent( String ethaddr ) throws Exception
  {
    boolean result = false;

    String query =
      "SELECT Sent FROM KeyStore WHERE ETHAddress='" + ethaddr + "'";

    Statement stmt = cx_.createStatement();
    ResultSet rs = stmt.executeQuery( query );
    while (rs.next())
      result = rs.getBoolean( 1 );

    rs.close();
    stmt.close();

    return result;
  }

  public void setSent( String ethaddr ) throws Exception
  {
    String query =
      "UPDATE KeyStore SET Sent=TRUE WHERE ETHAddress='" + ethaddr + "'";

    Statement stmt = cx_.createStatement();
    stmt.executeUpdate( query );
    stmt.close();
  }

  public static void main( String[] args ) throws Exception
  {
    KeyStore ks = new KeyStore();

    System.out.println( "BTCKey, ETHAddress, Sent" );
    String query = "SELECT BTCKey, ETHAddress, Sent FROM KeyStore";
    Statement stmt = cx_.createStatement();
    ResultSet rs = stmt.executeQuery( query );
    while (rs.next())
      System.out.println( rs.getString(1) + ", " +
                          rs.getString(2) + ", " +
                          rs.getBoolean(3) );
    rs.close();
    stmt.close();
  }
}
