package com.bitsanity.bitchange.server.spring_boot.dao;

import java.io.Serializable;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "USER_AUTHORIZATION")
public class UserAuthorization implements Serializable {

	public static final String KEY_TYPE_PRIVATE = "V";
	public static final String KEY_TYPE_PUBLIC = "B";
	public static final String KEY_ALGORITHM_RSA = "RSA";
	public static final String KEY_ALGORITHM_ELLIPTIC = "EC";

	private static final long serialVersionUID = 1L;

	@Id
	//@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(insertable=false)
	private long id;

	@Column(name = "CLIENT_ID", nullable = false)
	private String systemId;

	@Column(nullable = false)
	private String keyType;

	@Column(nullable = false)
	private String description;

	@Column(name = "CREATED_ON", insertable=false)
	//@GeneratedValue()
	private Timestamp lastModified;

	@Column(name = "expiration", nullable = false)
	private Timestamp keyExpiration;

	@Column(name = "ASYM_KEY", nullable = false)
	private byte[] key;
	
	@Column()
	private String keyAlgorithm = "RSA";
	
	@Transient
	private PublicKey publicKey;
	
	//TODO - for any roles needed outside a transaction
	//@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)

	protected UserAuthorization() {

	}

	public UserAuthorization(String systemId, String keyType, String description, Timestamp keyExpiration, byte[] key) {
		super();
		id = -1;
		this.systemId = systemId;
		this.keyType = keyType;
		this.description = description;
		this.keyExpiration = keyExpiration;
		this.key = key;
	}

	public long getId() {
		return id;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getKeyType() {
		return keyType;
	}

	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getLastModified() {
		return lastModified;
	}

	public void setLastModified(Timestamp lastModified) {
		this.lastModified = lastModified;
	}

	public Timestamp getKeyExpiration() {
		return keyExpiration;
	}

	public void setKeyExpiration(Timestamp keyExpiration) {
		this.keyExpiration = keyExpiration;
	}

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public String getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public void setKey_Algorithm(String key_Algorithm) {
		this.keyAlgorithm = key_Algorithm;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	@Override
	public String toString() {
		return systemId + " (" + id + ") : " + description + " : Algorithm: [" + keyAlgorithm + "] : Modified:" 
				+ LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified.getTime()), ZoneId.of("Z")) + " : Key Expires: " 
				+ LocalDateTime.ofInstant(Instant.ofEpochMilli(keyExpiration.getTime()), ZoneId.of("Z"));
	}
}
