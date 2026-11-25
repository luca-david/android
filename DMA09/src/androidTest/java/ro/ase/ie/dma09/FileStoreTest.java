package ro.ase.ie.dma09;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.io.IOException;

import ro.ase.ie.dma09.data.FileStore;

@RunWith(AndroidJUnit4.class)
public class FileStoreTest {

    private FileStore store;

    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        store = new FileStore(ctx);
        store.delete();
    }

    @After
    public void tearDown() {
        store.delete();
        store.clearCache();
    }

    @Test
    public void saveThenRead() throws IOException {
        store.save("hello");
        assertEquals("hello", store.read());
    }

    @Test
    public void appendAddsLines() throws IOException {
        store.save("one");
        store.append("two");
        assertEquals("one\ntwo", store.read());
    }

    @Test(expected = FileNotFoundException.class)
    public void readMissingFileThrows() throws IOException {
        store.read();
    }

    @Test
    public void deleteRemovesFile() throws IOException {
        store.save("x");
        assertTrue(store.delete());
        assertFalse(store.delete());
    }

    @Test
    public void cacheCanBeWrittenAndCleared() throws IOException {
        store.writeCache("temp");
        assertTrue(store.cacheSize() > 0);
        assertTrue(store.clearCache() >= 1);
        assertEquals(0, store.cacheSize());
    }

    @Test
    public void rawAndAssetAreReadable() throws IOException {
        assertTrue(store.readRaw().contains("res/raw"));
        assertTrue(store.readAsset().contains("assets"));
    }
}
