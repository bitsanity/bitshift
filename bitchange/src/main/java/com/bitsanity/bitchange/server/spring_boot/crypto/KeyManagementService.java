package com.bitsanity.bitchange.server.spring_boot.crypto;

import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
//import javax.transaction.Transactional;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bitsanity.bitchange.server.spring_boot.dao.KeyRepository;
import com.bitsanity.bitchange.server.spring_boot.dao.UserAuthorization;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

@Service()
@Scope(value = "singleton")
public class KeyManagementService {
	
	private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern(CryptographyService.EXPIRATION_DATE_FORMAT).withZone(ZoneId.of("Z"));

	@Autowired
	private KeyRepository keyRepository;
	@Autowired
	private CryptographyService cryptographyService;
	
	@Value("${keystore.disableKeyMgmt:}")
	private Optional<Boolean> disableKeyMgmt;
	@Value("${keystore.soakInMinutes:5}")
	private int keySoakPeriod;
	@Value("${keystore.serverId}")
	private String serverKeyId;
	@Value("${keystore.clientKeyValidForDays}")
	private int clientKeyDuration;
	
	private Map<String, UserAuthorization> clientMap = Collections.emptyMap();
	
	private Instant lastKeyRefresh = Instant.MIN;
	
	private final Logger LOG = CustomLoggerFactory.getLogger(KeyManagementService.class);
	
	@PostConstruct
	private void init() {
		//disable key retrieval if setting set
		if (!disableKeyMgmt.isPresent() || !disableKeyMgmt.get()) {
			LOG.audit("Key retrieval invoked.");

			
			//get keys from database
			//List<UserAuthorization> authorizations = keyRepository.findAll();
			List<UserAuthorization> authorizations = keyRepository.findByKeyExpirationIsNotNullAndKeyExpirationAfter(
					new Timestamp(ZonedDateTime.now(ZoneId.of("Z")).toInstant().toEpochMilli()));
			LOG.debug("Retrieved " + authorizations.size() + " authorization keys from repository.");
			
			KeyFactory rsaKeyFac;
			KeyFactory ellipticKeyFac;
			try {
				rsaKeyFac = KeyFactory.getInstance("RSA");
				ellipticKeyFac = KeyFactory.getInstance("EC");
			} catch (NoSuchAlgorithmException e) {
				//FATAL exception
				String msg = "FATAL:  Unable to intialize key factory.";
				LOG.error(msg, e);
				throw new BeanCreationException(msg, e);
			}

			short ownKeyCount = 0;
			int errorCount = 0;
			HashMap<String, UserAuthorization> authMap = new HashMap<String, UserAuthorization>();
			PublicKey publicKey;
			for (UserAuthorization auth : authorizations) {
				LOG.debug("Processing authorization for: " + auth);
				
				if (auth.getSystemId().equals(serverKeyId)) {
					ownKeyCount++;

					if (auth.getKeyType().equals(UserAuthorization.KEY_TYPE_PUBLIC)) {
						// handle own public key
						try {
							X509EncodedKeySpec keySpec = new X509EncodedKeySpec(auth.getKey());

							//support EC
							if (auth.getKeyAlgorithm().equals(UserAuthorization.KEY_ALGORITHM_RSA)) {
								publicKey = rsaKeyFac .generatePublic(keySpec);
							} else if (auth.getKeyAlgorithm().equals(UserAuthorization.KEY_ALGORITHM_ELLIPTIC)) {
								publicKey = ellipticKeyFac.generatePublic(keySpec);
							} else {
								throw new InvalidKeySpecException("Unknown key type specified: " + auth.getKeyAlgorithm());
							}
							
							cryptographyService.setPublicKey(publicKey);
							
							//TODO add to client key map?
							
						} catch (InvalidKeySpecException e) {
							//FATAL exception
							String msg = "FATAL:  Unable to process server public key.";
							LOG.error(msg, e);
							throw new BeanCreationException(msg, e);
						}
					} else {
						//handle own private key
						try {
							PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(auth.getKey());
							
							//support EC
							PrivateKey privateKey;
							if (auth.getKeyAlgorithm().equals(UserAuthorization.KEY_ALGORITHM_RSA)) {
								privateKey = rsaKeyFac .generatePrivate(encodedKeySpec);
							} else if (auth.getKeyAlgorithm().equals(UserAuthorization.KEY_ALGORITHM_ELLIPTIC)) {
								privateKey = ellipticKeyFac.generatePrivate(encodedKeySpec);
							} else {
								throw new InvalidKeySpecException("Unknown key type specified: " + auth.getKeyAlgorithm());
							}

							cryptographyService.setPrivateKey(privateKey);
						} catch (InvalidKeySpecException e) {
							//FATAL exception
							String msg = "FATAL:  Unable to process server private key.";
							LOG.error(msg, e);
							throw new BeanCreationException(msg, e);
						}
					}
				} else {
					if (auth.getKeyType().equals(UserAuthorization.KEY_TYPE_PUBLIC)) {
						try {
							X509EncodedKeySpec keySpec = new X509EncodedKeySpec(auth.getKey());

							//support EC
							if (auth.getKeyAlgorithm().equals(UserAuthorization.KEY_ALGORITHM_RSA)) {
								publicKey = rsaKeyFac .generatePublic(keySpec);
							} else if (auth.getKeyAlgorithm().equals(UserAuthorization.KEY_ALGORITHM_ELLIPTIC)) {
								publicKey = ellipticKeyFac.generatePublic(keySpec);
							} else {
								throw new InvalidKeySpecException("Unknown key type specified: " + auth.getKeyAlgorithm());
							}
							auth.setPublicKey(publicKey);
							
							//load keys into map
							authMap.put(auth.getSystemId(), auth);
						} catch (InvalidKeySpecException e) {
							//catch exception, but continue processing keys
							String msg = "FATAL:  Unable to process system " + auth.getSystemId() + " public key.";
							LOG.error(msg, e);
							errorCount++;
						}
					} else {
						//ignore other's private keys - log warn
						LOG.warn(String.format("Invalid key type found (%s) for system id (%s) : %s", auth.getKeyType(), auth.getSystemId(), auth.getDescription()));
						errorCount++;
					}
				}
			}

			if (ownKeyCount != 2 ) {
				//FATAL -- handle missing/expired own key
				String msg = "FATAL:  Server key pair not found.  Keys found: " + ownKeyCount;
				LOG.error(msg);
				throw new BeanCreationException(msg);
			}
			
			LOG.audit("Key update complete.  Acknowledging " + authMap.size() + " clients; errors encountered: " + errorCount);
			
			//TODO multi-threaded concerns?
			clientMap = authMap;

			//update refresh stamp
			lastKeyRefresh = Instant.now();
		} else {
			LOG.audit("Key management disabled per property setting (property exists: " + disableKeyMgmt.isPresent() + ").");
		}
	}

	public boolean refreshKeys() {
		LOG.info("Key refresh invoked.");
		
		//return immediately if within soaking period (DoS prevention)
		Instant current = Instant.now();
		long gap = ChronoUnit.MILLIS.between(lastKeyRefresh, current);
		if (gap <= (keySoakPeriod * 60 * 1000)) {
			LOG.info("Key refresh rejected due to request within soak period.  Next refresh available at: " + DT_FORMATTER.format(lastKeyRefresh.plus(keySoakPeriod, 
					ChronoUnit.MINUTES)) + " (Current: " + DT_FORMATTER.format(current) + ")" );
			return false;
		}
		
		//update keys
		init();
		
		//return successfully
		return true;
	}
	
	public boolean refreshKeysPriviledged(StackTraceElement[] stackTraceElements) {
		if ((stackTraceElements != null) && (stackTraceElements.length > 0)) {
			if (stackTraceElements[0].getClassName().equals("com.bitsanity.bitchange.server.spring_boot.jmx.TestServicesServerStatistics") ||
					stackTraceElements[0].getClassName().equals("com.bitsanity.bitchange.server.spring_boot.crypto.KeyManagementServiceTest") ||
					stackTraceElements[0].getClassName().equals("com.bitsanity.bitchange.server.spring_boot.web.DataServicesAdminControllerTest") 
					) {
				//force the refresh to happen
				LOG.info("Bypassing soak period, call made from authorized class: " + stackTraceElements[0].getClassName());
				init();
				return true;
			}
		}
		
		//let default processing determine if call should go through
		return refreshKeys();
	}

	@Transactional
	public void updateKey(String clientId, String encodedPublicKey, String keyAlgorithmType) throws KeyManagementException {
		LOG.info("Client public key updated called for client: " + clientId);
		
		//if client does not already exist, try refresh, then throw exception
		if (!clientMap.containsKey(clientId)) {
			refreshKeys();

			if (!clientMap.containsKey(clientId)) {
				throw new KeyManagementException("Certificate update failed.  Prior certificate not found for server:" + clientId);
			}
		}
		
		//check key type
		KeyFactory keyFactory;
		if (keyAlgorithmType.equals(UserAuthorization.KEY_ALGORITHM_ELLIPTIC)) {
			try {
				keyFactory = KeyFactory.getInstance("EC");
			} catch (NoSuchAlgorithmException e) {
				String msg = "Unable to intialize EC key factory.";
				LOG.error(msg, e);
				throw new KeyManagementException(msg, e);
			}
		} else if (keyAlgorithmType.equals(UserAuthorization.KEY_ALGORITHM_RSA)) {
			try {
				keyFactory = KeyFactory.getInstance("RSA");
			} catch (NoSuchAlgorithmException e) {
				String msg = "Unable to intialize RSA key factory.";
				LOG.error(msg, e);
				throw new KeyManagementException(msg, e);
			}
		} else {
			throw new KeyManagementException("Invalid key type specified: " + keyAlgorithmType);
		}

		//build the public key
		PublicKey pubKey;
		try {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(encodedPublicKey));
			pubKey = keyFactory .generatePublic(keySpec);
		} catch (InvalidKeySpecException e) {
			String msg = "Invalid public key.";
			LOG.error(msg, e);
			throw new KeyManagementException(msg, e);
		}
		
		//get current POJO
		UserAuthorization pojo;
		try {
			pojo = keyRepository.findBySystemIdAndKeyType(clientId, UserAuthorization.KEY_TYPE_PUBLIC); 
		} catch (Exception e) {
			//not sure what could happen here, but want to catch it.
			String msg = "Uable to retrieve public key for known client:" + clientId;
			LOG.error(msg, e);
			throw new KeyManagementException(msg, e);
		}
		
		//update client key
		pojo.setKey(pubKey.getEncoded());
		Instant keyExpiration = Instant.now().plus(clientKeyDuration, ChronoUnit.DAYS);
		pojo.setKeyExpiration(new Timestamp(keyExpiration.toEpochMilli()));
		//pojo.setLastModified(new Timestamp(Instant.now().toEpochMilli()));
		pojo.setKey_Algorithm(keyAlgorithmType);
		
		try {
			keyRepository.save(pojo);
		} catch (Exception e) {
			//not sure what could happen here, but want to catch it.
			String msg = "Uable to save public key for known client:" + clientId;
			LOG.error(msg, e);
			throw new KeyManagementException(msg, e);
		}
		
		//update current map
		pojo.setPublicKey(pubKey);
		clientMap.put(pojo.getSystemId(), pojo);
		
		LOG.audit("Updated client public key: " + pojo.toString());
	}

	public String getLastRefresh(){
		//return formatted time
		if (lastKeyRefresh.equals(Instant.MIN)) {
			return "never";
		}
		return DT_FORMATTER.format(lastKeyRefresh);
	}
	
	public int getKnownClientCount() {
		//TODO remove own public from count?
		return clientMap.size();
	}
	
	public int getRefreshInterval() {
		return keySoakPeriod;
	}
	
	public boolean setRefreshIntervalInMinutes(int interval) {
		if (interval < 1) {
			return false;
		}
		
		keySoakPeriod = interval;
		return true;
	}

	public UserAuthorization getUserAuthorization(String clientId) {
		LOG.info("Client public key updated called for client: " + clientId);
		
		//if client does not already exist, try refresh, then throw exception
		if (!clientMap.containsKey(clientId)) {
			refreshKeys();
		}
		
		return clientMap.get(clientId);
	}
}
