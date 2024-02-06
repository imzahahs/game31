package game31;

import com.badlogic.gdx.Gdx;

import sengine.Entity;
import sengine.Processor;

/**
 * Created by Azmi on 4/29/2017.
 */

public class RefreshAction extends Entity<Grid> {

    private Processor.Task task = new Processor.Task(true, true, false, false) {
        @Override
        protected void processAsync() {
            load();
        }
    };

    protected void load() {
        // user implementation
    }

    protected void complete() {
        Grid v = Globals.grid;

        // Stop loading
        v.loadingMenu.detach();
        detach();

        // Return normal
        v.screensGroup.renderingEnabled = true;
        v.whatsupApp.renderingEnabled = true;
        v.photoRollApp.renderingEnabled = true;
    }

    public void start() {
        Grid v = Globals.grid;

        // Show loading
        v.loadingMenu.attach(v);


        // Freeze
        v.screensGroup.renderingEnabled = false;
        v.whatsupApp.renderingEnabled = false;
        v.photoRollApp.renderingEnabled = false;

        // Start
        attach(v);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                if(task.reset())
                    task.start();
            }
        });
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        super.render(v, r, renderTime);

        if(task.isComplete())
            complete();
    }
}
