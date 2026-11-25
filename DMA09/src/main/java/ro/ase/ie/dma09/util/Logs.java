package ro.ase.ie.dma09.util;

import android.util.Log;

/**
 * One place for all logging, so everything can be filtered in Logcat with:
 *
 *     tag:DMA09
 *
 * or one area only, e.g.  tag:DMA09/FILES
 *
 * Every line also shows the thread name. Disk work in this demo runs on "main"
 * (fine for tiny files; use a background thread for anything bigger).
 */
public final class Logs {

    public static final String APP = "DMA09/APP";
    public static final String PREFS = "DMA09/PREFS";
    public static final String FILES = "DMA09/FILES";
    public static final String SAF = "DMA09/SAF";

    private Logs() {
    }

    private static String withThread(String msg) {
        return "[" + Thread.currentThread().getName() + "] " + msg;
    }

    public static void d(String tag, String msg) {
        Log.d(tag, withThread(msg));
    }

    public static void i(String tag, String msg) {
        Log.i(tag, withThread(msg));
    }

    public static void w(String tag, String msg) {
        Log.w(tag, withThread(msg));
    }

    public static void e(String tag, String msg, Throwable t) {
        Log.e(tag, withThread(msg), t);
    }
}
