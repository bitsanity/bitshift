var http = require( 'http' );
var elib = require( './ETHLIB' );

// deployment-specific:
const CBA = '0x47c0a46ddefde4e32afaef1fc8e060b16406cfb2';
const TOK = '0x827fc514d3596ccdbbac6f751b409ddde8428575';
const ICO = '0x8a6992b0f614b27c5288315aad092587d9e28084';

const toklib = elib.tokcon( TOK );
const icolib = elib.icocon( ICO );

var tokrate;

//icolib.methods.tokpereth().then( (res) => {
//  tokrate = res;
//  console.log( 'tokrate: ', tokrate );
//} );

tokrate = 1500;

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
          exchange( objReq['purchase_order']['eth_amount'],
                    ethaddr(objReq['purchase_order']['bitcoin_change_acct']),
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

function exchange( ethamt, ethaddr, reqObj, resp )
{
  var weiamt = ethamt * 1000000000000000000
                         - 2000000000000000; // keep 2 finney for gas

  console.log( 'exchange: eth = ' + ethamt + ' , wei = ' + weiamt );

  elib.web3().eth.sendTransaction( {from: CBA,
                                    to: ICO,
                                    value: weiamt,
                                    gas: 100000}, (err, hash1) => {

    if (err) {
      console.log( err );
      return;
    }

    var tokcount = ethamt * tokrate; // rate is tokens/eth
    console.log( 'bought ' + tokcount + ' tokens, hash: ' + hash1 );

    tokcon.methods.transfer( ethaddr, tokcount )
                  .send( {from: CBA, gas: 100000}, (err2, hash2) => {

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
      resp.end( JSON.stringify(objRtn) );

    } );
  } );
}

var tuples = {};

function ethaddr( bitcoinaddr ) {
  return tuples[bitcoinaddr];
}

function register( bitcoinaddr, ethaddr ) {
  console.log( 'register: ', bitcoinaddr, ' <=> ', ethaddr );
  tuples[bitcoinaddr] = ethaddr;
}

