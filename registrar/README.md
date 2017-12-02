# Bitshift Registrar

Simple web interface enabling user to purchase Ethereum token(s) using Bitcoin.

Scenario:

1. user provides an Ethereum address to receive tokens

2. Interface generates a new random Bitcoin P2PKH address and displays QR

3. The registrar informs bitchange to watch the Bitcoin blockchain for payments
   to that address

4. The registrar informs tokenbuyer of the Bitcoin/Ethereum address tuple.

5. Bitcoin payment arrives at the registered Bitcoin address

6. bitchange notices, informs tokenbuyer that payment has arrived and how
   much that is in Ether at current market rate

7. tokenbuyer spends Ether to buy tokens from the ICO contract

8. tokenbuyer calls the token contract to transfer the tokens to the
   corresponding Ethereum address

