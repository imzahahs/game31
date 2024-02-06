package sengine;

import java.util.concurrent.ConcurrentLinkedQueue;

import sengine.utils.Console;

import com.badlogic.gdx.graphics.Camera;

public abstract class Universe extends Entity<Universe> {
	static final String TAG = "Universe";
	
	public final float fixedProcessInterval;
	
	// Message queue
	final ConcurrentLinkedQueue<Runnable> messages = new ConcurrentLinkedQueue<Runnable>();
	
	public Universe(float fixedProcessInterval) {
		this.fixedProcessInterval = fixedProcessInterval;
		
		// Activate processing and input by default
		processEnabled = true;
		inputEnabled = true;
	}
	
	private static class InputIterator extends Iterator<Universe> {
		int inputType = 0;
		int key = 0;
		char character = 0;
		int scrolledAmount = 0;
		int pointer = 0;
		float x = 0;
		float y = 0;
		int button = 0;
		boolean handled = false;

		@Override
		boolean process(Universe v, Entity<Universe> c) {
			return c.renderingEnabled && c.inputEnabled;					// Added check for rendering 20171002
		}

		@Override
		boolean post(Universe v, Entity<Universe> c) {
			handled = c.input(v, inputType, key, character, scrolledAmount, pointer, x, y, button);
			if(handled)
				return false;		// no need to continue
			return true;			// continue
		}
	}
	private static final InputIterator inputIterator = new InputIterator();
	
	private static class TimestepIterator extends Iterator<Universe> {
		float delta;
		boolean processingNeeded = false;

		@Override
		boolean process(Universe v, Entity<Universe> c) {
			// Update timing
			c.effectiveTimeMultiplier = c.timeMultiplier;
			if(c.parent != null)
				c.effectiveTimeMultiplier *= c.parent.effectiveTimeMultiplier;
			if(!c.renderingEnabled && !c.processEnabled)
				return false;
			c.renderDeltaTime = c.effectiveTimeMultiplier * delta;
			c.renderTime += c.renderDeltaTime;
			if(c.lastProcessTime < c.renderTime)
				processingNeeded = true;
			return true;
		}

		@Override
		boolean post(Universe v, Entity<Universe> c) {
			return true;		// nothing
		}
	}
	private static final TimestepIterator timestepIterator = new TimestepIterator();
	
	private static class ProcessIterator extends Iterator<Universe> {
		
		@Override
		boolean process(Universe v, Entity<Universe> c) {
			if(!c.processEnabled)
				return false;		// no processing here
			while(c.lastProcessTime < c.renderTime) {
				c.lastProcessTime += v.fixedProcessInterval;
				c.process(v, c.lastProcessTime);
			}
			return true;
		}

		@Override
		boolean post(Universe v, Entity<Universe> c) {
			return true;		// nothing
		}
	}
	private static final ProcessIterator processIterator = new ProcessIterator();
	
	private static class RenderIterator extends Iterator<Universe> {

		@Override
		boolean process(Universe v, Entity<Universe> c) {
			if(!c.renderingEnabled)
				return false;		// not rendering
			// Calculate R
			if(c.lastProcessTime < c.renderTime) {
				c.lastProcessTime = c.renderTime;
				c.r = 1.0f;
			}
			else 
				c.r = 1.0f - ((c.lastProcessTime - c.renderTime) / v.fixedProcessInterval);
			// Render
			c.render(v, c.r, c.renderTime);
			return true;
		}

		@Override
		boolean post(Universe v, Entity<Universe> c) {
			// Render finish 
			c.renderFinish(v, c.r, c.renderTime);
			return true;
		}
	}
	private static final RenderIterator renderIterator = new RenderIterator();
	
	
	

	@Override
	public final void reposition(Entity<?> parent, int index) {
		throw new RuntimeException("A Universe cannot be repositioned");
	}

	public void postMessage(Runnable message) {
		messages.add(message);
	}

	void processMessages() {
		Runnable r;
		while((r = messages.poll()) != null)
			r.run();
	}

	public boolean processInput(int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {

		long time1 = System.nanoTime();

		inputIterator.inputType = inputType;
		inputIterator.key = key;
		inputIterator.character = character;
		inputIterator.scrolledAmount = scrolledAmount;
		inputIterator.pointer = pointer;
		inputIterator.x = x;
		inputIterator.y = y;
		inputIterator.button = button;
		inputIterator.handled = false;		// reset
		
		try {
			inputIterator.iterateReverse(this, this, false);
			return inputIterator.handled;
		} catch (Throwable e) {
			if(Console.console == null || Console.universeRestartCode == null)
				throw new RuntimeException("Universe processInput failed: " + this, e);
			Sys.error(TAG, "Universe processInput failed: " + this, e);
            Sys.system.activate(Sys.system.dummyUniverse);
            Console.console.showRestartCode();
			Console.console.show();
			return false;
		} finally {
			// Calculate input time
			long time2 = System.nanoTime();
			Sys.system.statMessagesTime += time2 - time1;
		}
	}

	int processRender(float delta) {
		// First update timestep
		timestepIterator.delta = delta;
		timestepIterator.processingNeeded = false;
		timestepIterator.iterate(this, this, false);
		// Update process loop if has any
		if(timestepIterator.processingNeeded)
			processIterator.iterate(this, this, false);
		// Update render loop
		renderIterator.iterate(this, this, true);
		return renderIterator.structureHash;
	}

	// Universe handling
	protected abstract void pause();
	protected abstract float resize(int width, int height);
	protected abstract void resume();
	protected abstract void stopped();
	
	// Camera handling
	public abstract Camera getCamera();

	@Override
	public boolean isAttached() {
		return Sys.system.getUniverse() == this;
	}
	
	@Override
	public boolean isEffectivelyAttached() {
		return Sys.system.getUniverse() == this;
	}
	
	@Override
	public boolean isEffectivelyRendering() {
		return renderingEnabled;
	}
	
	@Override
	public boolean isEffectivelyProcessing() {
		return processEnabled;
	}
	
	@Override
	public boolean isEffectivelyReceiving() {
		return inputEnabled;
	}
}