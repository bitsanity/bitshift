package com.bitsanity.bitchange.server.spring_boot.crypto;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.bitsanity.bitchange.server.spring_boot.dao.UserAuthorization;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

@Service(value = "CryptographyService")
// this is the default value. Explicitly setting it just for readability.
@Scope(value = "singleton")
public class CryptographyService {

	public static final String EXPIRATION_DATE_FORMAT = "yyyy-MMM-dd HH:mm:ss:SSS z";

	@Value("${keystore.file:#{null}}")
	private String keystoreFile;
	@Value("${keystore.password}")
	private String keystorePassword;
	@Value("${keystore.type}")
	private String keystoreType;
	@Value("${keystore.alias}")
	private String privateKeyId;
	@Value("${cipher.algorithm}")
	private String cipherAlgorithm;
	@Value("${cipher.signature.algorithm}")
	private String signatureAlgorithm;
	@Value("${cipher.signature.EC.algorithm}")
	private String signatureECAlgorithm;
	@Value("${cipher.digest.algorithm}")
	private String digestAlgorithm;

	@Value("${keystore.AES.alias}")
	private String aesAlias;
	@Value("${cipher.aes.algorithm}")
	private String aesCipherAlgorithm;
	@Value("${keystore.AES.keyHex}")
	private String aesKeyHex;

	private KeyStore keystore = null;
	private PrivateKey privateKey = null;
	private PublicKey publicKey = null;
	private SecretKey aesKey;

	// http://primes.utm.edu/lists/small/1000.txt
	public static final int[] primes = { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73,
			79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191,
			193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307, 311,
			313, 317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439,
			443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541 };

	private static final Logger logger = CustomLoggerFactory.getLogger(CryptographyService.class);

	@PostConstruct
	private void init() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
			UnrecoverableKeyException {
		logger.debug("Initializing cryptography service with keystore: " + keystoreFile);

		// Get RSA private key
		if (getKeystore() != null) {
			if (!getKeystore().containsAlias(privateKeyId)) {
				throw new KeyStoreException("Private key not found for : " + privateKeyId);
			}
			privateKey = (PrivateKey) getKeystore().getKey(privateKeyId, keystorePassword.toCharArray());
			
			// Get AES secret key
			if (getKeystore().containsAlias(aesAlias)) {
				// load from keystore
				aesKey = (SecretKey) keystore.getKey(aesAlias, keystorePassword.toCharArray());
			}
		} 
		
		//Get AES key if not set and specified in properties
		if ( (aesKeyHex != null) && (!aesKeyHex.isEmpty()) ) {
			// load from application properties
			aesKey = new SecretKeySpec(DatatypeConverter.parseHexBinary(aesKeyHex), "AES");
			// System.err.println("AES key: " +
			// DatatypeConverter.printHexBinary(aesKey.getEncoded()));
			// throw new KeyStoreException("Alias for key '" + aesAlias + "' not
			// found in keystore.");
		}
	}

	public CryptoResponse encrypt(String targetAlias, String clearTextMessage, String expiry)
			throws CryptographyException {
		PublicKey key;
		try {
			if (!getKeystore().containsAlias(targetAlias)) {
				throw new CryptographyException("Alias for key '" + targetAlias + "' not found in keystore.");
			}
			key = keystore.getCertificate(targetAlias).getPublicKey();
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new CryptographyException(
					"Unable to retrieve public key for '" + targetAlias + "'; " + e.getMessage(), e);
		}

		byte[] encryptedMessageInBytes;
		long timeCipherGet;
		long timeCipherInit;
		long timeEncrypt;
		try {
			timeCipherGet = System.currentTimeMillis();
			Cipher cipher = Cipher.getInstance(cipherAlgorithm);
			timeCipherGet = System.currentTimeMillis() - timeCipherGet;

			// Initialization
			timeCipherInit = System.currentTimeMillis();
			cipher.init(Cipher.ENCRYPT_MODE, key);
			timeCipherInit = System.currentTimeMillis() - timeCipherInit;

			// encrypt
			timeEncrypt = System.currentTimeMillis();
			encryptedMessageInBytes = cipher.doFinal(clearTextMessage.getBytes("UTF-8"));
			timeEncrypt = System.currentTimeMillis() - timeEncrypt;
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException
				| IllegalBlockSizeException | BadPaddingException e) {
			throw new CryptographyException("Unable to encrypt message; " + e.getMessage(), e);
		}

		String base64EncodedEncryptedMsg = DatatypeConverter.printBase64Binary(encryptedMessageInBytes);

		// build CryptoResponse
		CryptoResponse response = new CryptoResponse(base64EncodedEncryptedMsg, expiry);

		// add signature
		long timeSign = System.currentTimeMillis();
		try {
			response.setSignature(sign(base64EncodedEncryptedMsg));
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			logger.warn("Unable to sign payload, ignoring.  No signature will be attached.", e);
		} finally {
			timeSign = System.currentTimeMillis() - timeSign;
		}

		logger.info(
				MessageFormat.format("***TIMING: Encryption (GetCipher:InitCipher:Encrypt:Sign)  {0} : {1} : {2} : {3}",
						timeCipherGet, timeCipherInit, timeEncrypt, timeSign));
		return response;
	}

	// FIXME -- UNUSED, UNTESTED
	// PGP style multiple recipient, session key cryptography
	public CryptoResponse encrypt(List<String> targets, String clearTextMessage) throws CryptographyException {
		// Generate session key
		SimpleDateFormat dateFormatUTC = new SimpleDateFormat(EXPIRATION_DATE_FORMAT);
		String expiry = dateFormatUTC.format(System.currentTimeMillis());
		SecureRandom dataKey;
		KeyPairGenerator generator;
		try {
			dataKey = new SecureRandom(getInitalizationVector(expiry).getIV());
			generator = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new CryptographyException(
					"Unable to intialize session key for multi-recipient encryption; " + e.getMessage(), e);
		}
		generator.initialize(256, dataKey);
		KeyPair pair = generator.generateKeyPair();

		// Encrypt data with session key
		Cipher cipher;
		byte[] encryptedMessageInBytes;
		String base64EncodedEncryptedMsg;
		CryptoResponse response;
		try {
			cipher = Cipher.getInstance(cipherAlgorithm);
			cipher.init(Cipher.ENCRYPT_MODE, pair.getPrivate());
			encryptedMessageInBytes = cipher.doFinal(clearTextMessage.getBytes("UTF-8"));
			base64EncodedEncryptedMsg = DatatypeConverter.printBase64Binary(encryptedMessageInBytes);

			// create CryptoResponse containing data, initializing map of
			// recipient alias:key, signature
			response = new CryptoResponse(base64EncodedEncryptedMsg);

			// sign with private key
			response.setSignature(sign(base64EncodedEncryptedMsg));
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | UnsupportedEncodingException | SignatureException e) {
			throw new CryptographyException(
					"Unable to encrypt/sign data for multi-recipient encryption; " + e.getMessage(), e);
		}

		// Encrypt key with recipient public key (for each recipient)
		for (String targetAlias : targets) {
			try {
				if (!getKeystore().containsAlias(targetAlias)) {
					logger.warn("Alias for recipient key not found: " + targetAlias);
				} else {
					PublicKey key = keystore.getCertificate(targetAlias).getPublicKey();

					// Initialization
					cipher.init(Cipher.ENCRYPT_MODE, key);

					// change to encrypt key
					encryptedMessageInBytes = cipher.doFinal(pair.getPublic().getEncoded());
					base64EncodedEncryptedMsg = DatatypeConverter.printBase64Binary(encryptedMessageInBytes);

					// add to response
					response.addRecipientKey(targetAlias, base64EncodedEncryptedMsg);
				}
			} catch (InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateException
					| IllegalBlockSizeException | BadPaddingException | IllegalStateException | IOException e) {
				throw new CryptographyException("Unable to encrypt session key for multi-recipient encryption, alias: "
						+ targetAlias + "; " + e.getMessage(), e);
			}
		}

		if (response.getRecipientKeys().isEmpty()) {
			throw new CryptographyException(
					"No recipients found for multi-recipient encryption; provided aliases are: " + targets);
		}

		return response;
	}

	public CryptoResponse encryptAES(String clearTextMessage, String salt) throws CryptographyException {
		byte[] encryptedMessageInBytes;
		long timeCipherGet;
		long timeCipherInit;
		long timeEncrypt;
		try {
			timeCipherGet = System.currentTimeMillis();
			Cipher cipher = Cipher.getInstance(aesCipherAlgorithm);
			timeCipherGet = System.currentTimeMillis() - timeCipherGet;

			// Initialization
			timeCipherInit = System.currentTimeMillis();
			IvParameterSpec ivSpec = getInitalizationVector(salt);
			cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
			timeCipherInit = System.currentTimeMillis() - timeCipherInit;

			// encrypt
			timeEncrypt = System.currentTimeMillis();
			encryptedMessageInBytes = cipher.doFinal(clearTextMessage.getBytes("UTF-8"));
			timeEncrypt = System.currentTimeMillis() - timeEncrypt;
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException
				| IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
			throw new CryptographyException("Unable to encrypt message; " + e.getMessage(), e);
		}

		String base64EncodedEncryptedMsg = DatatypeConverter.printBase64Binary(encryptedMessageInBytes);

		// build CryptoResponse
		CryptoResponse response = new CryptoResponse(base64EncodedEncryptedMsg, salt);

		// add signature
		long timeSign = System.currentTimeMillis();
		try {
			response.setSignature(sign(base64EncodedEncryptedMsg));
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			logger.warn("Unable to sign payload, ignoring.  No signature will be attached.", e);
		} finally {
			timeSign = System.currentTimeMillis() - timeSign;
		}

		logger.info(MessageFormat.format(
				"***TIMING: AES Encryption (GetCipher:InitCipher:Encrypt:Sign)  {0} : {1} : {2} : {3}", timeCipherGet,
				timeCipherInit, timeEncrypt, timeSign));
		return response;
	}

	/**
	 * Used only a temporary standin to get consistent return object, when
	 * backend encryption not supported
	 * 
	 * @param clearTextMessage
	 * @return
	 * @throws CryptographyException
	 */
	public CryptoResponse encodeBase64(String clearTextMessage) throws CryptographyException {
		String base64EncodedEncryptedMsg;
		try {
			base64EncodedEncryptedMsg = DatatypeConverter.printBase64Binary(clearTextMessage.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			String msg = "Unable to convert message to Base64";
			logger.error(msg, e);
			throw new CryptographyException(msg, e);
		}

		// create timestamp
		SimpleDateFormat dateFormatUTC = new SimpleDateFormat(CryptographyService.EXPIRATION_DATE_FORMAT);
		dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
		String stamp = dateFormatUTC.format(System.currentTimeMillis());

		// build CryptoResponse
		CryptoResponse response = new CryptoResponse(base64EncodedEncryptedMsg, stamp);

		return response;
	}

	public String decrypt(String encryptedString) throws CryptographyException {
		byte[] decryptedMessageInBytes;
		try {
			// TODO use pool of initialized decryption engines?
			long timeCipherGet = System.currentTimeMillis();
			Cipher cipher = Cipher.getInstance(cipherAlgorithm);
			timeCipherGet = System.currentTimeMillis() - timeCipherGet;

			// Initialization
			// cipher.init(Cipher.DECRYPT_MODE, privateKey,
			// getInitalizationVector(expiry));
			long timeCipherInit = System.currentTimeMillis();
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			timeCipherInit = System.currentTimeMillis() - timeCipherInit;

			// decrypt
			byte[] base64DecodedEncryptedBytes = DatatypeConverter.parseBase64Binary(encryptedString);
			long timeCipherDecrypt = System.currentTimeMillis();
			decryptedMessageInBytes = cipher.doFinal(base64DecodedEncryptedBytes);
			timeCipherDecrypt = System.currentTimeMillis() - timeCipherDecrypt;

			logger.info(MessageFormat.format("***TIMING: Decryption (GetCipher:InitCipher:Decrypt)  {0} : {1} : {2} ",
					timeCipherGet, timeCipherInit, timeCipherDecrypt));
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			throw new CryptographyException("Unable to decrypt message; " + e.getMessage(), e);
		}

		return new String(decryptedMessageInBytes);
	}

	// FIXME -- UNUSED, UNTESTED
	// PGP style multiple recipient, session key cryptography
	public String decrypt(String encryptedString, String encryptedKey) throws CryptographyException {
		// decrypt key
		Cipher cipher;
		byte[] base64DecodedEncryptedBytes;
		PublicKey publicKey;
		try {
			cipher = Cipher.getInstance(cipherAlgorithm);

			// Initialization
			cipher.init(Cipher.DECRYPT_MODE, privateKey);

			base64DecodedEncryptedBytes = DatatypeConverter.parseBase64Binary(encryptedKey);
			byte[] key = cipher.doFinal(base64DecodedEncryptedBytes);
			publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(key));
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | InvalidKeySpecException e) {
			throw new CryptographyException(
					"Unable to decrypt session key from multi-recipient decryption; " + e.getMessage(), e);
		}

		byte[] decryptedMessageInBytes;
		try {
			// Initialization
			cipher.init(Cipher.DECRYPT_MODE, publicKey);

			// decrypt payload
			base64DecodedEncryptedBytes = DatatypeConverter.parseBase64Binary(encryptedString);
			decryptedMessageInBytes = cipher.doFinal(base64DecodedEncryptedBytes);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			throw new CryptographyException(
					"Unable to decrypt payload using session key from multi-recipient decryption; " + e.getMessage(),
					e);
		}

		return new String(decryptedMessageInBytes);
	}

	public String decryptAES(String encryptedString, String salt) throws CryptographyException {
		byte[] decryptedMessageInBytes;
		try {
			// TODO use pool of initialized decryption engines?
			long timeCipherGet = System.currentTimeMillis();
			Cipher cipher = Cipher.getInstance(aesCipherAlgorithm);
			timeCipherGet = System.currentTimeMillis() - timeCipherGet;

			// Initialization
			long timeCipherInit = System.currentTimeMillis();
			cipher.init(Cipher.DECRYPT_MODE, aesKey, getInitalizationVector(salt));
			timeCipherInit = System.currentTimeMillis() - timeCipherInit;

			// decrypt
			byte[] base64DecodedEncryptedBytes = DatatypeConverter.parseBase64Binary(encryptedString);
			long timeCipherDecrypt = System.currentTimeMillis();
			decryptedMessageInBytes = cipher.doFinal(base64DecodedEncryptedBytes);
			timeCipherDecrypt = System.currentTimeMillis() - timeCipherDecrypt;

			logger.info(MessageFormat.format("***TIMING: Decryption (GetCipher:InitCipher:Decrypt)  {0} : {1} : {2} ",
					timeCipherGet, timeCipherInit, timeCipherDecrypt));
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | InvalidAlgorithmParameterException | UnsupportedEncodingException e) {
			throw new CryptographyException("Unable to decrypt message; " + e.getMessage(), e);
		}

		return new String(decryptedMessageInBytes);
	}

	public String sign(String data, PrivateKey pvtKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		if (pvtKey == null) {
			//no private key (or keystore) defined
			return "";
		}
		
		// TODO use pool of initialized signature engines?

		long timeSignGet = System.currentTimeMillis();
		
		//determine signing algorithm
		Signature signature;
		if (pvtKey.getAlgorithm().equals(UserAuthorization.KEY_ALGORITHM_RSA)) {
			signature = Signature.getInstance(signatureAlgorithm);
		} else if (pvtKey.getAlgorithm().equals(UserAuthorization.KEY_ALGORITHM_ELLIPTIC)) {
			signature = Signature.getInstance(signatureECAlgorithm);
		} else {
			throw new NoSuchAlgorithmException("Algorithm not handled currently: " + pvtKey.getAlgorithm());
		}
		timeSignGet = System.currentTimeMillis() - timeSignGet;

		long timeSignInit = System.currentTimeMillis();
		signature.initSign(pvtKey);
		timeSignInit = System.currentTimeMillis() - timeSignInit;

		long timeSignUpdate = System.currentTimeMillis();
		try {
			signature.update(data.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			String msg = "Unable to convert signature.";
			logger.error(msg, e);
			throw new SignatureException(msg, e);
		}
		timeSignUpdate = System.currentTimeMillis() - timeSignUpdate;

		long timeSignSign = System.currentTimeMillis();
		byte[] signing = signature.sign();
		timeSignSign = System.currentTimeMillis() - timeSignSign;

		logger.info(MessageFormat.format("***TIMING: Signing (GetSign:InitSign:Update:Sign)  {0} : {1} : {2} : {3}",
				timeSignGet, timeSignInit, timeSignUpdate, timeSignSign));
		return DatatypeConverter.printBase64Binary(signing);
	}

	public String sign(String data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		return sign(data, privateKey);
	}

	public boolean verifySignature(String base64EncodedEncryptedMsg, String base64EncodedSignature, PublicKey key) {
		try {
			long timeSignGet = System.currentTimeMillis();

			//determine signing algorithm
			Signature signature;
			if (key.getAlgorithm().equals(UserAuthorization.KEY_ALGORITHM_RSA)) {
				signature = Signature.getInstance(signatureAlgorithm);
			} else if (key.getAlgorithm().equals(UserAuthorization.KEY_ALGORITHM_ELLIPTIC)) {
				signature = Signature.getInstance(signatureECAlgorithm);
			} else {
				throw new NoSuchAlgorithmException("Algorithm not handled currently: " + key.getAlgorithm());
			}

			timeSignGet = System.currentTimeMillis() - timeSignGet;

			long timeSignInit = System.currentTimeMillis();
			signature.initVerify(key);
			timeSignInit = System.currentTimeMillis() - timeSignInit;

			long timeSignUpdate = System.currentTimeMillis();
			signature.update(base64EncodedEncryptedMsg.getBytes("UTF-8"));
			timeSignUpdate = System.currentTimeMillis() - timeSignUpdate;

			byte[] base64DecodedSignature = DatatypeConverter.parseBase64Binary(base64EncodedSignature);
			long timeSignVerify = System.currentTimeMillis();
			boolean verified = signature.verify(base64DecodedSignature);
			timeSignVerify = System.currentTimeMillis() - timeSignVerify;

			logger.info(MessageFormat.format(
					"***TIMING: Verify Signing (GetSign:InitSign:Update:Verify)  {0} : {1} : {2} : {3}", timeSignGet,
					timeSignInit, timeSignUpdate, timeSignVerify));

			return verified;
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | UnsupportedEncodingException e) {
			logger.error("Exception encountered during attempt to verify signature: " + e.getMessage(), e);
			return false;
		}
	}

	public boolean verifySignature(String base64EncodedEncryptedMsg, String base64EncodedSignature, String sourceAlias)
			throws CryptographyException {
		PublicKey key;
		try {
			if (!getKeystore().containsAlias(sourceAlias)) {
				throw new CryptographyException("Alias for key '" + sourceAlias + "' not found in keystore.");
			}
			key = keystore.getCertificate(sourceAlias).getPublicKey();
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new CryptographyException(
					"Unable to retrieve public key for '" + sourceAlias + "'; " + e.getMessage(), e);
		}

		return verifySignature(base64EncodedEncryptedMsg, base64EncodedSignature, key);
	}

	public String publishCertificate() throws CryptographyException {
		byte[] encoded;
		try {
			if (!getKeystore().containsAlias(privateKeyId)) {
				throw new CryptographyException(
						"Alias for server certificate '" + privateKeyId + "' not found in keystore.");
			}
			Certificate cert = keystore.getCertificate(privateKeyId);
			encoded = cert.getEncoded();
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new CryptographyException(
					"Unable to retrieve certificate for server '" + privateKeyId + "'; " + e.getMessage(), e);
		}

		return DatatypeConverter.printBase64Binary(encoded);
	}

	public void addCertificate(String base64EncodedCertificate, String alias) throws CryptographyException {
		if ((alias.equalsIgnoreCase(privateKeyId)) || (alias.equalsIgnoreCase(aesAlias))) {
			throw new CryptographyException("DENIED:  Specified alias would overwrite server certificate: " + alias);
		}

		byte[] bytes = DatatypeConverter.parseBase64Binary(base64EncodedCertificate);

		try {
			// PublicKey publicKey =
			// KeyFactory.getInstance("RSA").generatePublic(new
			// X509EncodedKeySpec(bytes));
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(bytes));

			if (getKeystore().containsAlias(alias)) {
				logger.warn("Adding certificate/public key overrides existing entry for alias: '" + alias
						+ "'; prior value = " + getKeystore().getCertificate(alias).toString() + "; new value = "
						+ cert.toString());
				logger.info("Public key modulus: "
						+ ((RSAPublicKey) cert.getPublicKey()).getModulus().toString(16).toUpperCase());
			}
			getKeystore().setCertificateEntry(alias, cert);

			// need to save updated keystore
			logger.info("Saving new keystore after adding/updating certificate for alias: " + alias);
			getKeystore().store(new FileOutputStream(keystoreFile), keystorePassword.toCharArray());
		} catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
			throw new CryptographyException("Unable to add client certificate for '" + alias + "'; " + e.getMessage(),
					e);
		}
	}

	public String getPublicKeyEncoded() {
		byte[] encoded = publicKey.getEncoded();
		return DatatypeConverter.printBase64Binary(encoded);
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	/* pkg */ void setPublicKey(PublicKey key) {
		publicKey = key;
	}

	/* pkg */ PrivateKey getPrivateKey() {
		// return private key
		return privateKey;
	}

	/* pkg */ void setPrivateKey(PrivateKey key) {
		privateKey = key;
	}

	private KeyStore getKeystore() throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
		if ((keystore == null) && !((keystoreFile == null) || (keystoreFile.isEmpty())) ) {
			logger.debug("Opening keystore file: " + keystoreFile + " [" + keystoreFile.length() + "]");
			InputStream keystoreStream = new FileInputStream(keystoreFile);
			keystore = KeyStore.getInstance(keystoreType);
			keystore.load(keystoreStream, null);
		}

		return keystore;
	}

	/**
	 * Not needed for RSA, but good for AES via:
	 * cipher.init(Cipher.ENCRYPT_MODE, privateKey,
	 * getInitalizationVector(timeToLive));
	 * 
	 * @param key
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	private IvParameterSpec getInitalizationVector(String key) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		// need 512 bits = 64 bytes
		MessageDigest digest = MessageDigest.getInstance(digestAlgorithm);
		byte[] seed = digest.digest(key.getBytes("UTF-8"));
		logger.debug("IV digest size: " + seed.length);

		// get bytes in prime indexes (16th ==> byte 53)
		byte[] iv = new byte[16];
		for (int index = 0; index < 16; index++) {
			iv[index] = seed[index];
		}

		// return new IvParameterSpec(iv, 0, 16);
		return new IvParameterSpec(iv);
	}

	@SuppressWarnings("unused")
	private X509Certificate loadPublicX509FromPEM(String fileName) throws CertificateException, IOException {
		X509Certificate crt = null;
		try (InputStream is = fileName.getClass().getResourceAsStream("/" + fileName)) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			crt = (X509Certificate) cf.generateCertificate(is);
		}
		return crt;
	}

	@SuppressWarnings("unused")
	private PrivateKey loadPrivateKeyFromPEM(String fileName) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		PrivateKey key = null;
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(fileName.getClass().getResourceAsStream("/" + fileName)))) {
			StringBuilder builder = new StringBuilder();
			boolean inKey = false;
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				if (!inKey) {
					if (line.startsWith("-----BEGIN ") && line.endsWith(" PRIVATE KEY-----")) {
						inKey = true;
					}
					continue;
				}
				
				if (line.startsWith("-----END ") && line.endsWith(" PRIVATE KEY-----")) {
					inKey = false;
					break;
				}
				builder.append(line);
			}

			byte[] encoded = DatatypeConverter.parseBase64Binary(builder.toString());
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			key = kf.generatePrivate(keySpec);
		}
		return key;
	}
}
