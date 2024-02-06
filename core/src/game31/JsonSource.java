package game31;

import sengine.File;
import sengine.utils.LiveEditor;

/**
 * Created by Azmi on 23/7/2016.
 */
public class JsonSource<T> extends LiveEditor.Source<T> {

    public static final String BASEPATH = "assets";

    public interface OnChangeListener<T> {
        void onChangeDetected(JsonSource<T> source);
    }


    private final Class<T> type;
    private OnChangeListener<T> listener;



    public OnChangeListener<T> listener() {
        return listener;
    }

    public JsonSource<T> listener(OnChangeListener<T> listener) {
        this.listener = listener;
        return this;
    }

    public JsonSource(String filename, Class<T> type) {
        path(BASEPATH, filename);

        this.type = type;
    }

    public T load() {
        return load(true);
    }

    public T load(boolean enforceExistence) {
        T model = super.load(false);
        if(model != null)
            return model;
        // Cannot load from source, get from hints if exists
        model = File.getHints(filename(), true, false);
        if (model != null)
            return model;
        if(enforceExistence)
            throw new RuntimeException("Unable to load json " + filename());
        return null;        // not enforced
    }

    @Override
    protected void onChangeDetected() {
        if(listener != null)
            listener.onChangeDetected(this);
    }

    @Override
    protected T convert(String content) throws Throwable {
        T model = Globals.gson.fromJson(content, type);
        File.saveHints(filename(), model);
        return model;
    }

    @Override
    protected T convertOnline(String source) throws Throwable {
        T model = Globals.sheets.parse(source, type);

        if(model == null)
            throw new RuntimeException("Failed to convert source\n" + source);
        File.saveHints(filename(), model);
        return model;
    }
}
