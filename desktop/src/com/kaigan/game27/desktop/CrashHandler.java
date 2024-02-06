package com.kaigan.game27.desktop;

import org.lwjgl.opengl.Display;
import org.oxbow.swingbits.dialog.task.TaskDialog;
import org.oxbow.swingbits.util.Strings;

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * Created by Azmi on 9/13/2017.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final int PRINT_STREAM_SIZE = 1024;      // 1K

    private static FileOutputStream defaultOut = new FileOutputStream(FileDescriptor.out);
    private static final CacheOutputStream outBuffer = new CacheOutputStream(PRINT_STREAM_SIZE, defaultOut);
    private static final CacheOutputStream errBuffer = new CacheOutputStream(PRINT_STREAM_SIZE, defaultOut);

    public static String version = "undefined";

    public static void register() {
        // Hookup cached print streams
        System.setOut(new PrintStream(outBuffer, true));
        System.setErr(new PrintStream(errBuffer, true));

        // Crash handler
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
    }


    private CrashHandler() {
        // private
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // Destroy gdx window
        try {
            Display.destroy();
        } catch (Throwable e) {
            // Ignore
        }

        new Thread(() -> {
            // Driver details
            String vendor = System.getProperty("simulacra.gl.vendor", "unknown");
            String version = System.getProperty("simulacra.gl.version", "unknown");
            String renderer = System.getProperty("simulacra.gl.renderer", "unknown");

            // Prepare stack trace
            String stacktrace = Strings.stackStraceAsString(throwable);
//                details = details.replaceAll("(?m)^[\\s&&[^\\n]]+|[\\s+&&[^\\n]]+$", "");
            stacktrace = stacktrace.replace("java", "rt");

            // Logs
            String outLog = new String(outBuffer.toArray(), Charset.forName("UTF-8"));
            String errLog = new String(errBuffer.toArray(), Charset.forName("UTF-8"));
            if (outLog.isEmpty())
                outLog = "none";
            if (errLog.isEmpty())
                errLog = "none";

            // Compile
            String details = String.format(Locale.US,
                    "Version: " + version + "\n" +
                            "Vendor: %s\n" +
                            "OpenGL version: %s\n" +
                            "Renderer: %s\n" +
                            "\n" +
                            "Stacktrace:\n" +
                            "%s\n" +
                            "\n" +
                            "Output log:\n" +
                            "%s\n" +
                            "\n" +
                            "Error log:\n" +
                            "%s",
                    vendor, version, renderer, stacktrace, outLog, errLog);

            // Inspect stack trace and build appropriate report
            String inspect = stacktrace.toLowerCase();
            boolean openglProblem = inspect.contains("opengl") || inspect.contains("open gl") || inspect.contains("glfw");
            boolean steamProblem = inspect.contains("unable to initialize steam");

            // Log
            System.err.print("Exception in \"" + thread + "\" thread\n" + details + "\n");

            boolean okay = false;

            if (steamProblem) {
                // Steam
                showDialog(
                        "Steam Required",
                        "The game was unable to communicate with Steam. Please open your steam client and start the game again.",
                        details,
                        "Okay", null
                );
            } else if (openglProblem) {
                // Likely an opengl problem
                okay = showDialog(
                        "Graphics driver update required",
                        "We are very sorry that this crash occurred. Our goal is to prevent crashes like this from occurring\n" +
                                "in the future. Please help us track down and fix this crash by sending us the following report.\n" +
                                "\n" +
                                "It is likely that you can solve this problem by updating your graphics drivers.",
                        details,
                        "Send", "Close"
                );
            } else {
                // Else unexpected error
                okay = showDialog(
                        "Unfortunately the game has crashed",
                        "We are very sorry that this crash occurred. Our goal is to prevent crashes like this from occurring\n" +
                                "in the future. Please help us track down and fix this crash by sending us the following report.",
                        details,
                        "Send", "Close"
                );
            }

            if (okay) {
                try {
                    details = URLEncoder.encode(details, "UTF-8");
                    details = "https://docs.google.com/forms/d/e/1FAIpQLSeZkhCmKr_x1sFJ_gA2IBtfKSibjhnmA5LnJBYzwW-MymsEnw/viewform?usp=pp_url&entry.716492110&entry.978448459&entry.1820513751=" + details;
                    Desktop.getDesktop().browse(new URI(details));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            // Force quit
            System.exit(-1);
        }).start();
    }

    private static boolean showDialog(String instructions, String text, String details, String okText, String cancelText) {
        // Hack to remove the ugly ass java title icon.. ugh...
        JFrame frame = new JFrame();
        frame.setIconImage(null);
        frame.setAlwaysOnTop(true);

        // Exception Dialog
        TaskDialog dlg = new TaskDialog(frame, "SIMULACRA: Pipe Dreams");

        dlg.setInstruction(instructions);
        dlg.setText(text);

        if(cancelText != null) {
            dlg.setCommands(
                    TaskDialog.StandardCommand.OK.derive(okText),
                    TaskDialog.StandardCommand.CANCEL.derive(cancelText)
            );
        }
        else {
            dlg.setCommands(
                    TaskDialog.StandardCommand.OK.derive(okText)
            );
        }

        if(details != null) {
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setFont(UIManager.getFont("Label.font"));
            textArea.setText(details);
            textArea.setCaretPosition(0);

            JScrollPane scroller = new JScrollPane(textArea);
            scroller.setPreferredSize(new Dimension(100, 200));
            dlg.getDetails().setExpandableComponent(scroller);
            dlg.getDetails().setExpanded(true);
        }

        return dlg.show().equals(TaskDialog.StandardCommand.OK);
    }

}
