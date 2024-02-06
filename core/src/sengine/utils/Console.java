package sengine.utils;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

import bsh.Interpreter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;

import sengine.File;
import sengine.Sys;
import sengine.Universe;
import sengine.graphics2d.Font;
import sengine.graphics2d.Fonts;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.materials.ColoredMaterial;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.ui.UIElement.Metrics;

public class Console extends Menu<Universe> implements OnClick<Universe> {
	static final String TAG = "Console";
	
	static final Pattern newlinePattern = Pattern.compile("[\\r\\n]");
	
	public static int maxInput = 10;
	public static Console console = null;
	public static String interpreterDefaults = null;
	public static String universeRestartCode = null;
	public static float tPreviewTime = 1f;
	public static float tPreviewMinTime = 0.05f;

	public static float tStatsPollingTime = 1f;

    public static int cpuBgColor = 0x333333ff;
    public static int cpuCombinedColor = 0x00ff00ff;
    public static int cpuSystemColor = 0xff0000ff;
    public static float cpuStatSize = 0.12f;
    public static float cpuStatLength = 4f / 100f;


    public static final String colorCommand = "Console.command"; // "#8888ff";
    public static final String colorInfo = "Console.info"; // "#00ff00";
    public static final String colorDebug = "Console.debug"; // "#ff9933";
    public static final String colorError = "Console.error"; // "#ff0000";
    public static final String colorPastInput = "Console.pastInput";        // #aaaaaa
    public static final String colorInput = "Console.input";    // ffffff
    public static final String colorStatsFull = "Console.statsFull";
    public static final String colorStatsPreview = "Console.statsPreview";


    final Font font;
    final float fontSize;
	final TextBox textBox;
	final TextBox previewTextBox;
	final UIElement.Group bg;
	final Clickable bgSprite;
	final Sprite bgMat;

    final StaticSprite cpuView;
    final StaticSprite cpuCombinedView;
    final StaticSprite cpuSystemView;
	
	final Array<String> lines = new Array<String>(String.class);
	final Array<String> input = new Array<String>(String.class);
	
	int maxLines = 10;
	final StringBuilder text = new StringBuilder();
    String text1 = null;
    String text2 = null;
    final StringBuilder previewText = new StringBuilder();
    int upIndex = 0;
	float tPreviewEndScheduled = Float.MAX_VALUE;
	float tPreviewNextScheduled = Float.MAX_VALUE;
	boolean isPreviewShowing = false;
    boolean isPerfomanceOnly = false;
	int previewIndex = -1;
	float tStatsScheduled = Float.MAX_VALUE;
    final StringBuilder statLine = new StringBuilder();

	private long lastCpuTimestamp;
	private long lastCpuUsageTime;
    private long lastCpuUserTime;
    private boolean cpuUsageAvailable = true;

	public Interpreter interpreter = null;

    public void replaceInput(String statements) {
        lines.items[lines.size - 1] = statements;
        refresh();
    }

    public boolean showRestartCode() {
        if(universeRestartCode == null)
            return false;
        replaceInput(universeRestartCode);
        return true;
    }

    public Object eval(String statements) {
        // Exec
        try {
            return interpreter.eval(new StringReader(statements));
        } catch (Throwable t) {
            Sys.info(TAG, "Unable to eval statements " + statements, t);
            return null;
        }
    }

    public Object evalFile(String filename) {
        return evalFile(filename, true);
    }

    public Object evalFile(String filename, boolean enforceExistence) {
        String content;
        try {
            content = File.read(filename, enforceExistence);
            if(content == null) {
                Sys.info(TAG, "Unable to find file " + filename);
                return null;
            }
        } catch (Throwable t) {
            Sys.info(TAG, "Unable to read file " + filename, t);
            return null;
        }
        // Exec
        try {
            return interpreter.eval(new StringReader(content));
        } catch (Throwable t) {
            Sys.info(TAG, "Unable to eval " + filename, t);
            return null;
        }
    }
	
	public void show() {
		if(isPreviewShowing) {
			previewTextBox.detach();
			isPreviewShowing = false;
		}
		bg.attach();
	}
	
	public void showPreview(boolean performanceOnly) {
		bg.detach();
		previewTextBox.attach();
		isPreviewShowing = true;
        isPerfomanceOnly = performanceOnly;
        if(performanceOnly)
            tPreviewEndScheduled = -1;
	}
	
	public void hide() {
		bg.detach();
		previewTextBox.detach();
		isPreviewShowing = false;
		tPreviewEndScheduled = Float.MAX_VALUE;
	}
	
	public void reset() {
		interpreter = new Interpreter(new StringReader(""), System.out, System.err, true, null, File.defaultInterpreter, "Console");
		interpreter.setClassLoader(Console.class.getClassLoader());         // to work with codeblob
		try {
			interpreter.set("console", this);
			if(interpreterDefaults != null)
				interpreter.eval(interpreterDefaults);
		} catch (Throwable e) {
			e.printStackTrace();		// ignored
		}
		clear();
	}
	
	public void clear() {
		lines.clear();
		lines.add("");
        text1 = null;
        text2 = null;
	}
	
	public void refresh() {
		text.setLength(0);

		if(bg.isAttached())
			previewIndex = lines.size;

		if(lines.size > maxLines) {
			int removed = lines.size - maxLines;
			lines.removeRange(0, removed);
			previewIndex -= removed;
		}
		
		int totalLines = lines.size > maxLines ? maxLines : lines.size;
		
		for(int c = 0; c < totalLines; c++) {
			String line = lines.items[c];
			if(c == (lines.size - 1))
				line = Fonts.escapeMarkup(line);
			if(c != (totalLines - 1))
				text.append("[").append(colorPastInput).append("]").append(line).append("[]\n");
			else {
				text.append("[").append(colorStatsFull).append("]").append(statLine).append("[]\n");
				text.append("[").append(colorInput).append("]").append(line).append("[]");
            }
		}

		// Update display texts
        String status;
        if(lines.size > maxLines)
            status = "...";
        else if(lines.size > 0 && lines.peek().isEmpty())
            status = "Press '~' to close or start typing...";
        else
            status = "|";
        String newText1 = text + status;
        if(text1 == null || !text1.equals(newText1)) {
            text1 = newText1;
            text2 = text + " ";
        }

        int lastItem = lines.size - 2;
        if(!isPerfomanceOnly && isPreviewShowing && lines.size >= 2 && previewIndex < lastItem) {
			if(previewIndex < 0)
				previewIndex = 0;
			else if(previewIndex > lastItem)
				previewIndex = lastItem;
			if(previewIndex < lastItem) {
				tPreviewNextScheduled = getRenderTime() + tPreviewMinTime;
				tPreviewEndScheduled = Float.MAX_VALUE;
			}
			else {
				tPreviewNextScheduled = Float.MAX_VALUE;
				tPreviewEndScheduled = getRenderTime() + tPreviewTime;
			}
            previewText.setLength(0);
            previewText.append("[").append(colorStatsPreview).append("]").append(statLine).append("[]\n").append(lines.items[previewIndex]);
			previewTextBox.text().text(previewText.toString());
		}

        if(getRenderTime() > tPreviewEndScheduled) {
            previewText.setLength(0);
            previewText.append("[").append(colorStatsPreview).append("]").append(statLine).append("[]");
            previewTextBox.text().text(previewText.toString());
        }
    }

    public void print(Object o) {
        print(o, null, false);
    }


    public void print(Object o, String color) {
		print(o, color, false);
	}
	
	public String simpleException(Throwable ex) {
		if(ex == null)
			return "";
		Throwable e = ex;
		String s = "";
		while(e != null) {
			String message = e.getMessage();
			if(message != null && message.length() > 0)
				s += "\n(" + e.getClass().getSimpleName() + ") " + e.getMessage();
			e = e.getCause();
		}
		if(s.length() == 0) {
			StringWriter writer = new StringWriter();
			ex.printStackTrace(new PrintWriter(writer));
			s = writer.toString().replace("\r\n", "\n");
		}
		return s;
	}
	public void info(String source, String text) {
		info(source, text, null);
	}
	public void info(String source, String text, Throwable exception) {
		source = Fonts.escapeMarkup(source);
		text = Fonts.escapeMarkup(text);
		print(source + ": " + text + simpleException(exception), colorInfo);
	}
	public void debug(String source, String text) {
		debug(source, text, null);
	}
	public void debug(String source, String text, Throwable exception) {
		source = Fonts.escapeMarkup(source);
		text = Fonts.escapeMarkup(text);
		print(source + ": " + text + simpleException(exception), colorDebug);
	}
	public void error(String source, String text) {
		error(source, text, null);
	}
	public void error(String source, String text, Throwable exception) {
		source = Fonts.escapeMarkup(source);
		text = Fonts.escapeMarkup(text);
		print(source + ": " + text + simpleException(exception), colorError);
	}
	
	boolean isPrinting = false;
	
	public synchronized void print(Object o, String color, boolean printObjectContents) {
		if(isPrinting)
			return;		// recursive call or not yet set up
		isPrinting = true;
		try {
			String s;
			if(o == null || !printObjectContents)
				s = String.valueOf(o);
			else {
				Field[] fields = o.getClass().getDeclaredFields();
				s = o.toString() + " { ";
				for(int c = 0; c < fields.length; c++) {
					Field field = fields[c];
					try {
						if(!field.isAccessible())
							field.setAccessible(true);
						int modifiers = field.getModifiers();
						String fieldInfo = Modifier.isStatic(modifiers) ? "static " : "";
						fieldInfo += field.getType().getSimpleName();
						s += "\n    " + fieldInfo + " " + field.getName() + " : " + field.get(o);
					} catch (Throwable e) {
						continue;			// failed
					}
				}
				s += "\n";
				
				Method[] methods = o.getClass().getMethods();
				for(int c = 0; c < methods.length; c++) {
					Method method = methods[c];
					try {
						if(!method.isAccessible())
							method.setAccessible(true);
						int modifiers = method.getModifiers();
						String fieldInfo = Modifier.isStatic(modifiers) ? "static " : "";
						fieldInfo += method.getReturnType().getSimpleName();
						Class<?>[] params = method.getParameterTypes();
						String paramTypes = "";
						for(int p = 0; p < params.length; p++)
							paramTypes += p == 0 ? params[p].getSimpleName() : (", " + params[p].getSimpleName());
						s += "\n    " + fieldInfo + " " + method.getName() + "(" + paramTypes + ")";
					} catch (Throwable e) {
						continue;			// failed
					}
				}
				s += "\n}";
			}
			s = Fonts.escapeMarkup(s);
			// Split lines
			s = font.wrap(s, textBox.text().wrapChars);
			String[] newLines = newlinePattern.split(s);
            if(color != null) {
                for (int c = 0; c < newLines.length; c++) {
                    newLines[c] = "[" + color + "]" + newLines[c] + "[]";
                }
            }
			String end = lines.pop();
			lines.addAll(newLines);
			lines.add(end);
			refresh();
		} finally {
			isPrinting = false;
		}
	}
	
	public Console(Sprite bgMat, Font font, float fontSize, int target) {
		this.font = font;
        this.fontSize = fontSize;

        // Register colors
        font.color(colorCommand, 0x8888ffff);
        font.color(colorInfo, 0x00ff00ff);
        font.color(colorDebug, 0xff9933ff);
        font.color(colorError, 0xff0000ff);
        font.color(colorPastInput, 0xaaaaaaff);
        font.color(colorInput, 0xffffffff);
        font.color(colorStatsFull, 0x00ffffff);
        font.color(colorStatsPreview, 0x00ffffff);

		this.bgMat = bgMat;

		this.bg = new UIElement.Group().viewport(viewport);

		this.bgSprite = new Clickable()
                .viewport(bg)
                .visuals(bgMat, target)
                .attach();
		
		this.textBox = new TextBox()
                .viewport(bg)
                .metrics(new Metrics().scale(0.97f))
				.text(new Text().font(font).bottomLeft().target(target).length(0))
                .attach();
		
		this.previewTextBox = new TextBox()
                .viewport(viewport)
                .metrics(new Metrics().scale(0.97f))
				.text(new Text().font(font).topLeft().target(target))
                ;

        Sprite sprite = new Sprite(cpuStatLength, new ColoredMaterial());
        ColorAttribute.of(sprite).set(cpuBgColor);
        this.cpuView = new StaticSprite()
                .viewport(viewport)
                .metrics(new Metrics().scale(cpuStatSize).anchorRight().anchorTop())
                .visual(sprite, target)
                ;

        sprite = sprite.instantiate();
        ColorAttribute.of(sprite).set(cpuCombinedColor);
        this.cpuCombinedView = new StaticSprite()
                .viewport(cpuView)
                .metrics(new Metrics().anchorRight())
                .visual(sprite, target)
                .attach();

        sprite = sprite.instantiate();
        ColorAttribute.of(sprite).set(cpuSystemColor);
        this.cpuSystemView = new StaticSprite()
                .viewport(cpuView)
                .metrics(new Metrics().anchorRight())
                .visual(sprite, target)
                .attach();

        statLine.append("...");
		reset();
	}
	
	@Override
	protected void recreate(Universe v) {
		super.recreate(v);

        try {
            ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
            if(tmx.isCurrentThreadCpuTimeSupported()) {
                lastCpuTimestamp = System.nanoTime();
                lastCpuUsageTime = tmx.getCurrentThreadCpuTime();
                lastCpuUserTime = tmx.getCurrentThreadUserTime();
                cpuUsageAvailable = true;
                cpuView.attach();
            }
            else
                cpuUsageAvailable = false;
        } catch (Throwable e) {
            Sys.error(TAG, "Cpu usage unavailable", e);
            cpuUsageAvailable = false;
        }

        refreshMetrics();

		tPreviewNextScheduled = 0;
		tStatsScheduled = 0;
	}

	public void refreshMetrics() {
        int width = Sys.system.getWidth();
        int height = Sys.system.getHeight();
        float length = (float)height / (float)width;

        float wrapChars = width / fontSize;

        bg.metrics(bg.metrics).length(length);

        textBox.text().position(0, 0, 1f, length, wrapChars);
        previewTextBox.text().position(0, 0, 1f, length, wrapChars);

        maxLines = font.getNumLines(length, wrapChars) - 1;      // reserve one line for stats

        // Clear all
        clear();
    }
	
	@Override
	protected void render(Universe v, float r, float renderTime) {
		super.render(v, r, renderTime);


		if(renderTime > tStatsScheduled) {
			tStatsScheduled = renderTime + tStatsPollingTime;

            long framesPolled = Sys.system.statFrames;

            double granularity = 1.0 / 1000000.0;

            long messagesTime = Math.round((Sys.system.statMessagesTime * granularity) / framesPolled);
            long universeTime = Math.round((Sys.system.statUniverseTime * granularity) / framesPolled);
            long rendererTime = Math.round((Sys.system.statRendererTime * granularity) / framesPolled);
            long baseProcessorTime = Math.round((Sys.system.statBaseProcessorTime * granularity) / framesPolled);
            long forcedProcessorTime = Math.round((Sys.system.statForcedProcessorTime * granularity) / framesPolled);
            long idleProcessorTime = Math.round((Sys.system.statIdleProcessorTime * granularity) / framesPolled);
            long statTotalTime = Math.round((Sys.system.statTotalTime * granularity) / framesPolled);
            // long statSwapTime = statTotalTime - messagesTime - universeTime - rendererTime - baseProcessorTime - forcedProcessorTime - idleProcessorTime;

            statLine.setLength(0);

            if(statTotalTime > 0 && framesPolled > 0) {

                if(cpuUsageAvailable) {
                    ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
                    long cpuTimestamp = System.nanoTime();
                    long cpuUsageTime = tmx.getCurrentThreadCpuTime();
                    long cpuUserTime = tmx.getCurrentThreadUserTime();

                    long elapsedTime = cpuTimestamp - lastCpuTimestamp;
                    long usageTime = cpuUsageTime - lastCpuUsageTime;
                    long userTime = cpuUserTime - lastCpuUserTime;

                    float usageRatio = (float)((double)usageTime / (double)elapsedTime);
                    float userRatio = (float)((double)userTime / (double)elapsedTime);


                    if(userRatio > usageRatio)
                        userRatio = usageRatio;
                    float systemRatio = usageRatio - userRatio;

                    cpuCombinedView.metrics.scaleX = usageRatio;
                    cpuSystemView.metrics.scaleX = systemRatio;

                    lastCpuTimestamp = cpuTimestamp;
                    lastCpuUsageTime = cpuUsageTime;
                }

                boolean hasTime = false;
                if (messagesTime > 0) {
                    statLine.append(messagesTime).append("m");
                    hasTime = true;
                }
                if (universeTime > 0) {
                    if (hasTime)
                        statLine.append(" + ");
                    statLine.append(universeTime).append("u");
                    hasTime = true;
                }
                if (rendererTime > 0) {
                    if (hasTime)
                        statLine.append(" + ");
                    statLine.append(rendererTime).append("r");
                    hasTime = true;
                }
                if ((baseProcessorTime + forcedProcessorTime + idleProcessorTime) > 0) {
                    // There is some processor time involved
                    if (hasTime)
                        statLine.append(" + ");
                    statLine.append("(");
                    boolean processorAppended = false;
                    if (baseProcessorTime > 0) {
                        statLine.append(baseProcessorTime).append("bp");
                        processorAppended = true;
                    }
                    if (forcedProcessorTime > 0) {
                        if (processorAppended)
                            statLine.append(" + ");
                        statLine.append(forcedProcessorTime).append("fp");
                        processorAppended = true;
                    }
                    if (idleProcessorTime > 0) {
                        if (processorAppended)
                            statLine.append(" + ");
                        statLine.append(idleProcessorTime).append("ip");
                    }
                    statLine.append(")");
                    hasTime = true;
                }

                if (hasTime)
                    statLine.append(" = ");
                statLine.append(framesPolled).append("fps");

                refresh();
            }
            else
                statLine.append("...");


            // Clear
            Sys.system.statFrames = 0;
            Sys.system.statMessagesTime = 0;
            Sys.system.statUniverseTime = 0;
            Sys.system.statBaseProcessorTime = 0;
            Sys.system.statRendererTime = 0;
            Sys.system.statForcedProcessorTime = 0;
            Sys.system.statIdleProcessorTime = 0;
            Sys.system.statTotalTime = 0;

        }


        if(bg.isAttached()) {
            int phase = (int) (renderTime / 0.5f);
            textBox.text().text(phase % 2 == 1 ? text1 : text2);
        }
        else if(!isPerfomanceOnly && previewTextBox.isAttached()) {
            if(renderTime > tPreviewNextScheduled) {
                if(previewIndex < 0)
                    previewIndex = 0;
                else
                    previewIndex++;
                int lastItem = lines.size - 2;
                if(previewIndex > lastItem)
                    previewIndex = lastItem;
                if(previewIndex < lastItem) {
                    tPreviewNextScheduled = getRenderTime() + tPreviewMinTime;
                    tPreviewEndScheduled = Float.MAX_VALUE;
                }
                else {
                    tPreviewNextScheduled = Float.MAX_VALUE;
                    tPreviewEndScheduled = getRenderTime() + tPreviewTime;
                }
                if(previewIndex >= 0) {
                    String line = "[" + colorStatsPreview + "]" + statLine + "[]\n" + lines.items[previewIndex];
                    if(previewTextBox.text().text == null || !previewTextBox.text().text.equals(line))
                        previewTextBox.text().text(line);
                }
            }
        }
		
		// Reattach to front
		attach(v);
	}
	
	@Override
	protected void release(Universe v) {
		super.release(v);
		
		// Reattach
		attach(v);
	}

	private void evaluateCurrentCommand() {
		upIndex = 0;
		// Enter, evaluate line
		String command = lines.items[lines.size - 1];
		if (command.length() > 0) {
			input.add(command);
			lines.items[lines.size - 1] = Fonts.escapeMarkup(lines.items[lines.size - 1]);
			if (input.size > maxInput)
				input.removeRange(0, input.size - maxInput - 1);
			// New line
			lines.add("");
			int currentLines = lines.size;
			boolean isDeepPrint;
			if (command.charAt(0) == '?') {
				command = command.substring(1);
				isDeepPrint = true;
			} else
				isDeepPrint = false;
			try {
				Object result = interpreter.eval(command);
				if (result != null)
					print(result, colorCommand, isDeepPrint);
			} catch (Throwable e) {
				Sys.error(TAG, "Command failed: " + command, e);
			}
			int addedLines = lines.size - currentLines;
			if (lines.size > maxLines && addedLines < maxLines) {
				int removedLines = lines.size - maxLines;
				lines.removeRange(0, removedLines);
				previewIndex -= removedLines;
			}
			attach(Sys.system.getUniverse());
		}
	}
	
	@Override
	protected boolean input(Universe v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
        // Special commands
        if(inputType == INPUT_KEY_UP && key == Input.Keys.F1) {
            // Special command to show restart code
            showRestartCode();
            evaluateCurrentCommand();
            return true;
        }
        if(inputType == INPUT_KEY_UP && key == Input.Keys.F2) {
            // Special command to show restart code
            Fonts.debugFonts = !Fonts.debugFonts;
            return true;
        }
        if(inputType == INPUT_KEY_TYPED && character == '`') {
			if(bg.isAttached()) {
				bg.detach();
				if(tPreviewEndScheduled != Float.MAX_VALUE || tPreviewNextScheduled != Float.MAX_VALUE)
					previewTextBox.attach();
				isPreviewShowing = true;
			}
			else if(isPreviewShowing) {
				previewTextBox.detach();
				isPreviewShowing = false;
			} else
				bg.attach();
			return true;
		}
		else if(!bg.isAttached()) {
            return false;
		}
		else if(lines.size > maxLines) {
			if(inputType == INPUT_KEY_TYPED) {
				lines.removeIndex(0);
				previewIndex--;
				refresh();
				return true;
			}
			else
				return false;
		}
        else if(key == Input.Keys.UP) {
			if(inputType == INPUT_KEY_UP) {
				upIndex++;
				if(upIndex > input.size)
					upIndex = input.size;
				if(input.size != 0)
					lines.items[lines.size - 1] = input.items[input.size - upIndex];
			}
		}
		else if(key == Input.Keys.DOWN) {
			if(inputType == INPUT_KEY_UP) {
				upIndex--;
				if(upIndex < 1)
					upIndex = 1;
				if(upIndex > input.size)
					upIndex = input.size;
				if(input.size != 0)
					lines.items[lines.size - 1] = input.items[input.size - upIndex];
			}
		}
		else if(inputType != INPUT_KEY_TYPED)
			return false;
		else if(character == 13 || character == 10) {          // line break
			if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
				lines.items[lines.size - 1] += '\n';
			}
            else {
                evaluateCurrentCommand();
            }
		}
		else if(character == 8) {           // backspace
			// Backspace
			String command = lines.items[lines.size - 1];
			if(command.length() > 0)
				lines.items[lines.size - 1] = command.substring(0, command.length() - 1);
		}
		else if(!Character.isISOControl(character)) {
            lines.items[lines.size - 1] = font.wrap(lines.items[lines.size - 1] + character, textBox.text().wrapChars);
        }
		
		// Refresh text
		refresh();
		
		return true;
	}

    @Override
    public void onClick(Universe v, UIElement<?> view, int button) {
        if(view == bgSprite) {
            // Simulate enter
            input(v, INPUT_KEY_TYPED, 0, (char)13, 0, 0, 0, 0, 0);
            showPreview(true);
            return;
        }
    }
}
