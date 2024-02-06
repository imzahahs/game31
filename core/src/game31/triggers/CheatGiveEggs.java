package game31.triggers;

import game31.Globals;
import game31.Keyboard;
import game31.app.homescreen.Homescreen;
import sengine.Entity;
import sengine.graphics2d.Sprite;

public class CheatGiveEggs implements Homescreen.App {
    @Override
    public Entity<?> open() {

        Globals.grid.flapeeBirdApp.queueReward(30);

        Globals.grid.notification.show(Sprite.load("apps/kaigan/icon.png"), null, -1, "Give Eggs", "Adding 30 eggs", Globals.CONTEXT_APP_CHEAT_EGGS);

        return null;
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {
        // nothing
    }
}
