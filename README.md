# bitshift

Receive Bitcoin to purchase Ethereum ERC20 tokens

## Scenario

1. Receive message to expect btc will arrive at bitcoin address specified in the message
2. Message includes an Ethereum address to receive tokens
3. Watch bitcoin blocks for payments with at least (config, default 1) confirmations
4. Calculate equivalent eth based on btceth rate retrieved from (config) external api
5. Send (config, default 99.5%) of ethereum to a configured smart contract address to purchase tokens
6. Transfer ownership of tokens from bitshift to client address

## Assumptions:

* some external process exists for ensuring bitshift's ethereum address holds enough Ether to accomdate requests
* external rate api is reliable

## Future:

* automate shapeshifting input currency to output currency
* add currencies for input
* support additional smart contract platforms (NEO, Rootstock, EOS, ...) for token purchase
* UI for monitoring and support
