/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.bitcoin;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.crypto.KeyCrypterException;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.bitsanity.bitchange.canonical.response.Respondable;
import com.bitsanity.bitchange.server.spring_boot.AbstractDatabaseIntegrationTest;
import com.bitsanity.bitchange.server.spring_boot.BitsanityServer;
import com.bitsanity.bitchange.server.spring_boot.jmx.ExchangerServerStatistics;
import com.bitsanity.bitchange.server.spring_boot.jmx.RollingStatistics;
import com.bitsanity.bitchange.server.spring_boot.web.BitchangeController;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * @author lou.paloma
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes=BitsanityServer.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@TestPropertySource(
    properties = {
    	"keystore.disableKeyMgmt:true"
    	,"spring.datasource.url:"
    }
)
public class BitcoinExchangeServiceExternalAddressIntegrationTest extends AbstractDatabaseIntegrationTest {

	@Value("${bitcoin.network}")
	private String network;
	
	@Value("${local.server.port}")
	private int port;

	@Autowired
	private ExchangerServerStatistics statistics;

	
	private static final Logger LOGGER = CustomLoggerFactory.getLogger(BitcoinExchangeServiceExternalAddressIntegrationTest.class);

	/**
	 * Test method for {@link com.bitsanity.bitchange.server.spring_boot.bitcoin.BitcoinExchangeService#init()}.
	 */
	@Test
	public void testInit() throws Exception {
		//load source test wallet
		
		NetworkParameters params;
		if (network.equals("testnet")) {
		    params = TestNet3Params.get();
		} else if (network.equals("regtest")) {
		    params = RegTestParams.get();
		} else {
			LOGGER.error("Invalid Bitcoin network specified: " + network);
			
			//throw FATAL exception
			throw new RuntimeException();
		}
		
		// Start up a basic app using a class that automates some boilerplate. Ensure we always have at least one key.
		//WalletAppKit kit = new WalletAppKit(params, new File("./src/test/resources"), this.getClass().getSimpleName()) {
		WalletAppKit sourceKit = new WalletAppKit(params, new File("./src/test/resources"), BitcoinExchangeServiceIntegrationTest.SOURCE_WALLET_NAME) {
		    @Override
		    protected void onSetupCompleted() {
		        // This is called in a background thread after startAndWait is called, as setting up various objects
		        // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
		        // on the main thread.
		        if (wallet().getKeyChainGroupSize() < 1) {
		        	//create key
		            wallet().importKey(new ECKey());
		        }
		    }
		};

		if (params == RegTestParams.get()) {
		    // Regression test mode is designed for testing and development only, so there's no public network for it.
		    // If you pick this mode, you're expected to be running a local "bitcoind -regtest" instance.
		    sourceKit.connectToLocalHost();
		}

		// Download the block chain and wait until it's done.
		sourceKit.startAsync();
		sourceKit.awaitRunning();
        
        if (sourceKit.wallet().getWatchedAddresses().isEmpty()) {
        	sourceKit.wallet().addWatchedAddress(sourceKit.wallet().freshReceiveAddress());
        }
        LOGGER.info("SOURCE Wallet AppKit synchronized and listening on addreses(es): " + sourceKit.wallet().getWatchedAddresses());

        sourceKit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
                Coin value = tx.getValueSentToMe(w);
                LOGGER.audit("Received transaction for " + value.toFriendlyString() + ": " + tx);

                // Wait until it's made it into the block chain (may run immediately if it's already there).
                //
                // For this dummy app of course, we could just forward the unconfirmed transaction. If it were
                // to be double spent, no harm done. Wallet.allowSpendingUnconfirmedTransactions() would have to
                // be called in onSetupCompleted() above. But we don't do that here to demonstrate the more common
                // case of waiting for a block.
                Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<TransactionConfidence>() {
                    @Override
                    public void onSuccess(TransactionConfidence result) {
                    	LOGGER.audit("Confidence met for transaction " + tx.getHashAsString());
                    }

                    @Override
                    public void onFailure(Throwable t) {
                    	
                    }
                });
            }
        });
        if (!sourceKit.wallet().getBalance().isPositive()) {
        	String msg = "Source wallet has no funds.  Send funds to address: " + sourceKit.wallet().getWatchedAddresses();
        	LOGGER.error(msg);
        	fail(msg);
        } else {
            LOGGER.info("Source wallet has balance of: " + sourceKit.wallet().getBalance().toFriendlyString());
        }

        //set up secondary kit for external address
		WalletAppKit kit = new WalletAppKit(params, new File("./src/test/resources"), this.getClass().getSimpleName()) {
		    @Override
		    protected void onSetupCompleted() {
		        if (wallet().getKeyChainGroupSize() < 1) {
		            wallet().importKey(new ECKey());
		        }
		    }
		};
		if (params == RegTestParams.get()) {
		    sourceKit.connectToLocalHost();
		}
		kit.startAsync();
		kit.awaitRunning();
        Address externalAddress;
        if (kit.wallet().getWatchedAddresses().isEmpty()) {
        	externalAddress = kit.wallet().freshReceiveAddress();
        	kit.wallet().addWatchedAddress(externalAddress);
        } else {
        	externalAddress = kit.wallet().getWatchedAddresses().get(0);
        }
        LOGGER.info("EXTERNAL Wallet AppKit synchronized and listening on addreses(es): " + kit.wallet().getWatchedAddresses());
        kit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
                Coin value = tx.getValueSentToMe(w);
                LOGGER.audit("Received transaction for " + value.toFriendlyString() + ": " + tx);
                Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<TransactionConfidence>() {
                    @Override
                    public void onSuccess(TransactionConfidence result) {
                    	LOGGER.audit("Confidence met for transaction " + tx.getHashAsString());

                    	//send money back to source
                        try {
                            Coin value = w.getBalance();
                            System.out.println("Returning " + value.toFriendlyString());
                            SendRequest request = SendRequest.to(sourceKit.wallet().getWatchedAddresses().get(0), value );
                            Wallet.SendResult sendResult = sourceKit.wallet().sendCoins(kit.peerGroup(), request); 
                            checkNotNull(sendResult); 
                            sendResult.broadcastComplete.addListener(new Runnable() {
                                @Override
                                public void run() {
                                    // The wallet has changed now, it'll get auto saved shortly or when the app shuts down.
                                    System.err.println("Returned coins! Transaction hash is " + sendResult.tx.getHashAsString() + "; tracking url: https://live.blockcypher.com/btc-testnet/tx/" + 
                                    		sendResult.tx.getHashAsString());
                                    LOGGER.audit("Returned coins: " + sendResult.tx.toString());
                                }
                            }, MoreExecutors.directExecutor());
                        } catch (Throwable e) {
                            LOGGER.error("Error while returning coin; " + e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                    	
                    }
                });
            }
        });
        
		//test
        String externalAddressStr = externalAddress.toBase58();
		Set<String> watchedAddresses = statistics.getWatchedAddresses();
		assertFalse("invalid watched address list", watchedAddresses.isEmpty());
		assertFalse("address already being watched", watchedAddresses.contains(externalAddressStr));
		
		//add watched address -- validate
		ResponseEntity<Respondable> entity = new TestRestTemplate().postForEntity("http://localhost:" + this.port + BitchangeController.URL_WALLET_ADDRESS_MGMT,
			null, Respondable.class, externalAddressStr);

		assertEquals(HttpStatus.CREATED, entity.getStatusCode());
	
		System.err.println("POST body: " + entity.getBody().toString());
		assertEquals("Invalid POST response body", 
			"{\"@class\":\"com.bitsanity.bitchange.canonical.RestMessage\",\"command\":\"/bitcoin/address/" + externalAddressStr + "\",\"result\":0,\"message\":\"Ok\"}",
			entity.getBody().toString());
		watchedAddresses = statistics.getWatchedAddresses();
		assertFalse("invalid watched address list", watchedAddresses.isEmpty());
		assertTrue("address not being watched", watchedAddresses.contains(externalAddressStr));
        
		//send coin from source to watched exchange address
        try {
        	//TODO get brokerage amount from service
            Coin value = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.multiply(2);
            System.out.println("Sending " + value.toFriendlyString() + " (includes brokerage fee).");
            
            // Now send the coins back! Send with a small fee attached to ensure rapid confirmation.
            final Coin amountToSend = value.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
            
            SendRequest request = SendRequest.to(externalAddress, amountToSend );
            //request.feePerKb = Transaction.DEFAULT_TX_FEE.times(2);
            Wallet.SendResult sendResult = sourceKit.wallet().sendCoins(sourceKit.peerGroup(), request); 

            checkNotNull(sendResult);  // We should never try to send more coins than we have!
            System.err.println("Sending ...");
            
            // Register a callback that is invoked when the transaction has propagated across the network.
            // This shows a second style of registering ListenableFuture callbacks, it works when you don't
            // need access to the object the future returns.
            sendResult.broadcastComplete.addListener(new Runnable() {
                @Override
                public void run() {
                    // The wallet has changed now, it'll get auto saved shortly or when the app shuts down.
                    System.err.println("Sent coins! Transaction hash is " + sendResult.tx.getHashAsString() + "; tracking url: https://live.blockcypher.com/btc-testnet/tx/" + 
                    		sendResult.tx.getHashAsString());
                    LOGGER.audit("Sent coins: " + sendResult.tx.toString());
                }
            }, MoreExecutors.directExecutor());
            
            //TODO sleep until received
            Thread.sleep(120_000);
        } catch (KeyCrypterException | InsufficientMoneyException e) {
            // We don't use encrypted wallets in this example - can never happen.
        	String msg = "Error while sending coin; " + e.getMessage(); 
            LOGGER.error(msg, e);
            fail(msg);
        } finally {
        	//remove watched address
    		entity = new TestRestTemplate().exchange("http://localhost:" + this.port + BitchangeController.URL_WALLET_ADDRESS_MGMT, HttpMethod.DELETE, null, 
    			Respondable.class, externalAddressStr);

    		assertEquals(HttpStatus.OK, entity.getStatusCode());

    		System.err.println("DELETE body: " + entity.getBody().toString());
    		assertEquals("Invalid DELETE response body", 
    			"{\"@class\":\"com.bitsanity.bitchange.canonical.RestMessage\",\"command\":\"/bitcoin/address/" + externalAddressStr + "\",\"result\":0,\"message\":\"Ok\"}",
    			entity.getBody().toString());
    		watchedAddresses = statistics.getWatchedAddresses();
    		assertFalse("invalid watched address list", watchedAddresses.isEmpty());
    		assertFalse("address still being watched", watchedAddresses.contains(externalAddressStr));
        }
        
        //validate
        assertEquals("invalid received coin count", 1, statistics.getCoinNotificationCount());
        assertEquals("invalid success count", 1, statistics.getSuccessfulExchangeCount());
        assertEquals("invalid currently processing count", 0, statistics.getProcessingReceiptCount());
        assertEquals("invalid currently pending exchange count", 0, statistics.getPendingExchangeCount());
        assertEquals("invalid currently pending exchange count", 0, statistics.getPendingReceiptCount());

        assertTrue("invalid statistics count", statistics.getRollingStatistics().length > 0);
        assertThat("invalid statistics type", statistics.getRollingStatistics()[0], instanceOf(RollingStatistics.class));
        assertTrue("invalid duration count", ((RollingStatistics)statistics.getRollingStatistics()[0]).getExchangeDurationStatisticsCount() == 1);
        assertTrue("invalid duration", ((RollingStatistics)statistics.getRollingStatistics()[0]).getExchangeDurationStatisticsAverage() > 0);
        
        //shutdown
		LOGGER.info("Shuttting down SOURCE Bitcoin wallet...");
		sourceKit.stopAsync();
		sourceKit.awaitTerminated(30, TimeUnit.SECONDS);
		LOGGER.info("Shuttting down EXTERNAL Bitcoin wallet...");
		kit.stopAsync();
		kit.awaitTerminated(30, TimeUnit.SECONDS);

	}

}
