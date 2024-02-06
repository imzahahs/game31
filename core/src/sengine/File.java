package sengine;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ObjectMap;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import bsh.BshMethod;
import bsh.CallStack;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.NameSpace;
import sengine.kryo.BoundingBoxKryoSerializer;
import sengine.kryo.PixmapKryoSerializer;
import sengine.mass.Mass;
import sengine.mass.MassFile;
import sengine.utils.MemoryFileHandle;
import sengine.utils.WeakCache;

public class File {
	private static final String TAG = "File";

    public static final String VAR_FILENAME = "FILENAME";

	public static final String UTF8_BOM = "\uFEFF";


    public interface CustomFileSource {
        FileHandle open(String path);
    }

	public static boolean skipInternalFileCheck = true;
    public static boolean showMainThreadWarning = false;

    public static CustomFileSource customFileSource = null;

	public static String removeUTF8BOM(String s) {
		if (s.startsWith(UTF8_BOM)) {
			s = s.substring(1);
		}
		return s;
	}

	public static String changeExtension(String path, String extension) {
		String[] split = splitExtension(path);
		return split[0] + "." + extension; 
	}
	
	public static String[] splitExtension(String path) {
		return splitExtension(path, 1);
	}

	
	public static String[] splitExtension(String path, int numExtensions) {
		String[] split = new String[2];
		int lastDot = path.lastIndexOf(".");
		numExtensions--;
		if(lastDot > 0) {
			while(numExtensions-- > 0) {
				lastDot = path.lastIndexOf(".", lastDot - 1);
				if(lastDot == -1)
					break;
			}
		}
		if(lastDot != -1 && lastDot > 0 && path.length() > (lastDot + 1)) {
			// Path has both name and extension 
			split[0] = path.substring(0, lastDot);
			split[1] = path.substring(lastDot+1, path.length());
		}
		else {
			split[0] = path;	// Else splitting not possible
		}
		return split;
	}
	
	public static String getExtension(String path) {
		return splitExtension(path)[1];
	}

	// Globals
	public static boolean allowExternalOverride = false;
    public static boolean externalOverrideIsAbsolute = false;
	public static String externalOverridePath = null;
	public static FileHandle optimizedCacheDir = null;
	public static boolean allowCopyToCache = true;
	public static boolean persistHints = false;
	public static final String hintsExtension = ".hints";
	
	// Used to share class loaders
	public static final Interpreter defaultInterpreter = new Interpreter(new StringReader(""), System.out, System.err, false, null, null, "File.defaultInterpreter");
	// File system
	private static final ArrayList<MassFile> loadedFs = new ArrayList<MassFile>();
	private static final ObjectMap<String, MassFile> loadedFsLookup = new ObjectMap<String, MassFile>();
	private static final WeakCache<String, Object> hints = new WeakCache<String, Object>();
	// Scripts
	private static final WeakCache<String, Interpreter> runningScripts = new WeakCache<String, Interpreter>();
	private static final ArrayList<String> currentScripts = new ArrayList<String>();
	
	public static void resetSerializers() {
		// Register basic serializers
		Mass.registerSerializer(BoundingBox.class, new BoundingBoxKryoSerializer());
		Mass.registerSerializer(Pixmap.class, new PixmapKryoSerializer());
	}
	
	static void reset() {
		synchronized(hints) {
			// Kryo
			resetSerializers();
			
			// File system
			hints.clear();
			loadedFs.clear();
			
			// Running scripts
			runningScripts.clear();
			currentScripts.clear();
		}
	}
	
	static {
		reset();
	}
	
	public static String getHintsName(Object o) {
		synchronized(hints) {
			return hints.findKey(o);
		}
	}

	// Hints
	public static <T> T peekHints(String filename) {
		filename = filename.trim();
		while(filename.startsWith("/"))
			filename = filename.substring(1);
		Object o;
		synchronized(hints) {
			o = hints.get(filename);
			if(o != null)
				return (T)o;
			return null;			// not loaded yet
		}
	}

	public static <T> T getHints(String filename) {
		return getHints(filename, false, true);
	}

	public static <T> T getHints(String filename, boolean enforceExistence) {
		return getHints(filename, false, enforceExistence);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getHints(String filename, boolean doNotCompile, boolean enforceExistence) {
		// Trim optional '/';
		filename = filename.trim();
		while(filename.startsWith("/"))
			filename = filename.substring(1);
		Object o;
		synchronized(hints) {
			o = hints.get(filename);
			if(o != null)
				return (T)o;
			// Hints not found in default fs, find in loaded fs
			for(int c = 0; c < loadedFs.size(); c++) {
				MassFile l = loadedFs.get(c);
				o = l.get(filename);
				if(o != null) {
					// This MassFile has reference to specified hint, save back in global hints
					hints.put(filename, o);
					if(persistHints)
						hints.setStrongRef(o);
					return (T)o;
				}
			}
		}
		// Hints not found, load manually if allowed
		if(doNotCompile) {
			if(enforceExistence)
				throw new RuntimeException("Compiled hints not found: " + filename);
			else
				return null;
		}
		o = File.run(filename + hintsExtension, enforceExistence, false);
		if(o == null) {
			if(enforceExistence)
				throw new RuntimeException("Hints not found: " + filename);
			else
				return null;
		}
		// Save to cache
		synchronized(hints) {
			hints.put(filename, o);
			if(persistHints)
				hints.setStrongRef(o);
		}
		// Else hints found
		return (T)o;
	}
	
	public static void saveHints(String filename, Object hint) {
		// Trim optional '/';
		filename = filename.trim();
		while(filename.startsWith("/"))
			filename = filename.substring(1);
		// Build actual FS name
		synchronized(hints) {
			Object o = hints.put(filename, hint);
//			if(persistHints)		TODO for saving should not use weak cache?
				hints.setStrongRef(hint);
			if(o != null)
				Sys.info(TAG, "Overriding existing hints: " + filename + " " + o);
		}
	}
	
	public static boolean removeHints(String filename) {
		// Trim optional '/';
		filename = filename.trim();
		while(filename.startsWith("/"))
			filename = filename.substring(1);
		// Remove hints extension if exists
		if(filename.endsWith(hintsExtension))
			filename = filename.substring(0, filename.length() - hintsExtension.length());
		synchronized(hints) {
			boolean removed = hints.remove(filename) != null;
			for(int c = 0; c < loadedFs.size(); c++) {
				MassFile fs = loadedFs.get(c);
				fs.remove(filename);
			}
			return removed;
		}
	}
	
	public static boolean exists(String filename) {
		return File.open(filename, false) != null;
	}

	public static void loadFS(String filename, boolean selectiveDecoding) {
		synchronized(hints) {
			MassFile loaded = loadedFsLookup.get(filename);
			if(loaded != null)
				unloadFS(filename);
			// Parse FS
			MassFile fs = new MassFile();
			fs.load(filename);
			loadedFs.add(fs);		// added to loaded fs
			loadedFsLookup.put(filename, fs);
		}
	}

	public static void loadFS(String filename) {
		loadFS(filename, true);		// by default selective decoding
	}

	public static void unpackFS(String ... filenames) {
		for(int c = 0; c < filenames.length; c++) {
			String filename = filenames[c];
			if(exists(filename))
				loadFS(filename);
		}
	}


	public static void packFS(String ... paths) {
		int c = 0;
		while(c < paths.length) {
			String filename = paths[c];
			if((c + 1) < paths.length) {
				String prefix = paths[c + 1];

				// Save and unload, so that remaining can be compiled separately
				saveFS(filename, prefix);
				unloadFS(filename);

				c += 2;
			}
			else {
				// Last filename, save any remaining to this
				saveFS(filename, null);
				unloadFS(filename);
				c++;
			}
		}
		// Reload all fs
		while(c < paths.length) {
			String filename = paths[c];
			loadFS(filename);
			if((c + 1) < paths.length)
				c += 2;
			else
				c++;
		}
	}
	
	public static void flushFS() {
		synchronized(hints) {
			// Clear all MassFile
			hints.clear();
			loadedFs.clear();
			loadedFsLookup.clear();
		}
	}
	
	public static void unloadFS(String filename) {
		synchronized(hints) {
			MassFile mass = loadedFsLookup.remove(filename);
			if(mass != null) {
				loadedFs.remove(mass);
				String[] names = mass.names();
				for(int c = 0; c < names.length; c++)
					hints.remove(names[c]);
			}
		}
	}

	public static void saveFS(String filename, String prefix) {
		synchronized(hints) {
			try {
				HashMap<String, Object> map = new HashMap<>();
				// Accumulate hints
				for (int c = loadedFs.size() - 1; c >= 0; c--) {		// Load from last so that earlier filesystems can have overriding priority
					MassFile fs = loadedFs.get(c);
					String[] names = fs.names();
					for (int n = 0; n < names.length; n++) {
						String name = names[n];
						if (prefix == null || name.startsWith(prefix)) {
							Object hints;
							try {
								hints = getHints(name);
							} catch (Throwable e) {
								Sys.error(TAG, "Unable to decode object \"" + name + "\"", e);
								continue;           // Skip
							}
							map.put(name, hints);
						}
					}
				}
				for (Entry<String, Object> e : hints.entrySet()) {
					String name = e.getKey();
					if (prefix == null || name.startsWith(prefix))
						map.put(name, e.getValue());           // Loaded hints will always have final overriding priority
				}
				// Dump to mass
				MassFile mass = new MassFile();
				for(Entry<String, Object> e : map.entrySet()) {
					mass.add(e.getKey(), e.getValue());
				}
				mass.save(filename);
			}
			catch (Throwable e) {
				throw new RuntimeException("Failed to compile filesytem: " + filename, e);
			}
			loadFS(filename);
		}
	}
	
	// Standard IO and caches
	public static boolean isCacheAvailable() {
		return (allowExternalOverride && File.optimizedCacheDir != null);
	}
	
	public static FileHandle openCache(String filename) {
		return File.openCache(filename, true);		// default enforce
	}
	
	public static FileHandle openCache(String filename, boolean enforceExistence) {
		if(!isCacheAvailable()) {
			if(enforceExistence)
				throw new RuntimeException("Failed to open cache: " + filename);
			else
				return null;		// cache not possible
		}
		// Else open cache
		FileHandle f = File.optimizedCacheDir.child(filename);
		// If does not exist, mkdirs to it
		if(!f.exists())
			f.parent().mkdirs();
		return f;
	}
	
	public static FileHandle openExternal(String filename) {
        if(externalOverrideIsAbsolute)
            return Gdx.files.absolute(externalOverridePath + filename);
        else
            return Gdx.files.external(externalOverridePath + filename);
	}
	
	public static MemoryFileHandle openHintsFile(String filename) {
		return openHintsFile(filename, true);		
	}
	
	public static MemoryFileHandle openHintsFile(String filename, boolean enforceExistence) {
		MemoryFileHandle massFileHandle = getHints(filename, false);
		if(massFileHandle == null) {
			// Try to load from normal file
			FileHandle fileHandle = open(filename, enforceExistence);
			if(fileHandle == null)
				return null;		// only if enforceExistence == false, otherwise would have thrown exception
			// Copy to hints
			massFileHandle = new MemoryFileHandle(fileHandle);
			saveHints(filename, massFileHandle);
		}
		return massFileHandle;
	}

    public static String read(String filename) {
        return read(filename, true);
    }

    public static String read(String filename, boolean enforceExistence) {
		FileHandle fileHandle = open(filename, enforceExistence);
		if(fileHandle == null)
			return null;		// existence not enforced
		return removeUTF8BOM(fileHandle.readString("UTF-8"));			// remove bom
	}
	
	public static FileHandle open(String filename) {
		return File.open(filename, true, false);		// default enforce and do not copy to cache
	}

	public static FileHandle open(String filename, boolean enforceExistence) {
		return File.open(filename, enforceExistence, false);		// default do not copy to cache
	}

	public static FileHandle open(String filename, boolean enforceExistence, boolean copyToCache) {
		if(Gdx.app.getType() == Application.ApplicationType.iOS) {
			// For iOS, ogg is not supported, so open mp3 instead
			if(File.getExtension(filename).compareToIgnoreCase("ogg") == 0)
				filename = File.changeExtension(filename, "mp3");
		}
		// Load from cache first
		FileHandle fileHandle;
		// Else cache has no knowledge
		if(allowExternalOverride) {
			// Open cached first
			if(File.optimizedCacheDir != null) {
				fileHandle = File.optimizedCacheDir.child(filename);
				if(fileHandle.exists()) {
					if(showMainThreadWarning && Thread.currentThread() == Sys.system.getRenderingThread())
						Sys.debug(TAG, "I/O operation in rendering thread for: " + filename);
					return fileHandle;	// cache exists
				}
			}
			// Try to open external path
			fileHandle = openExternal(filename); // Gdx.files.external(externalOverridePath + filename);
			if(fileHandle.exists()) {
				// Copy to cache if necessary
				if(copyToCache && allowCopyToCache && File.optimizedCacheDir != null) {
					Sys.debug(TAG, "Copying file to cache: " + filename);
					fileHandle.copyTo(File.optimizedCacheDir.child(filename));	
				}
				if(showMainThreadWarning && Thread.currentThread() == Sys.system.getRenderingThread())
					Sys.debug(TAG, "I/O operation in rendering thread for: " + filename);
				return fileHandle;		// external override exists
			}
			if(customFileSource != null) {
			    fileHandle = customFileSource.open(filename);
			    if(fileHandle != null && fileHandle.exists()) {
                    if(showMainThreadWarning && Thread.currentThread() == Sys.system.getRenderingThread())
                        Sys.debug(TAG, "I/O operation in rendering thread for: " + filename);
                    return fileHandle;
                }
                // Else custom file source didn't have this file
            }
            fileHandle = Gdx.files.internal(filename);
			if(fileHandle.exists()) {
				// No need to copy to cache if already internal
				if(showMainThreadWarning && Thread.currentThread() == Sys.system.getRenderingThread())
					Sys.debug(TAG, "I/O operation in rendering thread for: " + filename);
                return fileHandle;		// external override exists
			}
		}
		else {
            if(customFileSource != null) {
                fileHandle = customFileSource.open(filename);
                if(fileHandle != null && fileHandle.exists()) {
                    if(showMainThreadWarning && Thread.currentThread() == Sys.system.getRenderingThread())
                        Sys.debug(TAG, "I/O operation in rendering thread for: " + filename);
                    return fileHandle;
                }
                // Else custom file source didn't have this file
            }
            fileHandle = Gdx.files.internal(filename);
			if(skipInternalFileCheck || fileHandle.exists()) {
				if(showMainThreadWarning && Thread.currentThread() == Sys.system.getRenderingThread())
					Sys.debug(TAG, "I/O operation in rendering thread for: " + filename);
				return fileHandle;
			}
		}
		// Else not found
		if(enforceExistence)
			throw new RuntimeException("Failed to open file: " + filename);
		else
			return null;
	}
	
	public static <T> T buildCascadingConfig(String filename, Class<T> type) {
		// Create new instance of T
		T o;
		try {
			o = type.newInstance();
		} catch (Throwable e) {
			throw new RuntimeException("Failed to instantiate type " + type, e);
		}
		// Run script
		Interpreter script = interpreter();
		try {
			script.set("config", o);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		String typeName = getExtension(filename);
		String[] paths = filename.split("/");
		String[] extensions = paths[paths.length].split(".");
		if(extensions.length <= 1)
			throw new IllegalArgumentException("Cannot infer type name for " + filename);
		String currentPath = null;
		String currentFile = "defaults." + typeName + hintsExtension;
		run(currentFile, false, false, false, script);
		for(int c = 0; c < paths.length - 1; c++) {
			if(c == 0)
				currentPath = paths[c];
			else
				currentPath += "/" + paths[c];
			currentFile = currentPath + "/defaults." + typeName + hintsExtension;
			run(currentFile, false, false, false, script);
		}
		run(filename + hintsExtension, false, false, false, script);
		return o;
	}
	
	// Scripts
	private static BshMethod compileScript(String filename, FileHandle file, ArrayList<String> inlined) {
		// Now compile .ps
		try {
			// Compile script
			Interpreter script = interpreter();
			script.eval("__execute(){" + expandScript(new String(file.readBytes()), inlined) + "}");
			BshMethod method = script.getNameSpace().getMethod("__execute", emptyClassArray);
			method.declaringNameSpace = null;		// detach
			method.name = null;
			return method;
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to compile script: " + filename, e);
		}
	}
	
	private static final Pattern importMethodStart = Pattern.compile("importScript\\(\"");
	private static final Pattern importMethodEnd = Pattern.compile("\"\\);");
	
	private static String expandScript(String script, ArrayList<String> inlined) {
		String expanded = "";
		while(true) {
			String[] splits = importMethodStart.split(script, 2);
			if(splits.length == 1) {
				expanded += splits[0];
				break;	// no more importScript commands
			}
			else {
				String[] commandSplits = importMethodEnd.split(splits[1], 2); 
				// Inline specified script
				Sys.info(TAG, "Inlining imported script: " + commandSplits[0]);
				if(inlined != null)
					inlined.add(commandSplits[0]);
				String importedScript = File.open(commandSplits[0]).readString(); 
				importedScript = expandScript(importedScript, inlined);
				expanded += splits[0] + importedScript;
				if(commandSplits.length == 1)
					break;	// no more script left
				// Else continue
				script = commandSplits[1];
			}
		}
		return expanded;		
	}
	
	public static void registerScript(String filename, ArrayList<String> inlined) {
		// Do not retrive from MassFile
		FileHandle f = File.open(filename);
		BshMethod method = File.compileScript(filename, f, inlined);
		// Save to MassFile
		File.saveHints(filename, method);
	}
	
	private static final Object[] emptyObjectArray = new Object[0];
	private static final Class<?>[] emptyClassArray = new Class[0];
	
	public static void dynamicImportScript(String filename, Interpreter interpreter, CallStack callstack) {
		// Check in indexed MassFile first
		BshMethod method = File.getHints(filename, true, false);
		if(method == null) {
			// Load unindexed
			Sys.info(TAG, "Importing unindexed script: " + filename);
			FileHandle f = File.open(filename);
			method = File.compileScript(filename, f, null);
			// Save to MassFile
			File.saveHints(filename, method);
		}
		else
			Sys.info(TAG, "Importing script: " + filename);
		// Invoke
		try {
			method.invoke(emptyObjectArray, interpreter, callstack, null, true);
		} catch (EvalError e) {
			e.printStackTrace();
			throw new RuntimeException("Eval error for file: " + filename, e);
		}
	}
	
	public static Object run(String filename) {
		return run(filename, true, true, false, null);		// by default enforce existence and save
	}

	public static Object run(String filename, boolean enforceExistence) {
		return run(filename, enforceExistence, true, false, null);		// by default save
	}

	public static Object run(String filename, boolean enforceExistence, boolean save) {
		return run(filename, enforceExistence, save, false, null);
	}

	public static Object run(String filename, boolean enforceExistence, boolean save, boolean persistScript, Interpreter script) {
		// Check in indexed MassFile first
		BshMethod method = null;
		if(save)
			method = File.getHints(filename, true, false);
		if(method == null) {
			// Load unindexed
			FileHandle f = File.open(filename, enforceExistence);
			if(f == null)
				return null;		// not enforced existence
				method = File.compileScript(filename, f, null);
			// Save to MassFile if needed
			if(save)
				File.saveHints(filename, method);
			Sys.info(TAG, "Executing unindexed script: " + filename);
		}
		else
			Sys.info(TAG, "Executing script: " + filename);
		// Execute
		if(script == null)
			script = interpreter();
		Object result;
		try {
			// Push current script
			File.pushCurrentScript(filename);
			script.set(VAR_FILENAME, filename);
			result = method.invoke(emptyObjectArray, script, new CallStack(script.getNameSpace()), null, true);
			// Pop current script
			File.popCurrentScript();
		}
		catch(EvalError e) {
			e.printStackTrace();
			throw new RuntimeException("Eval error for file: " + filename, e);
		}
		// Save running script
		runningScripts.put(filename, script);
		if(persistScript)
			runningScripts.setStrongRef(script);
		return result;
	}
	
	public static NameSpace namespace(String filename) {
		Interpreter i = runningScripts.get(filename);
		if(i == null) {
			File.run(filename, true, false, true, null);		// by default namespace will not add to MassFile
			i = runningScripts.get(filename);
			runningScripts.removeStrongRef(i);			// no need to track
			// runningScripts.remove(filename);
		}
		return i.getNameSpace();
	}
	
	public static Interpreter getRunningScript(String filename) {
		return runningScripts.get(filename);
	}

	public static void clearRunningScript(String filename) {
		runningScripts.remove(filename);
	}
	
	public static void pushCurrentScript(String filename) {
		// Remove hints extension if exists
		if(filename.endsWith(hintsExtension))
			filename = filename.substring(0, filename.length() - hintsExtension.length());
		currentScripts.add(filename);
	}
	
	public static String getCurrentScript() {
		return currentScripts.get(currentScripts.size() - 1);
	}
	
	public static String popCurrentScript() {
		return currentScripts.remove(currentScripts.size() - 1);
	}

    public static Interpreter interpreter() {
        Interpreter interpreter = new Interpreter(new StringReader(""), System.out, System.err, false, null, defaultInterpreter, "interpreter");
		interpreter.setClassLoader(File.class.getClassLoader());            // to work with codeblob
        return interpreter;
    }
}