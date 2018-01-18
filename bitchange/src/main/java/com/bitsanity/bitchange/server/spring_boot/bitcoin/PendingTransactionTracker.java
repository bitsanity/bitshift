/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.bitcoin;

import java.util.ArrayList;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bitsanity.bitchange.server.spring_boot.jmx.ExchangerServerStatistics;

/**
 * @author lou.paloma
 *
 */
@Component
public class PendingTransactionTracker {
	
	@Autowired
	private ExchangerServerStatistics statistics;

	private ConcurrentSkipListMap<String, Long> pendingTransactions = new ConcurrentSkipListMap<>();
	
	@PostConstruct
	public void init() {
		statistics.setPendingTransactionTracker(this);
	}
	
	public int getBacklogSize() {
		return pendingTransactions.size();
	}
	
	public long getMaxWaitTime() {
		OptionalLong earliest = pendingTransactions.values().stream().mapToLong(Long::longValue).min();
		if (earliest.isPresent()) {
			return System.currentTimeMillis() - earliest.getAsLong();
		}
		return 0;
	}
	
	public double getAverageWaitTime() {
		ArrayList<Long> staticList = new ArrayList<>(pendingTransactions.values());
		if (staticList.isEmpty()) {
			return 0;
		}
		long total = staticList.stream().mapToLong(Long::longValue).sum();
		return (total * 1.0d) / staticList.size();
	}

	/*pkg*/ boolean add(String transactionHash) {
		if (pendingTransactions.containsKey(transactionHash)) {
			return false;
		}

		pendingTransactions.putIfAbsent(transactionHash, System.currentTimeMillis());
		
		return true;
	}

	/*pkg*/ long remove(String transactionHash) {
		if (!pendingTransactions.containsKey(transactionHash)) {
			//if not currently pending, don't throw exception
			return -1;
		}

		long duration = System.currentTimeMillis();
		duration -= pendingTransactions.remove(transactionHash);
		
		//update statistics
		statistics.addPendingTransactionDuration(duration);
				
		return duration;
	}

	/*pkg*/ boolean contains(String transactionHash) {
		return pendingTransactions.containsKey(transactionHash);
	}
	
	/*pkg*/ long getAge(String transactionHash) {
		if (!pendingTransactions.containsKey(transactionHash)) {
			//if not currently pending, don't throw exception
			return -1;
		}

		long duration = System.currentTimeMillis();
		duration -= pendingTransactions.get(transactionHash);
				
		return duration;
	}
}
