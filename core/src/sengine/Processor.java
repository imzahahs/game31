package sengine;

import com.badlogic.gdx.utils.Array;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Processor {
	static final String TAG = "Processor";
	
	// Processor
	public static Processor processor = null;
    public static int maxThreads = 4;
	public static int minThreads = 2;
	
	public static class TaskException extends RuntimeException {
		private static final long serialVersionUID = 5436118074456361659L;
		
		public final Task t;
		
		private TaskException(Task t, Throwable e) {
			super("Exception occured in task " + t + " with " + e, e);
			this.t = t;
			if(t.ignoreException)
				Sys.error(TAG, "Exception ignored in " + t, e);
		}
	}
	
	private class AsyncThread extends Thread {
		
		AsyncThread(String name) {
			super(name);
		}

		@Override
		public void run() {
			// Timed tasks
			Array<Task> timedTasks = new Array<Task>(Task.class);
			// Eternally process async messages
			while(true) {
				// Take a task to process
				Task t = null;
				long currentTime = System.currentTimeMillis();
				long timedTaskTime = Long.MAX_VALUE;
				Task timedTask = null;
				for(int c = 0; c < timedTasks.size; c++) {
					t = timedTasks.items[c];
					long delay = t.executeMillis - currentTime;
					if(delay < timedTaskTime) {
						timedTaskTime = delay;
						timedTask = t;
					}
				}
				if(timedTask == null || (timedTaskTime >= 0 && !executeFutureTasks)) {
					try {
						t = tasksForAsync.pollFirst(timedTaskTime, TimeUnit.MILLISECONDS);
					} catch(InterruptedException e) {
						if(!isReleasing)
							continue;
						return;		// Requested to die
					}
					// Elapsed specified wait time, select timed task automatically
					if(t == null) {
						if(timedTask == null)
							continue;		// prevent UB
						timedTasks.removeValue(timedTask, true);
						t = timedTask;
					}
					else if(!executeFutureTasks && t.executeMillis > System.currentTimeMillis()) {
						// Else this task is supposed to be done at a later time, remember and postpone
						timedTasks.add(t);
						continue;
					}
					// Else can execute this task
				}
				else {
					timedTasks.removeValue(timedTask, true);
					t = timedTask;
				}
				// Process task's async
				t.processorPerformAsync();
				remainingAsync.decrementAndGet();
			}
		}
	}

//	public static void enforceAccessThread() {			// 20181103: This crashes on weird devices
//		if(Thread.currentThread() != Sys.system.getRenderingThread())
//			throw new IllegalThreadStateException("Can only access on rendering thread, this thread: " + Thread.currentThread());
//	}
	
	// Executor
	final AsyncThread[] threads;
	// Current
	final LinkedBlockingDeque<Task> tasksForAsync = new LinkedBlockingDeque<Task>();
	final ConcurrentLinkedQueue<Task> tasksForSyncImmediate = new ConcurrentLinkedQueue<Task>();
	final ConcurrentLinkedQueue<Task> tasksForSync = new ConcurrentLinkedQueue<Task>();
	final AtomicInteger remainingAsync = new AtomicInteger();
	final AtomicInteger remainingSync = new AtomicInteger();

	volatile boolean executeFutureTasks = false;
	volatile boolean isReleasing = false;
	
	public int getRemainingTasks() { return remainingAsync.get() + remainingSync.get(); }
	
	void addSync(Task task, boolean immediate) {
		if(immediate)
			tasksForSyncImmediate.add(task);
		else
			tasksForSync.add(task);
        remainingSync.incrementAndGet();
	}
	
	boolean removeSync(Task task) {
		if(tasksForSyncImmediate.remove(task))
            remainingSync.decrementAndGet();
		else if(tasksForSync.remove(task))
            remainingSync.decrementAndGet();
		else
			return false;
		return true;
	}
	
	void addAsync(Task task) {
		tasksForAsync.add(task);
        remainingAsync.incrementAndGet();
    }

	public int getMaxThreads() {
		return threads.length;
	}
	
	public void updateFinish() {
		executeFutureTasks = true;
//		for(int c = 0; c < threads.length; c++)
//			threads[c].interrupt();						not sure the reason for this.
		while(updateSingle(true));
		executeFutureTasks = false;
	}
	
	public boolean update(float t) {
		long c = System.nanoTime();
		while(updateSingle(true)) {
			// Evaluate time constraint
			double e = (double)(System.nanoTime() - c) / 1000000000.0;
			if(e > t)
				return true;		// still have processing to do
		}
		// Else completed
		return false;
	}

	public boolean updateSingle() {
		return updateSingle(false);
	}
	
	Task retrieveExecutableTask(ConcurrentLinkedQueue<Task> queue) {
		float tSysTime = Sys.getTime();
		Task t = null;
		Task wrap = null;
		wrap = t = queue.poll();
		if(t == null)
			return null;	// none found
		while(true) {
			if(t.tExecuteSysTime <= tSysTime || executeFutureTasks)
				return t;
			// Else not yet time, add back
			queue.add(t);		// Is not yet time, add back
			t = queue.poll();
			if(t == wrap) {
				// Made a full cycle, all tasks are not executing
				queue.add(t);
				return null;
			}
		}
	}

	public boolean updateSingle(boolean yieldWhenNoUpdates) {
//		enforceAccessThread();			// 20181103: This crashes on weird devices
		if(getRemainingTasks() == 0)		// no tasks
			return false;
		// Process one immediate sync
		Task t = retrieveExecutableTask(tasksForSyncImmediate); 
		// If a sync task is available do it now
		if(t == null) {
			// Else find a normal sync task
			t = retrieveExecutableTask(tasksForSync);	
			// If still cant find tasks, yield
			if(t == null) {
				if(yieldWhenNoUpdates)
					Thread.yield();		// No tasks for syncing, give some processing time to async threads
				return true;	// still got tasks to go
			}
			
		}
		// Else there is a task to sync, process a single sync action
		t.processorPerformSync();
        remainingSync.decrementAndGet();
		return true;
	}

	public Processor() {
		this(Runtime.getRuntime().availableProcessors(), TAG, Thread.NORM_PRIORITY);		// TODO????
	}
	
	public Processor(int numThreads, final String name, final int priority) {
		if(numThreads < minThreads)
			numThreads = minThreads;
        if(numThreads > maxThreads)
            numThreads = maxThreads;
		// Create threads
		Sys.info(TAG, "Creating " + numThreads + " threads for " + name);
		this.threads = new AsyncThread[numThreads];
		for(int c = 0; c < numThreads; c++) {
			threads[c] = new AsyncThread(name + "-" + c);
			threads[c].setDaemon(true);
			threads[c].setPriority(priority);
			threads[c].start();
		}
	}
	
	public void release() {
		// Finish all first
		updateFinish();
		// Ask all threads to die
		isReleasing = true;
		for(int c = 0; c < threads.length; c++)
			threads[c].interrupt();
	}
	
	public static class Task {

		public static final int STATE_IDLE = 0;
		public static final int STATE_STARTED = 1;
		public static final int STATE_ASYNC = 2;
		public static final int STATE_SYNC = 3;
		public static final int STATE_COMPLETED = 4;
		
		
		protected boolean immediate = false;
		protected boolean async = false;
		protected boolean sync = false;
		protected boolean ignoreException = false;
		public long executeMillis = -1;
		public float tExecuteSysTime = -1;
		
		int state = 0;
		TaskException e = null;
		
		synchronized void processorPerformSync() {
			boolean finishedSync = true;
			
			try {
				finishedSync = completeSync();
			} catch(Throwable error) {
				e = new TaskException(this, error);
			}
			
			if(!finishedSync)
				processor.addSync(this, immediate);
			else {
                state = STATE_COMPLETED;
                try {
                    completed();
                } catch (Throwable error) {
                    Sys.error(TAG, "Error on complete", error);
                }
            }
		}
		
		void processorPerformAsync() {
			try {
				processAsync();
			} catch(Throwable error) {
				e = new TaskException(this, error);
			}
			synchronized (this) {
                // Check if need sync
                if (e == null && sync) {
                    processor.addSync(this, immediate);
                    state = STATE_SYNC;
                } else {
                    state = STATE_COMPLETED;
                    try {
                        completed();
                    } catch (Throwable error) {
                        Sys.error(TAG, "Error on complete", error);
                    }
                }
                notifyAll();
            }
		}
		
		public Task() {
			// default values
		}
		
		public Task(boolean immediate, boolean async, boolean sync, boolean ignoreException) {
			this.immediate = immediate;
			this.async = async;
			this.sync = sync;
			this.ignoreException = ignoreException;
		}
		
		public synchronized boolean isStarted() {
			return state == STATE_IDLE;
		}
		
		public synchronized Throwable getError() {
			if(state == STATE_IDLE)
				return null;
			return e;
		}
		
		public synchronized boolean isRunning() {
			int currentState = state;
			return currentState > STATE_IDLE && currentState < STATE_COMPLETED;
		}
		
		public synchronized boolean isComplete() {
			if(state != STATE_COMPLETED)
				return false; 
			else if(e != null) {
				if(!ignoreException)
					throw e;			// propogate to whoever is looking at this
				return false;
			}
			// Else done
			return true;
		}
		
		public synchronized int getState() {
			return state;
		}
		
		public synchronized boolean reset() {
			int currentState = state;
			if(currentState > STATE_IDLE && currentState < STATE_COMPLETED)
				return false;		// Cannot reset as already started
			// Else reset
			state = STATE_IDLE;
			e = null;
			return true;
		}

		public synchronized boolean resetAndStart() {
			if(!reset())
				return false;
			start();
			return true;
		}

		public synchronized void start() {
			if(state > STATE_IDLE) {
                Sys.error(TAG, "Task already started " + state + " " + this);
                return;            // task already started
            }
			// Add to proccessor
			if(async) {
				state = STATE_ASYNC;
				processor.addAsync(this);
			}
			else if(sync) {
				state = STATE_SYNC;
				processor.addSync(this, immediate);
			}
			else {
				state = STATE_COMPLETED;
				Sys.debug(TAG, "Empty task started: " + this);
                try {
                    completed();
                } catch (Throwable error) {
                    Sys.error(TAG, "Error on complete", error);
                }
				return;
			}
		}
		
		public synchronized void finish() {
			// Calculate total time taken
			long tStartTime = System.nanoTime();
			try {
				int currentState = state;
				if(currentState == STATE_IDLE) {
					// Havent started yet
					if(async)
						currentState = STATE_ASYNC;
					else if(sync)
						currentState = STATE_SYNC;
					else {
						currentState = STATE_COMPLETED;
						Sys.debug(TAG, "Empty task finishing: " + this);
						return;
					}
				}
				else if(currentState == STATE_ASYNC) {
					// Need to wait async processor to be done with this first
					while((currentState = state) < STATE_SYNC)
						wait();
					// Remove from sync list if async processor put it there
					if(currentState == STATE_SYNC)
						processor.removeSync(this);
					else if(e != null)		// Else currentState == STATE_COMPLETED, check if any errors occured during async
						throw e;
				}
				else if(currentState == STATE_SYNC) 
					processor.removeSync(this);			// Remove from sync processor, this is guaranteed to return true as we are in rendering thread
				else { // if(currentState == STATE_COMPLETED)
					if(e != null)
						throw e;
					return;
				}
				
				// Process async if needed
				if(currentState == STATE_ASYNC) {
					processAsync();
					if(sync)
						currentState = STATE_SYNC;
				}
				
				if(currentState == STATE_SYNC) {
//					enforceAccessThread();			// 20181103: This crashes on weird devices
					// Complete sync totally
					while(!completeSync());
				}
				
				// Done
				state = STATE_COMPLETED;
                try {
                    completed();
                } catch (Throwable error) {
                    Sys.error(TAG, "Error on complete", error);
                }
				
			} catch(Throwable error) {
				// Catch any errors, either from async or sync
				e = new TaskException(this, error);
				state = STATE_COMPLETED;
                try {
                    completed();
                } catch (Throwable completeError) {
                    Sys.error(TAG, "Error on complete", completeError);
                }
				throw e;
			}
			finally {
				long tEndTime = System.nanoTime();
				long tElapsed = tEndTime - tStartTime;
				Sys.system.statForcedProcessorTime += tElapsed;
				Sys.system.tFrameTimeAdjustment -= (double)tElapsed / 1000000000.0;
			}
		}
		
		// User implementation
		protected void processAsync() {
		}
		protected boolean completeSync() {
			return true;
		}

        protected void completed() {

        }
	}
}
