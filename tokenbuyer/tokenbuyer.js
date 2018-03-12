var fs = require( 'fs' );
var http = require( 'http' );
var elib = require( './ETHLIB' );

// rinkeby:
const TOK = '0x5171E06007169f7Cf092afdD68D166f814b44A18';
const ICO = '0x79eE56a0E99657D1018Aa2258740b0378Ca1ef38';

const toklib = elib.tokcon( TOK );
const icolib = elib.icocon( ICO );

console.log( '\n=======================================================\n' );

var cba;
elib.web3().eth.getAccounts().then( (res) => {
  cba = res[0];
  console.log( 'cba: ', cba );
} );

var tokrate;

icolib.methods.tokpereth().call().then( (res) => {
  tokrate = res;
  console.log( 'tokrate: ', tokrate );
} );

const responseHeader = { 'Content-Type' : 'application/json; charset=utf-8' };

if ( process.argv.length != 3 ) {
  console.log( 'Usage: node tokenbuyer.js <port>' );
  process.exit( 1 );
}

http.createServer( (req, resp) => {

  resp.on( 'error', (err) => { console.log( 'resp error: ' + err ); } );

  try
  {
    if ( req.method != 'POST' ) throw 'only POST supported';

    let body = [];

    req.on( 'error', (err) => {
      console.log( 'request error: ' + err );
      resp.writeHead( 400, responseHeader );
      resp.end( '{"error" : "' + err + '"' );

    } ).on( 'data', (data) => {
      body.push( data );

    } ).on( 'end', () => {

        body = Buffer.concat( body ).toString();

        let objReq = JSON.parse( body );

        if (objReq['purchase_order'])
        {
          var ea = ethaddr( objReq['purchase_order']['bitcoin_acct'] );

          if (!ea) {
            console.log( "*** no registration for: ", ea );
            return;
          }

          exchange( objReq['purchase_order']['eth_amount'],
                    ea,
                    objReq,
                    resp );
        }
        else if (objReq['address_tuple'])
        {
          register( objReq['address_tuple']['bitcoin_addr'],
                    objReq['address_tuple']['ethereum_addr'] );
        }

        resp.writeHead( 201, { responseHeader } );
        resp.end( '{}' );
    } );
  }
  catch( ex )
  {
    resp.writeHead( 400, { responseHeader } );
    resp.end( '{"error" : "' + ex + '" }' );
  }

} ).listen( process.argv[2] );

function exchange( ethamt, ethaddr, objReq, resp )
{
  // reserve 4 finney for gas
  var weiamt = parseInt((ethamt * 1000000000000000000l) - 4000000000000000);

  console.log( 'exchange: eth = ' + ethamt + ' , wei = ' + weiamt );

  if (!ethaddr) {
  }

  elib.web3().eth.sendTransaction( {from: cba,
                                    to: ICO,
                                    value: weiamt,
                                    gas: 100000}, (err, hash1) => {

    if (err) {
      console.log( err );
      return;
    }

    var tokcount = ethamt * tokrate; // rate is tokens/eth
    console.log( 'bought ' + tokcount + ' tokens, hash: ' + hash1 );

    toklib.methods.transfer( ethaddr, tokcount )
                  .send( {from: cba, gas: 100000}, (err2, hash2) => {

      if (err2) {
        console.log( err2 );
        return;
      }

      console.log( 'transferred ' + tokcount + ' tokens to ' + ethaddr );

      var body = {};
      body['fulfilment'] = {};

      body['fulfilment']['bitcoin_tx_hash'] =
        objReq['purchase_order']['bitcoin_tx_hash'];

      body['fulfilment']['tok_purchase_tx_hash'] = hash1;
      body['fulfilment']['tok_transfer_tx_hash'] = hash2;

      resp.writeHead( 201, { responseHeader } );
      resp.end( JSON.stringify(body) );

    } );
  } );
}

const DBFILE = './db.txt';

var tuples = {};
var savedData = fs.readFileSync( DBFILE );
tuples = JSON.parse( savedData );

function ethaddr( bitcoinaddr ) {
  return tuples[bitcoinaddr];
}

function register( bitcoinaddr, ethaddr ) {
  console.log( bitcoinaddr, ' <=> ', ethaddr );

  tuples[bitcoinaddr] = ethaddr;

  fs.writeFile( DBFILE, JSON.stringify(tuples), function(err) {
    if (err) console.log( err );
  } );

}

