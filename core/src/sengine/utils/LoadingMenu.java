package sengine.utils;

import sengine.Processor;
import sengine.Sys;
import sengine.Universe;
import sengine.ui.Menu;
import sengine.ui.UIElement;

public class LoadingMenu extends Menu<Universe> {
	static final String TAG = "LoadingMenu";
	
	public static interface Handler {
		void load();
		void complete(LoadingMenu menu);
		void exception(LoadingMenu menu, Throwable e);
	}
	
	class LoadingTask extends Processor.Task {
		LoadingTask() {
			super(false, true, false, true);
		}

		@Override
		protected void processAsync() {
			handler.load();
		}
	} 
	
	// UI 
	public final UIElement<?>[] elements;
	// Precacher
	public final StreamablePrecacher precacher;
	// Timing
	public final float tMinLoadingTime;
	public final float tStandardLoadingTime;
	public final float tMaxLoadingTime;
	public final float readyTimeMultiplier;
	public final boolean allowSkipLoading;
	public final boolean triggeredLoading; 
	// Handler
	Handler handler = null;
	boolean hasStarted = false;
	boolean hasEnded = false;
	LoadingTask task = null;
	
	public boolean setHandler(Handler handler) {
		if(hasStarted)
			return false;	// Already started, cannot change
		this.handler = handler;
		return true;
	}
	
	public boolean restartLoading() {
		if(task != null)
			return false;
		hasStarted = false;
		hasEnded = false;
		startLoading();
		return true;
	}
	
	public boolean startLoading() {
		if(hasStarted || handler == null)
			return false;
		// Else first time starting
		// Reset
		if(task == null) {
			task = new LoadingTask();
			task.start();
		}
		hasStarted = true;
		return true;
	}
	

	public LoadingMenu(UIElement<?> root, UIElement<?>[] elements, float tMinLoadingTime, float tStandardLoadingTime, float tMaxLoadingTime, float readyTimeMultiplier, boolean allowSkipLoading, boolean triggeredLoading) {
		// Attach root
		if(root != null) {
			root.viewport(viewport);
			root.attach();
		}
		
		this.elements = elements;
		
		// Precacher
		this.precacher = new StreamablePrecacher();
		precacher.attach(this);
		
		// Timing
		this.tMinLoadingTime = tMinLoadingTime;
		this.tStandardLoadingTime = tStandardLoadingTime;
		this.tMaxLoadingTime = tMaxLoadingTime;
		this.readyTimeMultiplier = readyTimeMultiplier;
		this.allowSkipLoading = allowSkipLoading;
		this.triggeredLoading = triggeredLoading;
		
		// Flow control
		processEnabled = true;
		inputEnabled = true;
	}

	@Override
	public void detachWithAnim() {
		if(!tryToCompleteLoading())
			return;		// Still loading
        if(elements != null) {
            for (UIElement<?> e : elements)
                e.detachWithAnim();
        }
		hasEnded = true;
	}

	@Override
	protected void recreate(Universe v) {
		// Attach all elements
        if(elements != null) {
            for (UIElement<?> e : elements)
                e.attach();
        }
		if(triggeredLoading || hasStarted)
			return;
		
		startLoading();
	}

	@Override
	protected void process(Universe v, float renderTime) {
		if(!hasStarted)
			return;
		if(hasEnded) {
			// Check if all elements has detached
            if(elements != null) {
                for (UIElement<?> e : elements)
                    if (e.isEffectivelyRendering())
                        return;
            }
			// Else all has detached
			detach();
			return;
		}
		if(renderTime < tMinLoadingTime)
			return;		// Still need to show loading screen
		// Check if thread is running
		if(task != null) {
			// Check for exception
			if(task.getError() != null) {
				// Error on load
				hasEnded = true;
				handler.exception(this, task.getError());
				return;
			}
			if(!task.isComplete())
				return;		// still loading
			// Else done loading
			task = null;
			// Inform ready
			viewport.timeMultiplier *= readyTimeMultiplier;
		}
		if(renderTime < tMaxLoadingTime) {
			// Check if precached all assets
			if(!precacher.isLoadedAll())
				return;
			else if(Processor.processor.getRemainingTasks() > 0)
				return;						// Processor seems busy
			// Else precached all and ping has been received
		}
		if(renderTime < tStandardLoadingTime)
			return;		// do not end automatically
		// Else done
		hasEnded = true;
		handler.complete(this);
	}
	
	public boolean tryToCompleteLoading() {
		if(!allowSkipLoading)
			return false;
		if(task != null)
			return false;
		// Else can complete
		if(!hasEnded) {
			hasEnded = true;
			handler.complete(this);
		}
		return true;
	}

	@Override
	protected void render(Universe v, float r, float renderTime) {
		// nothing
	}

	@Override
	protected void release(Universe v) {
		if(!hasEnded) {
			Sys.debug(TAG, "LoadingMenu was detached before loading end!");
		}
	}
	
	@Override
	protected boolean input(Universe v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
		if(inputType == INPUT_TOUCH_UP)
			return tryToCompleteLoading();
		return false;
	}
}
