package bitshift.registrar;

import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.Hashtable;
import javax.imageio.*;

import tbox.*;

// built on: http://library.sourcerabbit.com/v/?id=19
public class WebServer
{
  private static final Executor pool_ = Executors.newFixedThreadPool( 100 );
  private static String indexhtml_;
  private static String pphrase_;

  private static String bitchangeurl_ = "http://localhost:8080/something";
  private static String tokbuyerurl_  = "http://localhost:8080/something";

  public static void main( String[] args ) throws Exception
  {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    System.out.print( "Passphrase: " );
    pphrase_ = br.readLine();

    if (null == pphrase_ || 0 == pphrase_.length())
      throw new Exception( "passphrase required." );

    // catch up on previous attempts to inform first
    KeyStore ks = new KeyStore();
    String[] todo = ks.allUnsent();

    for (int ii = 0; ii < todo.length; ii++)
    {
      String btcKey = ks.btcKey( todo[ii] );
      byte[] black = Base64.decode( btcKey );
      byte[] red =
        (new AES256(SHA256.hash(pphrase_.getBytes()))).decrypt( black );
      BitcoinAddress addr = new BitcoinAddress( red );

      try {
        inform( addr.toString(), todo[ii] );
      }
      catch( Exception e )
      {
        System.out.println( "failed to inform: " +
                            addr.toString() +
                            " => " +
                            todo[ii] );
      }
    }

    Path p = FileSystems.getDefault().getPath( ".", "index.html" );
    indexhtml_ = new String( Files.readAllBytes(p), "UTF-8" );

    ServerSocket socket = new ServerSocket( Integer.parseInt(args[0]) );

    while (true)
    {
      final Socket connection = socket.accept();

      pool_.execute( new Runnable() {
        public void run() {
          serve(connection);
        }
      } );
    }
  }
 
  private static void serve( Socket sock )
  {
    BufferedReader in;
    PrintWriter out;
    String req;
 
    try
    {
      in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
 
      req = in.readLine();
      System.out.println( "request: " + req );
 
      out = new PrintWriter( sock.getOutputStream(), true );

      if ( req.contains("favicon") )
      {
        out.println( "HTTP/1.0 200" );
        out.println( "Content-type: image/x-icon" );
      }
      else if ( req.contains("ethaddr") )
      {
        String ethaddr = req.split( "=" )[1].split( " " )[0];

        if (    null == ethaddr
             || 42 > ethaddr.length()
             || !ethaddr.toLowerCase().startsWith("0x") )
          throw new Exception( "Invalid Ethereum address: " + ethaddr );

        out.println( btcAddrPage(ethaddr) );
      }
      else
      {
        out.println( frontPage() );
      }

      out.flush();
      out.close();
      sock.close();
    }
    catch (IOException e)
    {
      System.out.println( e.getMessage() );
    }
    catch (Exception e)
    {}
    finally
    {
      try
      {
        sock.close();
      }
      catch (IOException e)
      {
        System.out.println( e.getMessage() );
      }
    }
  }
 
  private static String btcAddrPage( String ethaddr ) throws Exception
  {
    StringBuffer buff = new StringBuffer();

    buff.append( "HTTP/1.0 200\n" );
    buff.append( "Content-type: text/html\n" );
    buff.append( "Server-name: bitshift.registrar.WebServer\n" );

    StringBuffer htmlbuff = new StringBuffer();

    htmlbuff
      .append( "<html>\n" )
      .append( "<head>\n" )
      .append( "  <title>Registrar</title>\n" )
      .append( "</head>\n" )
      .append( "<h2>Buy Tokens with Bitcoin</h2>\n" );

    KeyStore ks = new KeyStore();

    BitcoinAddress btcaddr = null;
    AES256 aes = new AES256( SHA256.hash(pphrase_.getBytes()) );

    if (ks.hasAlreadyRegistered(ethaddr))
    {
      String s = ks.btcKey( ethaddr );
      byte[] blk = Base64.decode( s );
      byte[] red = aes.decrypt( blk );
      btcaddr = new BitcoinAddress( red );
    }
    else
    {
      ECKeyPair keypair = ECKeyPair.makeNew();
      btcaddr = new BitcoinAddress( keypair.privatekey() );
      String black = Base64.encode( aes.encrypt(keypair.privatekey()) );

      ks.register( black, ethaddr );
      inform( btcaddr.toString(), ethaddr );
    }

    htmlbuff.append( "Please send your BTC to this address:\n<p/>\n" );

    BufferedImage bi = QR.encode( btcaddr.toString(), 300 ); // pixels
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write( bi, "png", baos );
    String pngB64 = Base64.encode( baos.toByteArray() );

    htmlbuff.append( "<img alt=\"QR code\" src=\"data:image/png;base64," )
            .append( pngB64 )
            .append( "\" />\n<p/>\n" )
            .append( btcaddr.toString() )
            .append( "\n<p/>\n" )
            .append( "</body>\n" )
            .append( "</html>" );

    buff.append( "Content-length: " + htmlbuff.length() + "\n" );
    buff.append( "\n" );

    buff.append( htmlbuff );

    return buff.toString();
  }

  private static String frontPage() throws Exception
  {
    StringBuffer buff = new StringBuffer();

    buff.append( "HTTP/1.0 200\n" );
    buff.append( "Content-type: text/html\n" );
    buff.append( "Server-name: bitshift.registrar.WebServer\n" );
    buff.append( "Content-length: " + indexhtml_.length() + "\n" );
    buff.append( "\n" );

    buff.append( indexhtml_ );

    return buff.toString();
  }

  private static void inform( String btcaddr, String ethaddr ) throws Exception
  {
    String body =
      "{" +
        "\"address_tuple\":{" +
          "\"bitcoin_addr\":\"" + btcaddr + "\"," +
          "\"ethereum_addr\":\"" + ethaddr + "\"" +
        "}" +
      "}";

    int rc = post( bitchangeurl_, body );
    if (rc > 409)
      throw new Exception( "bitchange is down." );

    rc = post( tokbuyerurl_, body );
    if (rc != 201)
      throw new Exception( "tokenbuyer is down." );

    new KeyStore().setSent( ethaddr );
  }

  private static int post( String toURL, String body ) throws Exception
  {
    byte[] out = body.getBytes( "UTF-8" );

    HttpURLConnection http =
      (HttpURLConnection)(new URL(toURL)).openConnection();
    http.setConnectTimeout(10000);
    http.setRequestMethod( "POST" );
    http.setDoOutput( true );
    http.setAllowUserInteraction( false );
    http.setFixedLengthStreamingMode( out.length );
    http.setRequestProperty( "Content-type",
                             "application/json; charset=UTF-8" );
    http.connect();
    http.getOutputStream().write( out );

    BufferedReader br =
      new BufferedReader(new InputStreamReader(http.getInputStream()));

    StringBuffer respBuff = new StringBuffer();
    String line = null;
    while (null != (line = br.readLine()))
      respBuff.append( line );

    br.close();

    line = respBuff.toString().split("\n")[0];
    int rcode = Integer.parseInt( line.split(" ")[1] );

    System.out.println( toURL + " " + rcode );

    return rcode;
  }

}

