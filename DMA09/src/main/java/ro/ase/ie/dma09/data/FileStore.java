package ro.ase.ie.dma09.data;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import ro.ase.ie.dma09.R;
import ro.ase.ie.dma09.util.Logs;

/**
 * All the file work of the demo.
 *
 * Rule used everywhere: every stream is opened in a try-with-resources block,
 * so it is closed even when an exception is thrown. The leaky pattern to avoid:
 *
 *     FileOutputStream out = ctx.openFileOutput(NAME, MODE_PRIVATE);
 *     out.write(bytes);          // an exception here...
 *     out.close();               // ...skips this line: the file stays open
 */
public class FileStore {

    public static final String NOTE = "note.txt";
    public static final String TEMP = "temp.txt";
    public static final String EXPORT = "note_export.txt";
    public static final String SHARED_DIR = "shared";

    private final Context ctx;

    public FileStore(Context context) {
        this.ctx = context.getApplicationContext();
    }

    // ------------------------------------------------------------------
    // Internal storage (getFilesDir): private, no permission, deleted on uninstall
    // ------------------------------------------------------------------

    public void save(String text) throws IOException {
        write(text, Context.MODE_PRIVATE);
    }

    public void append(String text) throws IOException {
        write(text + "\n", Context.MODE_APPEND);
    }

    private void write(String text, int mode) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        try (FileOutputStream out = ctx.openFileOutput(NOTE, mode)) {
            out.write(bytes);
        }
        String how = mode == Context.MODE_APPEND ? "appended" : "saved";
        Logs.d(Logs.FILES, how + " " + bytes.length + " bytes -> "
                + new File(ctx.getFilesDir(), NOTE));
    }

    /** Throws FileNotFoundException when the note does not exist yet. */
    public String read() throws IOException {
        try (FileInputStream in = ctx.openFileInput(NOTE)) {
            String text = readAll(in);
            Logs.d(Logs.FILES, "read " + text.length() + " chars from " + NOTE);
            return text;
        }
    }

    public String readOrDefault(String fallback) {
        try {
            return read();
        } catch (IOException e) {
            Logs.d(Logs.FILES, NOTE + " not available, using default text");
            return fallback;
        }
    }

    /** Names and sizes of all private files in getFilesDir(). */
    public String list() {
        String[] names = ctx.fileList();
        Logs.d(Logs.FILES, "fileList() returned " + names.length + " file(s)");
        if (names.length == 0) {
            return "(no files)";
        }
        StringBuilder sb = new StringBuilder();
        for (String name : names) {
            sb.append(name).append("  ")
                    .append(new File(ctx.getFilesDir(), name).length()).append(" bytes\n");
        }
        return sb.toString();
    }

    public boolean delete() {
        boolean deleted = ctx.deleteFile(NOTE);
        Logs.i(Logs.FILES, "deleteFile(" + NOTE + ") returned " + deleted);
        return deleted;
    }

    // ------------------------------------------------------------------
    // Cache (getCacheDir): temporary data the system may delete when space is low
    // ------------------------------------------------------------------

    public File writeCache(String text) throws IOException {
        File file = new File(ctx.getCacheDir(), TEMP);
        writeFile(file, text);
        Logs.d(Logs.FILES, "cache file written: " + file);
        return file;
    }

    public long cacheSize() {
        long size = sizeOf(ctx.getCacheDir());
        Logs.d(Logs.FILES, "cache size = " + size + " bytes");
        return size;
    }

    /** Deletes everything inside the cache directory; returns the number of files removed. */
    public int clearCache() {
        int count = deleteChildren(ctx.getCacheDir());
        Logs.i(Logs.FILES, "cache cleared, " + count + " file(s) removed");
        return count;
    }

    // ------------------------------------------------------------------
    // External app-specific storage: visible to the user and to a computer over USB,
    // still removed on uninstall, and needs NO permission.
    // ------------------------------------------------------------------

    public File exportExternal(String text) throws IOException {
        String state = Environment.getExternalStorageState();
        Logs.d(Logs.FILES, "external storage state = " + state);
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            throw new IOException("External storage not available (" + state + ")");
        }
        File dir = ctx.getExternalFilesDir(null);
        if (dir == null) {
            throw new IOException("getExternalFilesDir() returned null");
        }
        File file = new File(dir, EXPORT);
        writeFile(file, text);
        Logs.i(Logs.FILES, "exported to external app-specific folder: " + file);
        return file;
    }

    public String paths() {
        File ext = ctx.getExternalFilesDir(null);
        String text = "Internal files : " + ctx.getFilesDir()
                + "\nCache          : " + ctx.getCacheDir()
                + "\nExternal files : " + (ext == null ? "(not available)" : ext.toString())
                + "\nExternal cache : " + ctx.getExternalCacheDir()
                + "\nPreferences    : " + new File(ctx.getDataDir(), "shared_prefs");
        Logs.d(Logs.FILES, "paths:\n" + text);
        return text;
    }

    // ------------------------------------------------------------------
    // Files shared through FileProvider (see res/xml/file_paths.xml)
    // ------------------------------------------------------------------

    public File prepareShareFile(String text) throws IOException {
        File dir = new File(ctx.getCacheDir(), SHARED_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create " + dir);
        }
        File file = new File(dir, NOTE);
        writeFile(file, text);
        Logs.d(Logs.FILES, "file prepared for sharing: " + file);
        return file;
    }

    // ------------------------------------------------------------------
    // Read-only files packed in the APK
    // ------------------------------------------------------------------

    /** res/raw: found by resource id, compile-time checked. */
    public String readRaw() throws IOException {
        try (InputStream in = ctx.getResources().openRawResource(R.raw.help)) {
            Logs.d(Logs.FILES, "opened res/raw/help.txt by id R.raw.help");
            return readAll(in);
        }
    }

    /** assets: found by file name, free folder structure. */
    public String readAsset() throws IOException {
        try (InputStream in = ctx.getAssets().open("license.txt")) {
            Logs.d(Logs.FILES, "opened assets/license.txt by name");
            return readAll(in);
        }
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private static void writeFile(File file, String text) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    /** Reads a whole stream as UTF-8. The caller owns (and closes) the stream. */
    public static String readAll(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (!first) {
                    sb.append('\n');
                }
                sb.append(line);
                first = false;
            }
        }
        return sb.toString();
    }

    private static long sizeOf(File file) {
        if (file.isFile()) {
            return file.length();
        }
        long total = 0;
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                total += sizeOf(child);
            }
        }
        return total;
    }

    private static int deleteChildren(File dir) {
        int count = 0;
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                boolean wasFile = child.isFile();   // ask BEFORE deleting
                if (child.isDirectory()) {
                    count += deleteChildren(child);
                }
                if (child.delete() && wasFile) {
                    count++;
                }
            }
        }
        return count;
    }
}
