package com.bitsanity.bitchange.server.spring_boot.dao;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface KeyRepository extends JpaRepository<UserAuthorization, Long> {
	
	/*
	 * 
	 	select UA.*
		from DATA_SERVICES.USER_AUTHORIZATION UA,
  			(
    			select distinct(USER_ID), key_type, max(LAST_MODIFIED) as LAST_MODIFIED
    			from DATA_SERVICES.USER_AUTHORIZATION
    			group by user_id, key_type
  			) A
		where UA.USER_ID = A.USER_ID
  			and UA.KEY_TYPE = A.KEY_TYPE
  			and UA.LAST_MODIFIED = A.LAST_MODIFIED
  			and expiration > DATE '2015-09-10';
	 */
	
    //@Query("delete from UserAuthorization u where u.userId = ?1")

	/*
    @Modifying
    //@Transactional
    @Query("select UserAuthorization, ( " +
    			"select distinct(USER_ID), key_type, max(LAST_MODIFIED) as LAST_MODIFIED " +
    			"from DATA_SERVICES.USER_AUTHORIZATION " +
    			"group by user_id, key_type ) A " +
		"where UserAuthorization.USER_ID = A.USER_ID " +
  			"and UserAuthorization.KEY_TYPE = A.KEY_TYPE " +
  			"and UserAuthorization.LAST_MODIFIED = A.LAST_MODIFIED " +
  			" and UserAuthorization.EXPIRATION > DATE ?1")
    List<UserAuthorization> findActiveKeys(Timestamp stamp);
    */
    
	//List<UserAuthorization> findDistinctSystemIdByKeyExpirationIsNotNullAndKeyExpirationAfter(Timestamp stamp);

	List<UserAuthorization> findByKeyExpirationIsNotNullAndKeyExpirationAfter(Timestamp stamp);

	UserAuthorization findBySystemIdAndKeyType(String clientId, String keyTypePublic);
	
	/******
	 * Unit test usage only
	 */

	List<UserAuthorization> findBySystemId(String systemId);

}
