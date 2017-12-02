var http = require( 'http' );
var elib = require( './ETHLIB' );

// deployment-specific:
const CBA = '0x0000000011111111';
const TOK = '0x0123456789abcdef';
const ICO = '0xfedcba9876543210';

const toklib = elib.tokcon( TOK );
const icolib = elib.icocon( ICO );

var tokrate;
icolib.methods.tokpereth().then( (res) => { tokrate = res; } );

const responseHeader = { 'Content-Type' : 'application/json; charset=utf-8' };

if ( process.argv.length != 5 ) {
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
          exchange( objReq['purchase_order']['eth_amount'],
                    ethaddr(objReq['purchase_order']['bitcoin_change_acct']),
                    objReq,
                    resp );
        }
        else if (objReq['address_tuple'])
        {
          register( objReq['address_tuple']['bitcoin_address'],
                    objReq['address_tuple']['ethereum_address'];
        }

    } );
  }
  catch( ex )
  {
    resp.writeHead( 400, { responseHeader } );
    resp.end( '{"error" : "' + ex + '" }' );
  }

} ).listen( process.argv[2] );

function exchange( ethamt, ethaddr, reqObj, resp )
{
  elib.web3().eth.sendTransaction( {from: CB,
                                    to: ICO,
                                    value: ethamt,
                                    gas: 100000}, (err, hash1) => {

    if (err) throw err;
    var tokcount = ethamt * tokrate;
    console.log( 'bought ' + tokcount + ' tokens, hash: ' + hash1 );

    tokcon.methods.transfer( ethaddr, tokcount )
                  .send( {from: CB, gas: 100000}, (err, hash2) => {

      if (err) throw err;
      console.log( 'transferred to ' + ethaddr );

      var body = {};
      body['fulfilment'] = {};

      body['fulfilment']['bitcoin_tx_hash'] =
        objReq['purchase_order']['bitcoin_tx_hash'];

      body['fulfilment']['tok_purchase_tx_hash'] = hash1;
      body['fulfilment']['tok_transfer_tx_hash'] = hash2;

      resp.writeHead( 201, { responseHeader } );
      resp.end( JSON.stringify(objRtn) );

    } );
  } );
}

var tuples = {};

function ethaddr( bitcoinaddr ) {
  return tuples[bitcoinaddr];
}

function register( bitcoinaddr, ethaddr ) {
  tuples[bitcoinaddr] = ethaddr;
}

