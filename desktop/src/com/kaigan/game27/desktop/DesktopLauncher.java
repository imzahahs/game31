package com.kaigan.game27.desktop;


import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.SharedLibraryLoader;

import org.oxbow.swingbits.util.Strings;

import java.nio.file.Paths;

import javax.swing.UIManager;

import game31.DesktopMain;

public class DesktopLauncher  {

    public static void main (String[] args) {
        // Register crash handling
        CrashHandler.register();

        // System UI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
            // Log but ignore
            System.err.print("Failed to set system look and feel\n" + Strings.stackStraceAsString(e) + "\n");
        }

        try {
            // Configure native libraries
            String workingPath = Paths.get(".").toAbsolutePath().normalize().toString();

            // Load GDX natives
            GdxNativesLoader.disableNativesLoading = true;
            SharedLibraryLoader.setLoaded("gdx");
            SharedLibraryLoader.setLoaded("gdx-freetype");
            System.load(workingPath + java.io.File.separator + "gdx64.dll");
            System.load(workingPath + java.io.File.separator + "gdx-freetype64.dll");
            System.load(workingPath + java.io.File.separator + "gdx-video-desktop64.dll");

            System.setProperty("org.lwjgl.librarypath", workingPath);

//
//            Configuration.LIBRARY_PATH.set(workingPath + "/lwjgl");

//            System.setProperty("org.lwjgl.librarypath", workingPath);

            // Load blob
            CodeBlob blob = new CodeBlob("core.binary", "bsh", "game31", "sengine");
            Thread.currentThread().setContextClassLoader(blob);

            // Load DesktopMain
            Class<?> type = blob.loadClass("game31.DesktopMain");

            // Start DesktopMain
            type.getConstructor(String[].class).newInstance((Object) args);

//            new DesktopMain(args);
        } catch (Throwable e) {
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);       // Workaround for JNI launcher not using uncaught exception
        }
    }



}
