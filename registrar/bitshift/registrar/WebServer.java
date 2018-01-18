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
  private static String pphrase_;

  private static String bitchangeurl_ = "http://localhost:8080/something";
  private static String tokbuyerurl_  = "http://localhost:8080/something";

  private static String tokaddr_;
  private static String tokname_;

  public static void main( String[] args ) throws Exception
  {
    if (null == args || 3 != args.length)
      throw new Exception( "Usage: <port> <tokaddr> <tokname>" );

    tokaddr_ = args[1];
    tokname_ = args[2];

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

    ServerSocket socket = new ServerSocket( Integer.parseInt(args[0]) );

    tokaddr_ = args[1];
    tokname_ = args[2];

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
      out = new PrintWriter( sock.getOutputStream(), true );

      if ( req.contains("favicon") )
      {
        out.println( "HTTP/1.0 200" );
        out.println( "Content-type: image/x-icon" );
      }
      else if (    req.contains("ethaddr")
                && req.contains("qid")
                && req.contains("answer")
              )
      {
        System.out.println( "request: " + req + " from " +
                            sock.getRemoteSocketAddress().toString() + "\n" );

        String parms = req.split( "\\?" )[1].split(" ")[0];
        String[] nvpairs = parms.split( "&" );
        String ethaddr = nvpairs[0].split( "=" )[1];
        String qix = nvpairs[1].split( "=" )[1];
        String answer = nvpairs[2].split( "=" )[1];

        if (    null == ethaddr
             || 0 == ethaddr.length()
             || 42 > ethaddr.length()
             || !ethaddr.toLowerCase().startsWith("0x") )
          throw new Exception( "Invalid Ethereum address: " + ethaddr );

        out.println( btcAddrPage(ethaddr,qix,answer) );
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
    {
      System.out.println( e.getMessage() );
      e.printStackTrace();
    }
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
 
  private static String btcAddrPage( String ethaddr, String qix, String answer )
  throws Exception
  {
    if (null == ethaddr || 0 == ethaddr.length())
      throw new Exception( "ETH address is mandatory." );

    if (null == qix || 0 == qix.length())
      throw new Exception( "Suspicious lack of question index." );

    if (null == answer || 0 == answer.length())
      throw new Exception( "Need an answer" );

    if (!BrainDeadCaptcha.check(qix, answer) )
      throw new Exception( "Invalid answer" );

    StringBuffer buff = new StringBuffer();

    buff.append( "HTTP/1.0 200\n" );
    buff.append( "Content-type: text/html\n" );
    buff.append( "Server-name: bitshift.registrar.WebServer\n" );

    StringBuffer htmlbuff = new StringBuffer();

    htmlbuff
      .append( "<!DOCTYPE html>\n" )
      .append( "<html>\n" )
      .append( "<head>\n" )
      .append( "  <title>Registrar</title>\n" )
      .append( "</head>\n" )
      .append( "<body>\n" );

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

    htmlbuff.append( "<div id=\"sendlabel\">\n" )
            .append( "Please send BTC to this address:\n</div><p/>\n" );

    BufferedImage bi = QR.encode( btcaddr.toString(), 300 ); // pixels
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write( bi, "png", baos );
    String pngB64 = Base64.encode( baos.toByteArray() );

    htmlbuff.append( "<img alt=\"QR code\" src=\"data:image/png;base64," )
            .append( pngB64 )
            .append( "\" />\n<p/>\n" )
            .append( "<div id=\"btcaddress\">" )
            .append( btcaddr.toString() )
            .append( "</div>\n<p/>\n" )
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

    StringBuffer html = new StringBuffer();
    html.append( "<!DOCTYPE html>\n" )
        .append( "<html>\n" )
        .append( "<head>\n" )
        .append( "  <title>Registrar</title>\n" )
        .append( "</head>\n" )
        .append( "<body>\n" )
        .append( "  <div id=\"heading\">\n" )
        .append( "  Buy " + tokname_ + " with Bitcoin\n" )
        .append( "  </div>\n<p/>\n" )
        .append( "  <div id=\"scalabel\">\n" )
        .append( "  from smart contract:\n" )
        .append( "  </div>\n" )
        .append( "  <div id=\"sca\">\n  " )
        .append( tokaddr_ )
        .append( "</div>\n<p/>\n" )
        .append( "  <form action=\"/registrar\" method=\"GET\">\n" )
        .append( "  <div id=\"ethaddrprompt\">\n" )
        .append( "  Ethereum address to receive the tokens:<br/>\n" )
        .append( "  </div>\n" )
        .append( "  <input type=\"text\" name=\"ethaddr\" size=\"42\"" )
        .append(         " maxlength=\"42\" />\n" )
        .append( "  <p/>\n" )
        .append( "  <div id=\"captchalabel\">\n" )
        .append( "    Are you human? Please answer this question:\n" )
        .append( "  </div><br>\n    " )
        .append( new BrainDeadCaptcha().toString() )
        .append( "\n  <p/>\n" )
        .append( "  <input type=\"submit\" value=\"Next\" />\n" )
        .append( "</form>\n" )
        .append( "</body>\n" )
        .append( "</html>" );

    buff.append( "HTTP/1.0 200\n" )
        .append( "Content-type: text/html\n" )
        .append( "Server-name: bitshift.registrar.WebServer\n" )
        .append( "Content-length: " + html.toString().length() + "\n" )
        .append( "\n" )
        .append( html.toString() );

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

