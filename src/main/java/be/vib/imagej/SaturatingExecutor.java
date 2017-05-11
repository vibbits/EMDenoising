package be.vib.imagej;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SaturatingExecutor
{
	private ThreadPoolExecutor executor;

	private static class MyDiscardOldestPolicy extends ThreadPoolExecutor.DiscardOldestPolicy
	{
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor e)  
		{
			System.out.println("Rejected execution");
			super.rejectedExecution(r, e);
		}
	}

	public SaturatingExecutor(int poolSize, int queueSize)
	{
		executor = new ThreadPoolExecutor(poolSize,
				                          poolSize,
				                          0L,
				                          TimeUnit.SECONDS,
				                          new ArrayBlockingQueue<Runnable>(queueSize, true),
				                          new MyDiscardOldestPolicy()); // TODO: use ThreadPoolExecutor.DiscardOldestPolicy as soon as we don't want the debug prints anymore
		
		executor.setRejectedExecutionHandler(new MyDiscardOldestPolicy());
	}
	
	public void Submit(Runnable task)
	{
		System.out.println("Trying to submit");
		executor.execute(task);
	}

}
