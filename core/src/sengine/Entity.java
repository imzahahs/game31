package sengine;

import sengine.graphics2d.Matrices;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

public abstract class Entity<T extends Universe> {
	public static final int INPUT_TOUCH_DOWN 		= 0x01;
	public static final int INPUT_TOUCH_DRAGGED 	= 0x02;
	public static final int INPUT_TOUCH_UP 			= 0x04;
	public static final int INPUT_TOUCH 			= 0x07;
	public static final int INPUT_KEY_DOWN 			= 0x08;
	public static final int INPUT_KEY_TYPED			= 0x10;
	public static final int INPUT_KEY_UP 			= 0x20;
	public static final int INPUT_KEY				= 0x38;
	public static final int INPUT_SCROLLED			= 0x40;
	
	
	static final IntArray activeIterators = new IntArray();
	static int currentIterationID = 0;
	
	static final Array<Runnable> deferredEvents = new Array<Runnable>(Runnable.class);
	
	public static boolean useStrictLinking = false;
	
	static void reset() {
		activeIterators.clear();
		currentIterationID = 0;
		deferredEvents.clear();
		useStrictLinking = false;
	}
	
	static void executeDeferredEvents(int offset) {
		// Detect if already executing events
		if(offset == 0 && deferredEvents.items[0] == null)
			return;
		// Keep track of current batch
		int size = deferredEvents.size;
		int currentSize = size;
		for(int c = offset; c < size; c++) {
			// Retrieve event and execute
			Runnable r = deferredEvents.items[c];
			deferredEvents.items[c] = null;
			r.run();
			// Detect new batch, and recursively execute it
			if(deferredEvents.size != currentSize) {
				executeDeferredEvents(currentSize);
				currentSize = deferredEvents.size;
			}
		}
		if(offset == 0)
			deferredEvents.size = 0;			// clear
	}
	
	static class DeferredReposition implements Runnable {
		static final Pool<DeferredReposition> pool = new Pool<DeferredReposition>(16, 64) {
			@Override
			protected DeferredReposition newObject() {
				return new DeferredReposition();
			}
		};
		
		static DeferredReposition create(Entity<?> entity, Entity<?> parent, int index) {
			DeferredReposition e = pool.obtain();
			e.entity = entity;
			e.parent = parent;
			e.index = index;
			return e;
		}
		
		Entity<?> entity;
		Entity<?> parent;
		int index;
		
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			repositionEntity((Entity<Universe>)entity, (Entity<Universe>)parent, index);
			pool.free(this);
		}
	}
	
	
	public static class Group extends Entity<Universe> {
		public Matrix4 matrix = null;
		public Matrices.ScissorBox scissor = null;
		
		public Group() {
			processEnabled = true;		// By default, groups have processing enabled		
		}
		
		public Group(boolean renderingEnabled, boolean processEnabled, boolean inputEnabled) {
			this.renderingEnabled = renderingEnabled;
			this.processEnabled = processEnabled;
			this.inputEnabled = inputEnabled;
		}
		
		public void setRenderingEnabled(boolean renderingEnabled) {
			this.renderingEnabled = renderingEnabled;
		}
		
		public void setProcessEnabled(boolean processEnabled) {
			this.processEnabled = processEnabled;
		}
		
		public void setInputEnabled(boolean inputEnabled) {
			this.inputEnabled = inputEnabled;
		}
		
		@Override
		protected void render(Universe v, float r, float renderTime) {
			Matrices.push();
			if(matrix != null) {
				Matrices.model.mul(matrix);
			}
			if(scissor != null) 
				Matrices.scissor.set(scissor);
		}
		
		@Override
		protected void renderFinish(Universe v, float r, float renderTime) {
			Matrices.pop();
		}
	}
	
	public static abstract class Iterator<T extends Universe> {
		
		abstract boolean process(T v, Entity<T> c);
		abstract boolean post(T v, Entity<T> c);
		
		public int structureHash = 0;
		
		public boolean iterate(T v, Entity<T> root, boolean computeStructureHash) {
			structureHash = 0;
			if(root.isIterating())
				return false;
			// Else claim and start iterating
			currentIterationID++;
			int iterationID = currentIterationID;
			activeIterators.add(iterationID);
			try {
				root.iterationID = iterationID;
	
				if(!process(v, root))
					return true;
				if(computeStructureHash)
					structureHash ^= System.identityHashCode(root);
				if(root.child == null) {
					post(v, root);
					return true;
				}
				// Else continue
				Entity<T> c = root.child;
				while(true) {
					c.iterationID = iterationID;
					if(process(v, c)) {
						if(computeStructureHash)
							structureHash ^= System.identityHashCode(c);
						// Continue with child if available
						if(c.child != null) {
							c = c.child;
							continue;
						}
						// Else finish process this entity
						if(!post(v, c))
							return true;
					}
					while(true) {
						if(c.next != null) {
							c = c.next;
							break;
						}
						else if(c.parent == root) {
							post(v, root);
							return true;
						}
						c = c.parent;
						if(!post(v, c))
							return true;
					}
				}
			} finally {
				activeIterators.removeValue(iterationID);
				if(activeIterators.size == 0)
					executeDeferredEvents(0);		// Execute deferred events
			}
		}
		
		public boolean iterateReverse(T v, Entity<T> root, boolean computeStructureHash) {
			structureHash = 0;
			if(root.isIterating())
				return false;
			// Else claim and start iterating
			currentIterationID++;
			int iterationID = currentIterationID;
			activeIterators.add(iterationID);
			try {
				root.iterationID = iterationID;
	
				if(!process(v, root))
					return true;
				if(computeStructureHash)
					structureHash ^= System.identityHashCode(root);
				if(root.last == null) {
					post(v, root);
					return true;
				}
				// Else continue
				Entity<T> c = root.last;
				while(true) {
					c.iterationID = iterationID;
					if(process(v, c)) {
						if(computeStructureHash)
							structureHash ^= System.identityHashCode(c);
						// Continue with child if available
						if(c.last != null) {
							c = c.last;
							continue;
						}
						// Else finish process this entity
						if(!post(v, c))
							return true;
					}
					while(true) {
						if(c.previous != null) {
							c = c.previous;
							break;
						}
						else if(c.parent == root) {
							post(v, root);
							return true;
						}
						c = c.parent;
						if(!post(v, c))
							return true;
					}
				}
			} finally {
				activeIterators.removeValue(iterationID);
				if(activeIterators.size == 0)
					executeDeferredEvents(0);		// Execute deferred events
			}
		}
	}

	static class RecreateIterator extends Iterator<Universe> {
		@Override
		boolean process(Universe v, Entity<Universe> c) {
			if(c.isEffectivelyAttached)
				return false;			// already attached
			c.renderDeltaTime = c.renderTime = c.lastProcessTime = c.r = 0.0f;
			c.isEffectivelyAttached = true;
			c.recreate(v);
			return true;			// next
		}

		@Override
		boolean post(Universe v, Entity<Universe> c) {
			return true;			// continue
		}
	}
	static final RecreateIterator recreateIterator = new RecreateIterator();

	static class ReleaseIterator extends Iterator<Universe> {
		@Override
		boolean process(Universe v, Entity<Universe> c) {
			return true;			// next
		}

		@Override
		boolean post(Universe v, Entity<Universe> c) {
			if(!c.isEffectivelyAttached)
				return false;			// already not attached
			// Release in reverse
			c.isEffectivelyAttached = false;
			c.release(v);
			c.renderDeltaTime = c.renderTime = c.lastProcessTime = c.r = 0.0f;
			return true;			// continue
		}
	}
	static final ReleaseIterator releaseIterator = new ReleaseIterator();
	

	static void repositionEntity(Entity<Universe> entity, Entity<Universe> parent, int index) {
		// Inferred that entity should be not iterating here 
		// Determine if need to load or unload
		boolean parentIsEffectivelyAttached = parent != null ? parent.isEffectivelyAttached : false;
		boolean entityWasEffectivelyAttached = entity.isEffectivelyAttached;
		
		if(entity.parent != null) {
			// Entity is already attached, detach from there first
			if(entity.previous == null)
				entity.parent.child = entity.next;
			else
				entity.previous.next = entity.next;
			if(entity.next == null)
				entity.parent.last = entity.previous;
			else
				entity.next.previous = entity.previous;
			entity.parent = null;
			entity.previous = null;
			entity.next = null;
		}
		
		// Now check if need to load
		// Entity should be detached here, do all loading or unloading
		Universe universe = Sys.system.getUniverse();
		if(entityWasEffectivelyAttached) {
			// Entity was already attached to this universe, so already loaded before
			if(!parentIsEffectivelyAttached)
				releaseIterator.iterateReverse(universe, entity, false);
			// Else is just changing places
		}
		// Else neither entity or parent is attached to Universe
		
		// Now create attachment to parent
		if(parent == null)
			return;				// no parent to attach
		entity.parent = parent;
		if(index == -1 || parent.child == null) {
			// Default to last
			entity.previous = parent.last;
			if(parent.last != null)
				parent.last.next = entity;
			else
				parent.child = entity;
			parent.last = entity;
		}
		else {
			// Find replacement entity
			Entity<Universe> replacement;
			if(index >= 0) {
				replacement = parent.child;
				while(index > 0 && replacement.next != null) {
					replacement = replacement.next;
					index--;
				}
			}
			else {
				index++;
				replacement = parent.last;
				while(index < 0 && replacement.previous != null) {
					replacement = replacement.previous;
					index++;
				}
			}
			// Replace this entity's position
			if(replacement.previous == null)
				parent.child = entity;
			else
				replacement.previous.next = entity;
			entity.previous = replacement.previous;
			replacement.previous = entity;
			entity.next = replacement;
		}
		
		if(!entityWasEffectivelyAttached && parentIsEffectivelyAttached)
			recreateIterator.iterate(universe, entity, false);		// Else entity wasnt loaded before, and attaching to universe, so load now
	}
	
	
	
	// Node graph
	Entity<T> parent = null;
	Entity<T> next = null;
	Entity<T> previous = null;
	Entity<T> child = null;
	Entity<T> last = null;
	int iterationID = -1;
	// Time control
	float renderDeltaTime = 0.0f;
	float renderTime = 0.0f;
	float lastProcessTime = 0.0f;
	float r = 0.0f;
	public float timeMultiplier = 1.0f;
	float effectiveTimeMultiplier = 1.0f;
	// Flow control
	public boolean renderingEnabled = true;
	public boolean processEnabled = false;
	public boolean inputEnabled = false;
	boolean isEffectivelyAttached = false;


	@SuppressWarnings("unchecked")
	public <I> I findParent(Class<I> type) {
		Entity<?> parent = this.parent;
		// Keep finding a parent which can be assigned to type
		while(parent != null && !type.isAssignableFrom(parent.getClass()))
			parent = parent.parent;
		return (I) parent;
	}


	public boolean isIterating() { return activeIterators.contains(iterationID); }
	
	public Entity<?> getEntityParent() { return parent; }
	public Entity<?> getEntityNext() { return next; }
	public Entity<?> getEntityPrevious() { return previous; }
	public Entity<?> getEntityChild() { return child; }
	public Entity<?> getEntityLast() { return last; }

	public boolean isAttached() {
		return parent != null;
	}
	
	public boolean isRenderingEnabled() {
		return renderingEnabled;
	}
	
	public boolean isProcessEnabled() { 
		return processEnabled;
	}
	
	public boolean isInputEnabled() {
		return inputEnabled;
	}
	
	public boolean isEffectivelyAttached() {
		return isEffectivelyAttached;
	}
	
	public boolean isEffectivelyRendering() {
		if(parent == null)
			return false;
		Entity<T> e = this;
		do {
			if(!e.renderingEnabled)
				return false;
			e = e.parent;
		} while(e.parent != null);
		return e.isEffectivelyRendering();
	}
	
	public boolean isEffectivelyProcessing() {
		if(parent == null)
			return false;
		Entity<T> e = this;
		do {
			if(!e.processEnabled)
				return false;
			e = e.parent;
		} while(e.parent != null);
		return e.isEffectivelyProcessing();
	}
	
	public boolean isEffectivelyReceiving() {
		if(parent == null)
			return false;
		Entity<T> e = this;
		do {
			if(!e.inputEnabled)
				return false;
			e = e.parent;
		} while(e.parent != null);
		return e.isEffectivelyReceiving();
	}
	
	public final float getRenderDeltaTime() { return renderDeltaTime; }
	public final float getRenderTime() { return renderTime; }
	public final float getRenderTimeR() { return r; }
	public final float getLastProcessTime() { return lastProcessTime; }
	public final float getEffectiveTimeMultiplier() { return effectiveTimeMultiplier; }

	@SuppressWarnings("unchecked")
	public void reposition(Entity<?> parent, int index) {
		// If both parent and entity was not attached and both entity were not iterated, just reposition them now
		boolean parentIsEffectivelyAttached = parent != null && parent.isEffectivelyAttached;
		boolean entityWasEffectivelyAttached = isEffectivelyAttached;
		boolean parentWasIterated = parent != null && parent.iterationID != -1;
		boolean entityWasIterated = iterationID != -1;
		if(!parentIsEffectivelyAttached && !entityWasEffectivelyAttached && !parentWasIterated && !entityWasIterated) {
			repositionEntity((Entity<Universe>) this, (Entity<Universe>) parent, index);
			return;
		}
		// If attaching from another thread, post to rendering thread
		if(useStrictLinking) { // 20170903 Is this still needed ? || Thread.currentThread() != Sys.system.getRenderingThread()) {
			DeferredReposition e = new DeferredReposition();
			e.entity = this;
			e.parent = parent;
			e.index = index;
			Sys.system.universe.postMessage(e);
		}
		else if(isIterating()) {
			// Else currently iterating, defer to end of iteration 
			DeferredReposition e = DeferredReposition.create(this, parent, index);
			deferredEvents.add(e);
		}
		else
			repositionEntity((Entity<Universe>)this, (Entity<Universe>)parent, index);
	}
	
	public void attach(Entity<?> parent) {
		attach(parent, -1);			// default to last
	}
	
	public void attach(Entity<?> parent, int index) {
		if(parent == null)
			throw new IllegalArgumentException("Cannot attach to null parent: " + this);
		// Reposition as specified
		reposition(parent, index);
	}
	
	public void attachChilds(Entity<?> parent) {
		while(child != null)
			child.attach(parent);
	}
	
	public void detach() {
		// Reposition to null parent
		reposition(null, -1);
	}

	public void detachChilds(Entity<?> ... except) {
		Entity<T> c = child;
		while(c != null) {
			boolean exempted = false;
			for(int i = 0; i < except.length; i++) {
				if(except[i] == c) {
					exempted = true;
					break;
				}
			}
			Entity<T> current = c;
			c = c.next;
			if(!exempted)
				current.detach();
		}
	}
	
	public Entity<?>[] getChilds() {
		// Calculate number of childs
		int numChilds = 0;
		Entity<T> c = child;
		while(c != null) {
			c = c.next;
			numChilds++;
		}
		Entity<?>[] childs = new Entity[numChilds];
		numChilds = 0;
		c = child;
		while(c != null) {
			childs[numChilds++] = c;
			c = c.next;
		}
		return childs;
	}

	public <T extends Entity<?>> T[] getChilds(Class<T> type) {
		// Calculate number of childs
		int numChilds = 0;
		Entity<?> c = child;
		while(c != null) {
			if(type.isAssignableFrom(c.getClass()))
				numChilds++;
			c = c.next;
		}
		T[] childs = (T[]) java.lang.reflect.Array.newInstance(type, numChilds);
		numChilds = 0;
		c = child;
		while(c != null) {
			if(type.isAssignableFrom(c.getClass()))
				childs[numChilds++] = (T) c;
			c = c.next;
		}
		return childs;
	}

    public <C extends Entity<?>> int getNumChilds(Class<C> type) {
        int childs = 0;
        Entity<T> c = child;
        while(c != null) {
            if(type.isAssignableFrom(c.getClass()))
                childs++;
            c = c.next;
        }
        return childs;
    }

	public <C extends Entity<?>> C getChild(Class<C> type) {
		return getChild(type, 0);
	}

	@SuppressWarnings("unchecked")
	public <C extends Entity<?>> C getChild(Class<C> type, int index) {
		Entity<T> c;
		if(index < 0) {
			c = last;
			while(c != null) {
				if(type.isAssignableFrom(c.getClass())) {
					if(index == -1)
						return (C)c;
					index++;
				}
				c = c.previous;
			}
		}
		else {
			c = child;
			while(c != null) {
				if(type.isAssignableFrom(c.getClass())) {
					if(index == 0)
						return (C)c;
					index--;
				}
				c = c.next;
			}
		}
		return null;
	}

	public Entity<?> iterate(Entity<?> previous, Entity<?>[] excluded) {
		while(true) {
			if(previous == null)
				previous = child;
			else if(previous.child != null)
				previous =  previous.child;
			else {
				// Else try sibling or keep rolling up
				while (previous.next == null) {
					previous = previous.parent;
					if (previous == this || previous == null)
						return null;        // reached root or this
				}
				previous = previous.next;
			}
			if(excluded != null) {
				boolean isExcluded = false;
				for(int c = 0; c < excluded.length; c++) {
					if(previous == excluded[c]) {
						isExcluded = true;
						break;
					}
				}
				if(isExcluded) {
					while(previous.child != null)
						previous = previous.last;
					continue;
				}
			}
			return previous;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <U extends Entity<?>> U iterate(U previous, Class<U> type, boolean allowSubclasses, Entity<?>[] excluded) {
		Entity<?> p = previous;
		if(allowSubclasses) {
			while((p = iterate(p, excluded)) != null) {
				// Check type
				if(type.isAssignableFrom(p.getClass()))
					break;
			}
		}
		else {
			while((p = iterate(p, excluded)) != null) {
				// Check type
				if(type == p.getClass())
					break;
			}
		}
		return (U) p;
	}

	// User implementation
	protected void recreate(T v) {
		
	}
	protected void process(T v, float renderTime) {
		
	}
	protected void render(T v, float r, float renderTime) {
		
	}
	protected void renderFinish(T v, float r, float renderTime) {
		
	}
	protected void release(T v) {
		
	}
	protected boolean input(T v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
		return false;			// do not absorb
	}
}
