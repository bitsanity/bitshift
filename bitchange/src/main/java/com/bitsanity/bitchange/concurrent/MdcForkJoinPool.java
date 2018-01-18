package com.bitsanity.bitchange.concurrent;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.MDC;

import com.bitsanity.bitchange.annotations.Experimental;

@Experimental
/**
 * This is an experimental extension of the @see ForkJoinPool to automatically carry over the MDC values.  Current state fails to replicate
 * the MDC across all threads in the pool.
 * 
 * @author billsa
 *
 */
public class MdcForkJoinPool extends ForkJoinPool {

	public MdcForkJoinPool(String poolName) {
		super(ForkJoinPool.getCommonPoolParallelism(), defaultForkJoinWorkerThreadFactory(poolName), null, true);
		//System.err.println("MDC: "+ MDC.getCopyOfContextMap());
	}

	public MdcForkJoinPool(int parallelism, String poolName) {
		super(parallelism, defaultForkJoinWorkerThreadFactory(poolName), null, true);
	}

	public MdcForkJoinPool(int parallelism, boolean asyncMode, String poolName) {
		super(parallelism, defaultForkJoinWorkerThreadFactory(poolName), null, asyncMode);
	}

	@Override
    public void execute(ForkJoinTask<?> task)
    {
		//System.err.println("MDC in execute(ForkJoinTask<?> task): "+ MDC.getCopyOfContextMap());
		
        // See http://stackoverflow.com/a/19329668/14731
        super.execute(wrap(task, MDC.getCopyOfContextMap()));
    }

    @Override
    public void execute(Runnable task)
    {
		//System.err.println("MDC in execute(Runnable task): "+ MDC.getCopyOfContextMap());

		// See http://stackoverflow.com/a/19329668/14731
        super.execute(wrap(task, MDC.getCopyOfContextMap()));
    }

	
	/* (non-Javadoc)
	 * @see java.util.concurrent.ForkJoinPool#submit(java.util.concurrent.ForkJoinTask)
	 */
	@Override
	public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
		//System.err.println("MDC in submit(ForkJoinTask<T> task): "+ MDC.getCopyOfContextMap());

		// TODO Auto-generated method stub
		return super.submit(task);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ForkJoinPool#submit(java.util.concurrent.Callable)
	 */
	@Override
	public <T> ForkJoinTask<T> submit(Callable<T> task) {
		//System.err.println("MDC in submit(Callable<T> task): "+ MDC.getCopyOfContextMap());

		// TODO Auto-generated method stub
		return super.submit(task);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ForkJoinPool#submit(java.lang.Runnable, java.lang.Object)
	 */
	@Override
	public <T> ForkJoinTask<T> submit(Runnable task, T result) {
		//System.err.println("MDC in submit(Runnable task, T result): "+ MDC.getCopyOfContextMap());

		// TODO Auto-generated method stub
		return super.submit(task, result);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ForkJoinPool#submit(java.lang.Runnable)
	 */
	@Override
	public ForkJoinTask<?> submit(Runnable task) {
		//System.err.println("MDC in submit(Runnable task): "+ MDC.getCopyOfContextMap());

		/*
		if (task == null) {
            throw new NullPointerException();
		}
		
        ForkJoinTask<?> job;
        if (task instanceof ForkJoinTask<?>) { // avoid re-wrap
            job = (ForkJoinTask<?>) task;
        } else {
        	job = ForkJoinTask.adapt(task);
        }

        return super.submit(wrap(job, MDC.getCopyOfContextMap()));
        */
		
        return super.submit(wrap(task, MDC.getCopyOfContextMap()));
        
		// TODO Auto-generated method stub
		//return super.submit(task);
	}

    private <T> ForkJoinTask<T> wrap(ForkJoinTask<T> task, Map<String, String> newContext)
    {
        return new ForkJoinTask<T>()
        {
            private static final long serialVersionUID = 1L;
            /**
             * If non-null, overrides the value returned by the underlying task.
             */
            private final AtomicReference<T> override = new AtomicReference<>();

            @Override
            public T getRawResult() {
                T result = override.get();
                if (result != null) {
                    return result;
                }
                return task.getRawResult();
            }

            @Override
            protected void setRawResult(T value) {
                override.set(value);
            }

            @Override
            protected boolean exec() {
                // According to ForkJoinTask.fork() "it is a usage error to fork a task more than once unless it has completed
                // and been reinitialized". We therefore assume that this method does not have to be thread-safe.
                Map<String, String> oldContext = beforeExecution(newContext);
                try {
                    task.invoke();
                    return true;
                } finally {
                    afterExecution(oldContext);
                }
            }
        };
    }

    private Runnable wrap(Runnable task, Map<String, String> newContext)
    {
        return () -> {
        	//System.err.println("New MDC: " + newContext);
            Map<String, String> oldContext = beforeExecution(newContext);
            try {
                task.run();
            } finally {
            	afterExecution(oldContext);
            }
        };
    }

    /**
     * Invoked before running a task.
     *
     * @param newValue the new MDC context
     * @return the old MDC context
     */
    private Map<String, String> beforeExecution(Map<String, String> newValue)
    {
        Map<String, String> previous = MDC.getCopyOfContextMap();
        if (newValue == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(newValue);
        }
        return previous;
    }

    /**
     * Invoked after running a task.
     *
     * @param oldValue the old MDC context
     */
    private void afterExecution(Map<String, String> oldValue)
    {
        if (oldValue == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(oldValue);
        }
    }
    
    private static ForkJoinWorkerThreadFactory defaultForkJoinWorkerThreadFactory(String poolName) {
		return new ForkJoinWorkerThreadFactory() {
		    @Override           
		    public ForkJoinWorkerThread newThread(ForkJoinPool pool)
		    {
		        final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
		        worker.setName("ForkJoinPool." + poolName + "-" + worker.getPoolIndex());
		        return worker;
		    }
		};
    }
}
