package game31.triggers;

import game31.Globals;
import game31.Keyboard;
import game31.app.homescreen.Homescreen;
import sengine.Entity;
import sengine.graphics2d.Sprite;

public class CheatGameSpeed implements Homescreen.App {
    @Override
    public Entity<?> open() {
        if(Globals.tChatTimingMultiplier != 0.1f) {
            Globals.tChatTimingMultiplier = 0.1f;
            Globals.tKeyboardAnimationSpeedMultiplier = 0.1f;
            Globals.grid.keyboard.detach();
            Globals.grid.keyboard = new Keyboard();

            Globals.grid.notification.show(Sprite.load("apps/kaigan/icon.png"), null, -1, "Game Speed", "Fast mode enabled", Globals.CONTEXT_APP_CHEAT_GAMESPEED);
        }
        else {
            Globals.tChatTimingMultiplier = 0.9f;
            Globals.tKeyboardAnimationSpeedMultiplier = 1.0f;
            Globals.grid.keyboard.detach();
            Globals.grid.keyboard = new Keyboard();

            Globals.grid.notification.show(Sprite.load("apps/kaigan/icon.png"), null, -1, "Game Speed", "Fast mode disabled", Globals.CONTEXT_APP_CHEAT_GAMESPEED);
        }
        return null;
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {
        // nothing
    }
}
