/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.bitcoin;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.bitsanity.bitchange.canonical.purchase.PurchaseOrder;
import com.bitsanity.bitchange.concurrent.MdcForkJoinPool;
import com.bitsanity.bitchange.server.spring_boot.RestOperationsService;
import com.bitsanity.bitchange.server.spring_boot.bitcoin.quote.Quote;
import com.bitsanity.bitchange.server.spring_boot.jmx.ExchangerServerStatistics;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

/**
 * @author lou.paloma
 *
 */
@Service
public class BitcoinExchangeService {
	
	private static final String PROCESSED_MEMO_PREFACE = "[Processed: ";
	private static final String PROCESSED_MEMO_SUCCESS_PREFACE = PROCESSED_MEMO_PREFACE + "SUCCESS: ";
	private static final String PROCESSED_MEMO_SUCCESS_FORMAT = PROCESSED_MEMO_SUCCESS_PREFACE + "{0}]";
	private static final String PROCESSED_MEMO_FAILURE_PREFACE = PROCESSED_MEMO_PREFACE + "FAILURE: ";
	private static final String PROCESSED_MEMO_FAILURE_FORMAT = PROCESSED_MEMO_FAILURE_PREFACE + "{0}]";
	
	@Value("${bitcoin.network}")
	private String network;
	@Value("${bitcoin.wallet.location}")
	private String walletLocation;
	@Value("${bitcoin.wallet.name}")
	private String walletRootName;
	@Value("${bitcoin.addresses:#{null}}")
	private Optional<String[]> addressList;
	
	@Value("${bitcoin.listenerPoolSize:10}")
	private int poolSize;
	@Value("${bitcoin.confirmationThreshold:1}")
	private int confirmationThreshold;
	
	/**
	 * Default value is {@link Transaction#REFERENCE_DEFAULT_MIN_TX_FEE}
	 */
	@Value("${bitcoin.brokerageFee:5000}")
	private long brokerageFee;
	@Value("${ethereum.buyer.url}")
	private String ethUrl;
	
	@Autowired
	private MarketService market;
	@Autowired
	private RestOperationsService restService;
	@Autowired
	private ExchangerServerStatistics statistics;

	//move to within new component to track submission stamp and allow for JMX, including avg/max wait time
	//private ConcurrentSkipListSet<String> pendingTransactions = new ConcurrentSkipListSet<>();
	@Autowired
	private PendingTransactionTracker pendingTransactions;
	
	private WalletAppKit kit;
	private Coin brokerageCoin;

	private MdcForkJoinPool backLogPool;

	private static final Logger LOGGER = CustomLoggerFactory.getLogger(BitcoinExchangeService.class);
	
	@PostConstruct
	public void init() {
		//validate brokerageFee is not negative
		try {
			brokerageCoin = Coin.valueOf(brokerageFee);
			if (brokerageCoin.isNegative() ) {
				throw new RuntimeException("Invalid negative brokerage fee value provided: " + brokerageFee);
			}
		} catch (IllegalArgumentException e) {
			//coin parsing error
			throw new RuntimeException("Unable to parse brokerage fee value: " + brokerageFee, e);
		}
		
		NetworkParameters params;
		if (network.equals("testnet")) {
		    params = TestNet3Params.get();
		} else if (network.equals("regtest") || network.equals("cypher-regtest")) {
		    params = RegTestParams.get();
		} else if (network.equals("mainnet")) {
		    params = MainNetParams.get();
		} else {
			String msg = "Invalid Bitcoin network specified: " + network;
			LOGGER.error(msg);
			
			//throw FATAL exception
			throw new RuntimeException(msg);
		}
				
		// Start up a basic app using a class that automates some boilerplate. Ensure we always have at least one key.
		kit = new WalletAppKit(params, new File(walletLocation), walletRootName) {
		    @Override
		    protected void onSetupCompleted() {
		        // This is called in a background thread after startAndWait is called, as setting up various objects
		        // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
		        // on the main thread.
		        if (wallet().getKeyChainGroupSize() < 1) {
		            wallet().importKey(new ECKey());
		        }
		    }
		};
		
		if (network.equals("regtest")) {
		    // Regression test mode is designed for testing and development only, so there's no public network for it.
		    // If you pick this mode, you're expected to be running a local "bitcoind -regtest" instance.
		    kit.connectToLocalHost();
		} else if (network.equals("cypher-regtest")) {
			//FIXME -- not working
		    try {
		    	//Blockcypher
		        //PeerAddress pa = new PeerAddress(params, InetAddress.getByName("https://api.blockcypher.com/v1/bcy/test"));
		        //PeerAddress pa = new PeerAddress(params, InetAddress.getByName("https://api.blockcypher.com/v1/btc/test3"));
		        //PeerAddress pa = new PeerAddress(params, InetAddress.getByName("api.blockcypher.com"));
		    	
		    	//TP's TestNet Faucet -- https://tpfaucet.appspot.com/ -- coin return address: n2eMqTT929pb1RDNuqEnxdaLau1rxy3efi
		        PeerAddress pa = new PeerAddress(params, InetAddress.getByName("52.4.156.236"), 18333);
		        
		        kit.setPeerNodes(pa);
		        //kit.peerGroup().addAddress(pa);
		    } catch (UnknownHostException e) {
		        //FATAL
		    	throw new RuntimeException("Unable to connect to regtest network");
		    }
		}

		// Download the block chain and wait until it's done.
		kit.startAsync();
		kit.awaitRunning();
        LOGGER.info("Wallet AppKit synchronized and running.");
        
        //Print out some state info
        LOGGER.info("Current wallet balance: " + kit.wallet().getBalance().toFriendlyString());
        if (kit.wallet().getWatchedAddresses().isEmpty()) {
        	Address addr = kit.wallet().freshReceiveAddress();
        	kit.wallet().addWatchedAddress(addr);
        }

        if (addressList.isPresent()) {
        	Arrays.asList(addressList.get()).forEach(addr -> {
        		Address address = Address.fromBase58(params, addr);
        		if (!kit.wallet().isAddressWatched(address)) {
        			LOGGER.audit("Adding NEW watched address: " + address.toBase58());
        			kit.wallet().addWatchedAddress(address);
        		}
        	});
        	
        }
        LOGGER.audit("Current watched addresses: " +  kit.wallet().getWatchedAddresses());
        statistics.setWatchedAddresses(kit.wallet().getWatchedAddresses().stream().map(Address::toBase58).collect(Collectors.toSet()));        

        //process "hung" transactions
        backLogPool = new MdcForkJoinPool(poolSize, true, "btc-backlog-pool");
        retryPurchases();
        		
		//add wallet listener
        MdcForkJoinPool receiverPool = new MdcForkJoinPool(poolSize, true, "btc-receiver-pool");
        kit.wallet().addCoinsReceivedEventListener(receiverPool, new WalletCoinsReceivedEventListener() {
            @SuppressWarnings("deprecation")
			@Override
            public void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
            	//add to processing list to prevent retry double processing
            	pendingTransactions.add(tx.getHashAsString());
            	
                // The transaction "tx" can either be pending, or included into a block (we didn't see the broadcast).
                Coin value = tx.getValueSentToMe(w);
                LOGGER.audit("Received transaction for " + value.toFriendlyString() + ": " + tx);
                if (tx.getInputs().isEmpty()) {
                    LOGGER.audit("Received transaction for " + value.toFriendlyString() + " from [No Input Address]: " + tx + ", awaiting confidence threshold.");
                } else {
                    LOGGER.audit("Received transaction for " + value.toFriendlyString() + " from [" + tx.getInputs().get(0).getFromAddress() + "], hash: " + tx.getHashAsString()
                    	+ ", awaiting confidence threshold.");
                    LOGGER.debug("Received transaction for " + value.toFriendlyString() + " from [" + tx.getInputs().get(0).getFromAddress() 
                    	+ "], awaiting confidence threshold.\": " + tx);
                }
                
                //System.err.println("Amount sent :" + tx.getValue(w));
                //System.err.println("Amount sent to me:" + tx.getValueSentToMe(w));
                //System.err.println("Amount sent from me:" + tx.getValueSentFromMe(w));
                //System.err.println("Wallet outputs:" + tx.getWalletOutputs(w));

                //update stats:  received++, pending++
                statistics.addCoinReceipt(value.longValue());
                statistics.incrementPendingReceipts();

                // Wait until it's made it into the block chain (may run immediately if it's already there).
                Futures.addCallback(tx.getConfidence().getDepthFuture(confirmationThreshold), new FutureCallback<TransactionConfidence>() {
                    @Override
                    public void onSuccess(TransactionConfidence result) {
                        //update stats:  pending--, processing++
                        statistics.decrementPendingReceipts();
                        statistics.incrementProcessingExchange();

                    	LOGGER.audit("Confidence met for transaction " + tx.getHashAsString());
                        
                    	try {
							purchaseCoins(tx, false);
						} catch (Throwable e) {
							//log and leave for later processing
							LOGGER.error("Unable to purchase coins.", e);
						} finally {
							//remove from processing list
							pendingTransactions.remove(tx.getHashAsString());
						}
                    }

                    @Override
                    public void onFailure(Throwable t) {
						//remove from processing list
						pendingTransactions.remove(tx.getHashAsString());

						//update stats:  pending--, failed++
                        statistics.decrementPendingReceipts();
                        statistics.incrementFailedReceipts();

                    	// This kind of future can't fail, just rethrow in case something weird happens.
                        throw new RuntimeException(t);
                    }
                }, receiverPool);
            }
        });
	}
	
	@PreDestroy
	public void destroy() throws TimeoutException {
		LOGGER.info("Shuttting down Bitcoin wallet...");
		kit.stopAsync();
		kit.awaitTerminated(30, TimeUnit.SECONDS);
	}
	
	public List<Address> getWatchedAddresses() {
		return Collections.unmodifiableList(kit.wallet().getWatchedAddresses());
	}
	
	public void retryPurchases() {
        kit.wallet().getTransactions(true).stream().forEach(tx -> {
        	StringBuilder toString = new StringBuilder(tx.toString());
        	toString.append("     memo ").append(tx.getMemo()).append('\n');
        	//System.err.println(toString.toString());
        });
        List<Transaction> outstandingTx = kit.wallet().getTransactions(true).stream()
        		.filter(t -> (
        				//no memo
        				StringUtils.isEmpty(t.getMemo()) 
        				//or doesn't start with success preface
        				|| !t.getMemo().startsWith(PROCESSED_MEMO_SUCCESS_PREFACE)
        			)
        		).collect(Collectors.toList());
        LOGGER.audit("Found " + outstandingTx.size() + " transactions in backlog.  Attempting to process.");
        if (!outstandingTx.isEmpty()) {
            //try {
				backLogPool.submit(() -> {
    				outstandingTx.parallelStream().forEach(t -> {
    					//process only if not already in queue
    					if (!pendingTransactions.contains(t.getHashAsString())) {
    						//check current confidence
    						if (t.getConfidence().getDepthInBlocks() >= confirmationThreshold ) {
    			            	//add to processing list to prevent retry double processing
    			            	pendingTransactions.add(t.getHashAsString());
    							
    							//update stats:  processing++
    							statistics.incrementProcessingExchange();
    							
    							//purchaseCoins
    							LOGGER.audit("Outstanding Transaction found that CAN be processed, id: " + t.getHashAsString() + ", current confidence: " 
    									+ t.getConfidence().getDepthInBlocks() + ", current memo: " + t.getMemo());
    							try {
    								purchaseCoins(t, true);
    							} catch (Throwable e) {
    								//log and leave for later processing
    								LOGGER.error("Unable to purchase coins.", e);
    							} finally {
    								//remove from processing list
    								pendingTransactions.remove(t.getHashAsString());
    							}
    						} else {
    							//just wait for retry cycle
    							LOGGER.warn("Outstanding Transaction found that CANNOT be processed, id: " + t.getHashAsString() + ", current confidence: " 
    									+ t.getConfidence().getDepthInBlocks() + ", current memo: " + t.getMemo());
    						}
    					}
    				});
				})
				//TODO get() needed?
				//.get()
				;
			//} catch (InterruptedException | ExecutionException e) {
			//}
        }
	}

	public boolean addWatchedAddress(String address) throws AddressException {
		Address addr;
		try {
			addr = Address.fromBase58(kit.params(), address);
		} catch (AddressFormatException e) {
			String msg = "Trying to add invalid address to watch: " + address + "; " + e.getMessage();
			LOGGER.error(msg, e);
			throw new AddressException(msg, e);
		}
		if (kit.wallet().isAddressWatched(addr)) {
			return false;
		}
		
		try {
			boolean retVal = kit.wallet().addWatchedAddress(addr);
	        LOGGER.audit("Current watched addresses: " +  kit.wallet().getWatchedAddresses());
	        statistics.addWatchedAddress(address);
	        return retVal;
		} catch (Throwable e) {
			String msg = "Error while trying to add new watch address: " + address;
			LOGGER.error(msg, e);
			throw new AddressException(msg, e);
		}
	}

	public boolean removeWatchedAddress(String address) throws AddressException {
		Address addr;
		try {
			addr = Address.fromBase58(kit.params(), address);
		} catch (AddressFormatException e) {
			String msg = "Trying to remove invalid watch address: " + address;
			LOGGER.error(msg, e);
			throw new AddressException(msg, e);
		}
		if (!kit.wallet().isAddressWatched(addr)) {
			return false;
		}
		
		try {
			boolean retVal = kit.wallet().removeWatchedAddress(addr);
	        LOGGER.audit("Current watched addresses: " +  kit.wallet().getWatchedAddresses());
	        statistics.removeWatchedAddress(address);
	        return retVal;
		} catch (Throwable e) {
			String msg = "Error while trying to remove watch address: " + address;
			LOGGER.error(msg, e);
			throw new AddressException(msg, e);
		}
	}

	private void purchaseCoins(Transaction tx, boolean retry) throws ExchangeException {
		Coin value = tx.getValueSentToMe(kit.wallet());
		LOGGER.debug("Purchasing coins for : " + value.toFriendlyString() + " for transaction: " + tx);

		//deduct fee
		if (value.isLessThan(brokerageCoin)) {
			String msg = MessageFormat.format("Amount to be purchased {0} is smaller than the brokerage fee {1}.  Purchase cannot be completed.", value.toFriendlyString(),
				brokerageCoin);
			LOGGER.error(msg);
			throw new ExchangeException(msg);
		}
		value = value.subtract(brokerageCoin);
		LOGGER.info("Purchasing coins (after brokerage cost) for: " + value.toFriendlyString() + " for transaction: " + tx);
		
		//get Ethereum exchange rate
		Quote quote = market.getMostRecentQuote(value);
		if (quote == null) {
			//no quote is available, throw exception
			throw new ExchangeException("No quote is availble for value: " + value);
		}
		
		//deprecated to handle multiple input addresses
		//extract change account address - use 1st output not matching incoming/this address
		/*
		Optional<TransactionOutput> outputAddress = tx.getOutputs().stream().filter(to -> !to.getAddressFromP2PKHScript(to.getParams()).toBase58().equals(address)).findFirst();
		if (!outputAddress.isPresent()) {
			List<String> outputList = tx.getOutputs().stream().map(to -> to.getAddressFromP2PKHScript(to.getParams()).toBase58()).collect(Collectors.toList());
			System.err.println("Listening Address: "  + address + ", outputs: " + outputList);
			//throw execption
			String msg = "No change/source address is supplied with transaction.  Aborting.";
			LOGGER.error(msg + "Transaction details: " + tx.toString());
			throw new RuntimeException(msg);
		}
		*/
		
		//determine listening and change addresses from output
		List<String> outputAddresses = tx.getOutputs().stream().map(to -> to.getAddressFromP2PKHScript(to.getParams()).toBase58()).collect(Collectors.toList());
		List<String> listeningAddresses = kit.wallet().getWatchedAddresses().stream().map(Address::toBase58).collect(Collectors.toList());
		listeningAddresses.retainAll(outputAddresses);
		if (listeningAddresses.isEmpty()) {
			//no matching listening address found in the output, throw exception
			throw new ExchangeException("No known listening address found: " + outputAddresses);
		} else if (listeningAddresses.size() != 1) {
			//multiple matching listening addresses found in the output, throw exception -- NOT POSSIBLE?
			throw new ExchangeException("Multiple listening addresses found, ONLY 1 allowed: " + outputAddresses);
		}
		outputAddresses.removeAll(listeningAddresses);
		if (outputAddresses.isEmpty()) {
			//no change address found in the output, throw exception
			throw new ExchangeException("No change/source address found in the outputs: " + tx.getOutputs());
		} else if (outputAddresses.size() != 1) {
			//multiple output addresses found in the output, throw exception -- NOT POSSIBLE?
			throw new ExchangeException("Multiple change/source addresses found beyond known listening address: " + tx.getOutputs());
		}
		
		//create request for purchase
		PurchaseOrder order = new PurchaseOrder(tx.getHashAsString(), listeningAddresses.get(0), outputAddresses.get(0), quote.getAmount());
            
		//post request
		RestTemplate template = restService.getRestOperationsTemplate(ethUrl);

		//add required agent header
		//HttpHeaders headers = new HttpHeaders();
		//headers.set("User-Agent", "none");
		//headers.set("Accept", MediaType.ALL.toString());
		HttpEntity<PurchaseOrder> request = new HttpEntity<PurchaseOrder>(order);
		//System.err.println(request.getBody());
		
		//submit request - check response code only		
        //stats:  pending++
        statistics.incrementPendingExchange();
        long duration = System.currentTimeMillis();
		ResponseEntity<String> response = template.postForEntity(ethUrl, request, String.class);
		duration = System.currentTimeMillis() - duration;
		statistics.addExchangeDuration(duration);
		if (!response.getStatusCode().equals(HttpStatus.CREATED) ) {
			//parse response body:      { "error" : "<some message>" }
			String errMsg = "<no message supplied>";
			try {
				errMsg = JsonPath.using(Configuration.defaultConfiguration()).parse(response.getBody()).read("$.error", String.class);
			} catch (Throwable e) {
				//ignore
			}

			//bad response
			LOGGER.error("Error response from Ethereum buyer, url: " + ethUrl +", returned code: " + response.getStatusCode() + ", content: " + response);
			
			//set memo to show failed
			if (StringUtils.isEmpty(tx.getMemo())) {
				tx.setMemo(MessageFormat.format(PROCESSED_MEMO_FAILURE_FORMAT, errMsg));	
			} else if (!retry) {
				//only mark failure on first pass
				tx.setMemo(MessageFormat.format(PROCESSED_MEMO_FAILURE_FORMAT, errMsg) + ", original memo: " + tx.getMemo());						
			}
			
			//update stats
            statistics.failedProcessingExchange();
		} else {
			//response body
			//		{"fulfilment" : {
	        //			"bitcoin_tx_hash": "<repeat the one provided>",
	        //			"tok_purchase_tx_hash" : "<ethereum transaction hash>",
	        //			"tok_transfer_tx_hash" : "<ethereum transaction hash>" }
	    	//		}
			
			LOGGER.audit("Successfully purchased Ethereum for purchase order: " + order.toString() + ", response: " + response.getBody());
            statistics.successfulProcessingExchange();
            
            //update eth amount statistics
            statistics.addEthereumReceipt(quote.getAmount());
            //update brokerage fees stats
            statistics.addBrokerFee(brokerageFee);
            
            //mark transaction as processed -- need for retry checking
			if (StringUtils.isEmpty(tx.getMemo())) {
				tx.setMemo(MessageFormat.format(PROCESSED_MEMO_SUCCESS_FORMAT, quote.getAmount()));			
			} else {
				//if retry, retain old failure, but prepend with success
				tx.setMemo(MessageFormat.format(PROCESSED_MEMO_SUCCESS_FORMAT, quote.getAmount()) + ", original memo: " + tx.getMemo());						
			}
		}
		statistics.decrementPendingExchange();
            
		//update stats
        statistics.decrementProcessingExchange();
            
	}
}
