package sengine.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.concurrent.ConcurrentHashMap;

import sengine.File;
import sengine.Sys;

/**
 * Created by Azmi on 4/7/2016.
 */
public class LiveEditor {
    public static final String TAG = "LiveEditor";

    public static boolean VERBOSE = false;


    public static class Source<T> {
        private String basePath;
        private String filename;

        // Current
        private LiveEditor editor = null;
        private long lastModified = -1;

        public String basePath() {
            return basePath;
        }

        public String filename() {
            return filename;
        }

        public Source path(String basePath, String filename) {
            this.basePath = basePath;
            this.filename = filename;
            return this;
        }

        private FileHandle openFile(boolean enforceExistence) {
            if(basePath == null || filename == null) {
                if(enforceExistence)
                    throw new RuntimeException("Incomplete path " + basePath + " " + filename);
                return null;
            }
            if(LiveEditor.editor == null) {
                if(enforceExistence)
                    throw new RuntimeException("LiveEditor required to read " + filename);
                return null;
            }
            return LiveEditor.editor.openSource(basePath, filename, enforceExistence);
        }

        private String readOnline() {
            if(basePath == null || filename == null || LiveEditor.editor == null)
                return null;
            return LiveEditor.editor.readOnlineSource(basePath, filename);
        }

        private void check() {
            FileHandle sourceFile = openFile(false);
            if(sourceFile == null)
                return;
            long modified = sourceFile.lastModified();
            if(modified == lastModified)
                return;            // already loaded before
            // Else file was modified
            try {
                // Inform change unless its the first time checking this file
                if(lastModified != -1)
                    onChangeDetected();
            } finally {
                lastModified = modified;
            }
        }

        public T load(boolean enforceExistence) {
            // Check if online source is available
            String source = readOnline();
            if(source != null) {
                // Successfully read online source, convert it
                // Convert
                Sys.info(TAG, "Converting online source " + filename);
                try {
                    T result = convertOnline(source);
                    if(result != null)
                        return result;          // success
                    Sys.error(TAG, "Unable to convert online source " + filename);
                } catch (Throwable e) {
                    Sys.error(TAG, "Failed to convert online source " + filename, e);
                }
            }
            // Else try to convert local
            FileHandle sourceFile = openFile(enforceExistence);
            if(sourceFile == null)
                return null;            // not enforced
            try {
                source = File.removeUTF8BOM(sourceFile.readString("UTF-8"));
            } catch (Throwable t) {
                if(enforceExistence)
                    throw new RuntimeException("Unable to read local source " + filename, t);
                Sys.info(TAG, "Unable to read local source " + filename, t);
                return null;
            }
            // Convert
            Sys.info(TAG, "Converting local source " + filename);
            try {
                return convert(source);           // consume
            } catch (Throwable e) {
                if(enforceExistence)
                    throw new RuntimeException("Unable to convert local source " + filename, e);
                Sys.error(TAG, "Unable to convert local source " + filename, e);
                return null;
            } finally {
                // Update
                lastModified = sourceFile.lastModified();
            }
        }


        public void start() {
            if(LiveEditor.editor == null)
                return;         // no editor
            editor = LiveEditor.editor;
            if(!editor.sources.contains(this, true))
                editor.sources.add(this);
        }

        public void stop() {
            if(editor == null)
                return;     // not started
            editor.sources.removeValue(this, true);
            editor = null;
        }

        // Implementation
        protected void onChangeDetected() {
            // do nothing
        }

        protected T convert(String source) throws Throwable {
            return null;    // nothing
        }

        protected T convertOnline(String source) throws Throwable {
            return null;    // nothing
        }
    }

    public static LiveEditor editor = null;

    public static void refresh() {
        if(editor == null)
            return;        // no editor
        editor.refresh(true);
    }

    // Online resolvers
    private final ObjectMap<String, NetRequest> onlineResolver = new ObjectMap<String, NetRequest>();
    private final ConcurrentHashMap<String ,String> onlineCache = new ConcurrentHashMap<>();

    // File system resolvers
    private final ObjectMap<String, FileHandle> basePathResolver = new ObjectMap<String, FileHandle>();
    private final float tRefreshInterval;


    final Array<Source> sources = new Array<Source>(Source.class);

    // Current
    float tScheduledRefresh = 0f;

    // Online Resolvers

    public void addOnlineSource(String basePath, String filename, NetRequest request) {
        String fullName = basePath + "://" + filename;
        onlineResolver.put(fullName, request);
    }

    public void removeOnlineSource(String basePath, String filename) {
        String fullName = basePath + "://" + filename;
        onlineResolver.remove(fullName);
    }

    public String readOnlineSource(String basePath, String filename) {
        String fullName = basePath + "://" + filename;
        NetRequest request = onlineResolver.get(fullName);
        if(request == null)
            return null;        // not found
        // Check cache
        String result = onlineCache.get(fullName);
        if(result != null)
            return result;
        // Instantiate
        request = request.instantiate(request.getRequestName() != null ? request.getRequestName() : fullName);
        result = request.read();
        // Save to cache
        onlineCache.put(fullName, result);
        return result;
    }

    public void clearOnlineCache() {
        onlineCache.clear();
    }


    // File system resolvers
    public void addBasePath(String basePath, FileHandle resolved) {
        basePathResolver.put(basePath, resolved);
    }

    public void removeBasePath(String basePath) {
        basePathResolver.remove(basePath);
    }

    public void clearSources() {
        sources.clear();
    }

    public void clearBasePaths() {
        basePathResolver.clear();
    }

    public LiveEditor(float tRefreshInterval) {
        this.tRefreshInterval = tRefreshInterval;
    }

    public FileHandle openSource(String basePath, String filename, boolean enforceExistence) {
        FileHandle base = basePathResolver.get(basePath);
        if(base == null) {
            if(enforceExistence)
                throw new RuntimeException("Unable to resolve base path " + basePath);
            if(VERBOSE) Sys.info(TAG, "Unable to resolve base path " + basePath);
            return null;
        }
        FileHandle source = base.child(filename);
        if(!source.exists()) {
            if(enforceExistence)
                throw new RuntimeException("Source not found " + source.path());
            if(VERBOSE) Sys.info(TAG, "Source not found " + source.path());
            return null;
        }
        return source;
    }

    public void refresh(boolean force) {
        float time = Sys.getTime();
        if(time < tScheduledRefresh && !force)
            return;
        tScheduledRefresh = time + tRefreshInterval;            // schedule next
        // Refresh now
        for(int c = 0; c < sources.size; c++)
            sources.items[c].check();
    }

}
