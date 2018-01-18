package bitshift.sweeper;

import java.sql.*;
import java.util.*;

import tbox.*;

public class Sweeper
{
  private static Connection cx_;

  //
  // MAIN
  //
  public static void main( String[] args ) throws Exception
  {
    Class.forName( "org.hsqldb.jdbc.JDBCDriver" );
    cx_ = DriverManager.getConnection(
      "jdbc:hsqldb:file:KeyStore", "SA", "kstore" );

    String pphrase = promptPassphrase();
    String[] pkeyHexs = privateKeys( pphrase );
    for (int ii = 0; ii < pkeyHexs.length; ii++)
    {
      byte[] pkey = HexString.decode( pkeyHexs[ii] );

      System.out.println( "Address:\t" + new BitcoinAddress(pkey).toString() +
                          "key (WIF):\t" + WIF.toWIF(pkey) );
    }
  }

  private static String promptPassphrase() throws Exception
  {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    System.out.print( "Passphrase: " );
    String result = br.readLine();
    if (null == result || 0 == result.length())
      throw new Exception( "passphrase required." );

    return result;
  }

  private static String[] privateKeys( String pphrase ) throws Exception
  {
    List<String> result = new ArrayList<String>();

    AES256 aes = new AES256( SHA256.hash(pphrase.getBytes()) );

    Statement stmt = cx_.createStatement();
    ResultSet rs = stmt.executeQuery( "SELECT BTCKey FROM KeyStore" );
    while (rs.next())
    {
      byte[] black = tbox.Base64.decode( rs.getString(1) );
      byte[] redkey = aes.decrypt( black );
      result.add( HexString.encode(redkey) );
    }

    return result.toArray( new String[0] );
  }

}

