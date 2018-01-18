const fs = require('fs');
const Web3_ = require('web3');
const web3_ =
  new Web3_(new Web3_.providers.HttpProvider("http://localhost:8545"));

exports.web3 = function() { return web3_; }

exports.tokABI = function() {
  var contents =
    fs.readFileSync('./ERC223Token_sol_ERC223Token.abi').toString();

  var abiObj = JSON.parse(contents);
  return abiObj;
}

exports.icoABI = function() {
  var contents = fs.readFileSync('./RTKICO_sol_RTKICO.abi').toString();
  var abiObj = JSON.parse(contents);
  return abiObj;
}

exports.tokcon = function(sca) {
  return new web3_.eth.Contract( exports.tokABI(), sca );
}

exports.icocon = function(sca) {
  return new web3_.eth.Contract( exports.icoABI(), sca );
}
