package com.badlogic.gdx.video;

public class FfMpeg {
    public static final String NATIVE_LIBRARY_NAME = "gdx-video-desktop";

    private static boolean loaded = false;
    private static String libraryPath;

    /**
     * This will set the path in which it tries to find the native library.
     *
     * @param path The path on which the library can be found. If it is null or an empty string, the default location
     *             will be used. This is usually a SteamJavaNatives folder inside the jar.
     */
    public static void setLibraryFilePath (String path) {
        libraryPath = path;
    }

    /**
     * This method will load the libraries from the path given with setLibraryFilePath.
     *
     * @return whether loading was successful
     */
    public static boolean loadLibraries () {
        if (loaded) {
            return true;
        }


//        try {
//            String workingPath = Paths.get(".").toAbsolutePath().normalize().toString();
//            System.load(workingPath + java.io.File.separator + "gdx-video-desktop64.dll");
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//            loaded = false;
//            return false;
//        }

        loaded = true;
        register();
        return true;
    }

    /**
     * This tells whether the native libraries are already loaded.
     *
     * @return Whether the native libraries are already loaded.
     */
    public static boolean isLoaded () {
        return loaded;
    }

    public static void setDebugLogging (boolean debugLogging) {
        if (!loaded) {
            if (!loadLibraries()) {
                return;
            }
        }
        setDebugLoggingNative(debugLogging);
    }

    /*
     * Native functions
     * @formatter:off
     */

	/*JNI
	 	extern "C"
	 	{
	 	//This makes certain C libraries usable for ffmpeg
	 	#define __STDC_CONSTANT_MACROS
		#include <libavcodec/avcodec.h>
		#include <libavformat/avformat.h>
		#include <libswscale/swscale.h>
		}
		#include "Utilities.h"
	 */

    private native static void register ();/*
		av_register_all();
		logDebug("av_register_all() called\n");
	 */

    /**
     * This function can be used to turn on/off debug logging of the native code
     *
     * @param debugLogging whether logging should be turned on or off
     */
    private native static void setDebugLoggingNative (boolean debugLogging);/*
		debug(debugLogging);
	 */
}
