package sengine;

import sengine.audio.Audio;
import sengine.audio.Stream;
import sengine.graphics2d.Fonts;
import sengine.graphics2d.Material;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.Renderer;
import sengine.utils.Console;
import sengine.utils.LiveEditor;
import sengine.utils.Universe2D;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.WindowedMean;

public class Sys {
	static final String TAG = "Sys";
	
	// Log messaging
	public static void info(final String source, final String text) {
		system.log(source, text);
		if(Console.console != null) {
			if(Thread.currentThread() != system.getRenderingThread()) {
				system.getUniverse().postMessage(new Runnable() {
					@Override
					public void run() {
						if(Console.console != null)
							Console.console.info(source, text);
					}
				});
			}
			else
				Console.console.info(source, text);
		}
	}
	public static void info(final String source, final String text, final Throwable exception) {
		system.log(source, text, exception);
		if(Console.console != null) {
			if(Thread.currentThread() != system.getRenderingThread()) {
				system.getUniverse().postMessage(new Runnable() {
					@Override
					public void run() {
						if(Console.console != null)
							Console.console.info(source, text, exception);
					}
				});
			}
			else
				Console.console.info(source, text, exception);
		}
	}
	public static void debug(final String source, final String text) {
		system.logDebug(source, text);
		if(Console.console != null) {
			if(Thread.currentThread() != system.getRenderingThread()) {
				system.getUniverse().postMessage(new Runnable() {
					@Override
					public void run() {
						if(Console.console != null)
							Console.console.debug(source, text);
					}
				});
			}
			else
				Console.console.debug(source, text);
		}
	}
	public static void debug(final String source, final String text, final Throwable exception) {
		system.logDebug(source, text, exception);
		if(Console.console != null) {
			if(Thread.currentThread() != system.getRenderingThread()) {
				system.getUniverse().postMessage(new Runnable() {
					@Override
					public void run() {
						if(Console.console != null)
							Console.console.debug(source, text, exception);
					}
				});
			}
			else
				Console.console.debug(source, text, exception);
		}
	}
	public static void error(final String source, final String text) {
		system.logError(source, text);
		if(Console.console != null) {
			if(Thread.currentThread() != system.getRenderingThread()) {
				system.getUniverse().postMessage(new Runnable() {
					@Override
					public void run() {
						if(Console.console != null)
							Console.console.error(source, text);
					}
				});
			}
			else
				Console.console.error(source, text);
		}
	}
	public static void error(final String source, final String text, final Throwable exception) {
		system.logError(source, text, exception);
		if(Console.console != null) {
			if(Thread.currentThread() != system.getRenderingThread()) {
				system.getUniverse().postMessage(new Runnable() {
					@Override
					public void run() {
						if(Console.console != null)
							Console.console.error(source, text, exception);
					}
				});
			}
			else
				Console.console.error(source, text, exception);
		}
	}
	
	static class TimerThread extends Thread {
		
		TimerThread() {
			super("Sys timer thread");
		}

        private final Object requestLock = new Object();
		
		public long waitMillis = 0;
		@Override
		public void run() {
			while(true) {
				try {
					// Wait for notify()
                    synchronized (requestLock) {
                        requestLock.wait();
                    }
					// Sleep millis
					Thread.sleep(waitMillis);
					// Request render
					Gdx.graphics.requestRendering();
				} catch (InterruptedException e) {
					return;		// requested to die
				}
			}
		}
		
		public void requestRendering(float seconds) {
			// Set time and notify
            synchronized (requestLock) {
                waitMillis = (long) (seconds * 1000f);
                requestLock.notify();
            }
		}
	}
	
	/**
	 * LibGDX Wrapper
	 * @author someguy233
	 *
	 */
	public class GDXApplicationListener implements ApplicationListener, InputProcessor {

		@Override
		public void create() {
			// Thread priority
            // Timer thread
            timerThread = new TimerThread();
            timerThread.setDaemon(true);
            timerThread.start();
			// Initialize globals
            // Entities
            Entity.reset();
			// Pixmap
//			Pixmap.setBlending(Pixmap.Blending.None);
			// Shaders
			ShaderProgram.pedantic = false;
			// Active system
			if(system != null)
				Sys.debug(TAG, "Unreleased renderer");
			system = Sys.this;
			// Reset renderer
			Renderer.renderer = null;
			// Reset time
			time = 0.0f;
			// Sprite batch
			sb = new SpriteBatch();
			// File system
			File.reset();
			// Matrices
			Matrices.reset();
			// Materials
			Material.reset();
			// Processor
			Processor.processor = new Processor();
			// Audio
			Audio.reset();
			// Initialize caches
			Cache.initializeTables();
			// Input
			Gdx.input.setInputProcessor(applicationListener);
			Gdx.input.setCatchBackKey(true);
			// Hack for iOS backend, does not call resize()
			if(Gdx.app.getType() == ApplicationType.iOS)
				resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			tFrameStarted = System.nanoTime();
		}
		
		@Override
		public void dispose() {
			Processor.processor.updateFinish();
			destroyed = true;
			Renderer.renderer.clearInstructions();
			// Release universe
			universe.processMessages();
			universe.stopped();
			universe.release(universe);
			// Destroy user renderer
			destroyed();
			// Matrices
			Matrices.reset();
			// Finish GC
			GarbageCollector.performGC(true);
			// Release cache
			Cache.releaseTables();
			// Reset File Systems if needed
			if(!Cache.persistentReferences)
				File.reset();
			// Processor
			Processor.processor.release();
			Processor.processor = null;
			// Sprite batch
			sb.dispose();
			sb = null;
			renderingThread = null;
			// Timer thread
			timerThread.interrupt();
		}
		
		
		
		// LibGDX renderer implementation
		@Override
		public void pause() {
			Sys.info(TAG, "Paused");
			// Inform universe
			universe.pause();
			// Stop all sounds
			Stream.stopAllStreams();
			// Release as much memory as possible
			if(Gdx.app.getType() != ApplicationType.Desktop && Gdx.app.getType() != ApplicationType.iOS)
				GarbageCollector.performGC(true);
			// Pause music
			Audio.pauseMusic();
		}
		
		@Override
		public void render() {
            // Font debug
            Fonts.debugFontCount = 0;

            long time1 = System.nanoTime();
            long time2;
            long tFrameStarted = time1;
			long tFrameEnded = tFrameStarted;
            long tFrameElapsed = tFrameEnded - Sys.this.tFrameStarted;

            // Calculate swap time
            statTotalTime += tFrameElapsed;

            float elapsed = (float)(tFrameElapsed / 1000000000.0);
			// Update timestep
			float delta = elapsed;

            if(time <= tIdleMinTimeIntervalScheduled) {
                if (delta > maxMeanTimeInterval)
                    delta = tLastStableInterval;
                else
                    tLastStableInterval = delta;
                // Average out frame time
                tFrameTime.addValue(delta);
                delta = tFrameTime.getMean();
            }


            // delta = Gdx.graphics.getDeltaTime();

			// Consider frame time adjustment
			if(delta < -tFrameTimeAdjustment)
				delta = tLastStableInterval;
			else
				delta += tFrameTimeAdjustment;
			tFrameTimeAdjustment = 0.0f;		// reset
			time += delta;
			
			// Calculate min time interval
			float currentMinTimeInterval = time > tIdleMinTimeIntervalScheduled ? idleMinTimeInterval : minTimeInterval;
			currentMinTimeInterval += (float)(Math.random() - 0.5f) * intervalRandomTime;


            // Prepare frame

            // Messages
            try {
                universe.processMessages();
            } catch (Throwable e) {
                if(Console.console == null || Console.universeRestartCode == null)
                    throw new RuntimeException("Universe failed on messages: " + universe + ": " + e.getMessage(), e);
                Sys.error(TAG, "Universe failed: " + universe, e);
                activate(dummyUniverse);
                Console.console.showRestartCode();
                Console.console.show();
            }

            // Calculate messages time
            time2 = System.nanoTime();
            statMessagesTime += time2 - time1;
            time1 = time2;

			// Live editor
            try {
                if(LiveEditor.editor != null)
                    LiveEditor.editor.refresh(false);
            } catch (Throwable e) {
                if(Console.console == null || Console.universeRestartCode == null)
                    throw new RuntimeException("Live editor failed: " + e.getMessage(), e);
                Sys.error(TAG, "Live editor failed", e);
                activate(dummyUniverse);
                LiveEditor.editor.clearSources();
                Console.console.showRestartCode();
                Console.console.show();
            }

            // Universe
            try {
                int renderHash = universe.processRender(delta);
				if(renderHash != lastRenderHash) {
					requestMaxFramerate(renderChangeMaxFramerateTime);
					lastRenderHash = renderHash;
				}
            } catch (Throwable e) {
				if(Console.console == null || Console.universeRestartCode == null)
					throw new RuntimeException("Universe failed on process: " + universe + ": " + e.getMessage(), e);
				Sys.error(TAG, "Universe failed: " + universe, e);
                activate(dummyUniverse);
				Console.console.showRestartCode();
				Console.console.show();
			}

            // Calculate universe time
            time2 = System.nanoTime();
            statUniverseTime += time2 - time1;
            time1 = time2;

			// Base streaming
			if(streamingDisabledThisFrame) {
				Processor.processor.updateFinish();
				streamingDisabledThisFrame = false;
			}
			else {
				for(int c = 0; c < streamingMinUpdates && Processor.processor.getRemainingTasks() > 0; c++)
					Processor.processor.updateSingle();
			}

            // Calculate base streaming time
            time2 = System.nanoTime();
            statBaseProcessorTime += time2 - time1;
            time1 = time2;

            // Render frame
			try {
				Renderer.renderer.render();
			} catch (Throwable e) {
				if(Console.console == null || Console.universeRestartCode == null)
					throw new RuntimeException("Renderer failed: " + Renderer.renderer + ": " + e.getMessage(), e);
				Sys.error(TAG, "Renderer failed: " + Renderer.renderer, e);
				if(Renderer.renderer.getClass() != Renderer.class)
					Renderer.renderer = new Renderer();
				else
					activate(dummyUniverse);
			}

			// Texture gc
			for(int c = 0; c < gcCyclesPerFrame; c++)
				GarbageCollector.performSingleGC(false);


            // Calculate renderer time
            time2 = System.nanoTime();
            statRendererTime += time2 - time1;
            time1 = time2;


			tFrameEnded = time2;
			elapsed = (float)((tFrameEnded - Sys.this.tFrameStarted) / 1000000000.0);
			float remainingMinTimeInterval = currentMinTimeInterval - elapsed;
			// Stream additional if can afford time
			if(Processor.processor.getRemainingTasks() > 0) {
				if(tStreamingInterval == -1) {
					// Measure optimal streaming interval
					tStreamingInterval = delta;
					currentFramesSkipped = 0;
					if(tStreamingInterval > streamingTimeInterval)
						tStreamingInterval = -1;		// cannot afford to stream additional right now
				}
				// Stream additional content
				float tAllowedStreamingInterval = tStreamingInterval > remainingMinTimeInterval ? tStreamingInterval : remainingMinTimeInterval;
				if(elapsed < tAllowedStreamingInterval) {
					do {
						if(!Processor.processor.updateSingle(true))
							break;		// finished streaming
						// Used up some time, either by streaming or yielding render thread, update time
						Thread.yield();
						time2 = tFrameEnded = System.nanoTime();
						elapsed = (float)((tFrameEnded - Sys.this.tFrameStarted) / 1000000000.0);
						remainingMinTimeInterval = currentMinTimeInterval - elapsed;
					} while(elapsed < tAllowedStreamingInterval);
				}
				else if(++currentFramesSkipped > streamingMaxSkippedFrames)
					tStreamingInterval = -1;
			}
			else
				tStreamingInterval = -1;
			if(remainingMinTimeInterval <= 0f) {
                if(currentMinTimeInterval <= 0 && !isContinuousRendering) {
                    Gdx.graphics.setContinuousRendering(true);
                    isContinuousRendering = true;
                }
                if(!isContinuousRendering)
                    Gdx.graphics.requestRendering();
            }
            else {
                if(currentMinTimeInterval > 0f && isContinuousRendering) {
                    Gdx.graphics.setContinuousRendering(false);
                    isContinuousRendering = false;
                }
                if(!isContinuousRendering)
                    timerThread.requestRendering(remainingMinTimeInterval);
            }

            Sys.this.tFrameStarted = tFrameStarted;

            // Calculate idle processor time
            statIdleProcessorTime += time2 - time1;
            statFrames++;
		}
		
		@Override
		public void resize(int width, int height) {
			if(width == 0 || height == 0)				// TODO: desktop alt tab problem ? could be lwjgl3 specific
				return;
			if(destroyed)
				return;		// Workaround for crash in certain conditions
			// Update screen metrics
			Sys.this.width = width;
			Sys.this.height = height;
			length = (float)height / (float)width;
			if(!created) {
				// Track rendering thread
				renderingThread = Thread.currentThread();
				// First time creating
				created();
				if(Fonts.fonts == null)
					throw new IllegalArgumentException("Fonts.fonts not created!");
				// Make sure renderer is created
				if(Renderer.renderer == null)
					throw new IllegalArgumentException("Renderer.renderer not created!");
				created = true;
			}
			else {
				// Inform universe
				length = universe.resize(width, height);
				// Inform renderer
				Renderer.renderer.resize(width, height);
				// Inform console
				if(Console.console != null)
					Console.console.refreshMetrics();
			}
			// Reset timing
			resetTimers();
		}
		
		@Override
		public void resume() {
			Sys.info(TAG, "Resumed");
			// Reset timing
			resetTimers();
			// Pause music
			Audio.resumeMusic();
			// Inform universe
			universe.resume();
			// Do not stream this frame
			if(streamingDisableOnFirstFrame)
				streamingDisabledThisFrame = true;
			tFrameStarted = System.nanoTime();
			requestMaxFramerate(inputMaxFramerateTime);
		}
		
		// Input multiplexer
		@Override
		public boolean keyDown(int key) {
			requestMaxFramerate(inputMaxFramerateTime);
			return universe.processInput(Entity.INPUT_KEY_DOWN, key, Character.MIN_VALUE, 0, 0, 0, 0, 0);
		}
		@Override
		public boolean keyTyped(char character) {
			requestMaxFramerate(inputMaxFramerateTime);
			return universe.processInput(Entity.INPUT_KEY_TYPED, 0, character, 0, 0, 0, 0, 0);
		}
		@Override
		public boolean keyUp(int key) {
			requestMaxFramerate(inputMaxFramerateTime);
			return universe.processInput(Entity.INPUT_KEY_UP, key, Character.MIN_VALUE, 0, 0, 0, 0, 0);
		}
		@Override
		public boolean scrolled(int amount) {
			requestMaxFramerate(inputMaxFramerateTime);
			return universe.processInput(Entity.INPUT_SCROLLED, 0, Character.MIN_VALUE, amount, 0, 0, 0, 0);
		}
		@Override
		public boolean touchDown(int xPos, int yPos, int pointer, int button) {
            requestMaxFramerate(inputMaxFramerateTime);
			float x = ((float)xPos / (float)width) - 0.5f;
			float y = (length - ((float)yPos / (float)height * length)) - (length / 2.0f);
			return universe.processInput(Entity.INPUT_TOUCH_DOWN, 0, Character.MIN_VALUE, 0, pointer, x, y, button);
		}
		@Override
		public boolean touchDragged(int xPos, int yPos, int pointer) {
			requestMaxFramerate(inputMaxFramerateTime);
			float x = ((float)xPos / (float)width) - 0.5f;
			float y = (length - ((float)yPos / (float)height * length)) - (length / 2.0f);
			return universe.processInput(Entity.INPUT_TOUCH_DRAGGED, 0, Character.MIN_VALUE, 0, pointer, x, y, 0);
		}
		@Override
		public boolean mouseMoved(int xPos, int yPos) {
			return false;		//  Ignore cursor movements (desktop mouse, not touchscreen)
		}
		@Override
		public boolean touchUp(int xPos, int yPos, int pointer, int button) {
			requestMaxFramerate(inputMaxFramerateTime);
			float x = ((float)xPos / (float)width) - 0.5f;
			float y = (length - ((float)yPos / (float)height * length)) - (length / 2.0f);
			return universe.processInput(Entity.INPUT_TOUCH_UP, 0, Character.MIN_VALUE, 0, pointer, x, y, button);
		}
	}

	public static Sys system = null;
	public static SpriteBatch sb = null;
	static float time = 0.0f;		// time
	
	public static int defaultFrameTimeSamples = 5;
	
	public static final float getTime() { return time; }
	
	// Active universe
	final Universe dummyUniverse = new DummyUniverse();
	Universe universe = dummyUniverse;		// Can never be null
	boolean created = false;
	boolean destroyed = false;
	TimerThread timerThread = null;
	Thread renderingThread = null;
	// Screen metrics data
	float length = 1.0f;
	int width = 100;
	int height = 100;
	// libGDX
	public final GDXApplicationListener applicationListener = new GDXApplicationListener();
	// Time
	public final WindowedMean tFrameTime;
	public float maxMeanTimeInterval = 1.0f / 10.0f;		// 10fps
	public float optimalMeanTimeInterval = 1.0f / 60.0f;	// 60fps
	float tLastStableInterval = 1.0f / 60.0f;
	public float streamingTimeInterval = 1.0f / 25.0f;
	public float minTimeInterval = 0.0f;			// 1.0f / 60.0f
	public float idleMinTimeInterval = 0.0f;
	public float intervalRandomTime = 0f; // TODO what is the purpose of this 1.0f / 60f;
	public float inputMaxFramerateTime = Float.MAX_VALUE;
	public float renderChangeMaxFramerateTime = Float.MAX_VALUE;
	int lastRenderHash = 0;
	float tIdleMinTimeIntervalScheduled = -1;		
	float tStreamingInterval = -1;
	public int streamingMaxSkippedFrames = 25;
	public int streamingMinUpdates = 5;
	int currentFramesSkipped = 0;
	long tFrameStarted = 0;

    boolean isContinuousRendering = true;
	public float tFrameTimeAdjustment = 0.0f;
	public boolean streamingDisableOnFirstFrame = true;
	public boolean streamingDisabledThisFrame = false;
    // Stats
    public long statFrames = 0;
    public long statMessagesTime = 0;
    public long statUniverseTime = 0;
    public long statBaseProcessorTime = 0;
    public long statRendererTime = 0;
    public long statForcedProcessorTime = 0;
    public long statIdleProcessorTime = 0;
    public long statTotalTime = 0;

	// Garbage Collector
	public int gcCyclesPerFrame = 5;
	
	public void requestMaxFramerate(float tMaxFramerateTime) {
        if(!isContinuousRendering) {
            Gdx.graphics.setContinuousRendering(true);
            isContinuousRendering = true;
			Gdx.graphics.requestRendering();
        }
		tIdleMinTimeIntervalScheduled = time + tMaxFramerateTime;
	}
	
	public Sys() {
		 this.tFrameTime = new WindowedMean(defaultFrameTimeSamples);
	}

	public int getWidth() {return width;}
	public int getHeight() {return height;}
	public float getLength() {return length;}

    public float getInputX(int pointer) {
        return ((float)Gdx.input.getX(pointer) / (float)width) - 0.5f;

    }

    public float getInputY(int pointer) {
        return (length - ((float)Gdx.input.getY(pointer) / (float)height * length)) - (length / 2.0f);
    }

	public Thread getRenderingThread() { return renderingThread; }
	
	public void resetTimers() {
		tLastStableInterval = optimalMeanTimeInterval;
		tFrameTime.clear();
		while(!tFrameTime.hasEnoughData())
			tFrameTime.addValue(tLastStableInterval);
		tFrameStarted = System.nanoTime();
		tStreamingInterval = -1;
		tFrameTimeAdjustment = 0.0f;
		lastRenderHash = 0;
	}
	
	// Dummy universe that ignores all callbacks 
	static class DummyUniverse extends Universe2D {
		public DummyUniverse() {
			super(1f, -1);		// TODO
		}
		
		@Override
		protected void recreate(Universe v) {
			if(Console.console != null) {
				Console.console.attach(v);
				Console.console.show();
			}
		}
		
		@Override
		protected boolean input(Universe v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
			if(inputType == INPUT_KEY_UP && key == Input.Keys.BACK)
				Gdx.app.exit();
			return true;
		}
	}
	
	public Universe getUniverse() {
		return universe; 
	}
	
	public void activate(final Universe universe) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				if(Sys.this.universe == universe || destroyed)
					return;			// activating same universe or destroyed
				// Clear all messages of existing universe
				Sys.this.universe.processMessages();
				Sys.this.universe.stopped();
				Entity.releaseIterator.iterate(Sys.this.universe, Sys.this.universe, false);
				// If new universe is null, use dummy universe instead
				Universe u = universe;
				if(u == null)
					u = dummyUniverse;
				// Attach console if exists
				if(Console.console != null)
					Console.console.attach(u);
				// Replace universe
				Sys.this.universe = u;
				// Find length parameter determined by universe
				length = u.resize(width, height);
				// Make sure universe is loaded
				Entity.recreateIterator.iterate(u, u, false);
				u.processMessages();
				// Inform resume event
				u.resume();
			}
		};
		if(Sys.this.universe == dummyUniverse)
			r.run();
		else
			Gdx.app.postRunnable(r);
	};
	
	/**
	 * Deactivates the current universe. Equivalent to activate(null).
	 */
	public void deactivate() {
		activate(null);
	}

	/**
	 * Deactivates the current universe, will only work if universe is the current active universe. 
	 * @param universe, {@link Universe} the current universe
	 * @return boolean, true if specified universe was the current universe and is deactivated, false if otherwise.
	 */
	public boolean deactivate(Universe universe) {
		if(universe == this.universe) {
			activate(null);
			return true;
		}
		return false;
	}
	
	// Messaging and global flow control
	public void log(String source, String text) {
		Gdx.app.log(source, text);
	}
	public void log(String source, String text, Throwable exception) {
		Gdx.app.log(source, text, exception);
	}
	public void logDebug(String source, String text) {
		Gdx.app.debug(source, text);
	}
	public void logDebug(String source, String text, Throwable exception) {
		Gdx.app.debug(source, text, exception);
	}
	public void logError(String source, String text) {
		Gdx.app.error(source, text);
	}
	public void logError(String source, String text, Throwable exception) {
		Gdx.app.error(source, text, exception);
	}

	// User implementation
	protected void created() {
		
	}
	protected void destroyed() {
		
	}
}
