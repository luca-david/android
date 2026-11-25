package ro.ase.ie.dma09;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ro.ase.ie.dma09.data.PrefsManager;

@RunWith(AndroidJUnit4.class)
public class PrefsManagerTest {

    private PrefsManager prefs;

    @Before
    public void setUp() {
        prefs = new PrefsManager(ApplicationProvider.getApplicationContext());
        prefs.clear();
    }

    @After
    public void tearDown() {
        prefs.clear();
    }

    @Test
    public void userNameRoundTrip() {
        assertEquals("", prefs.getUserName());
        assertTrue(prefs.setUserNameNow("Ana"));
        assertEquals("Ana", prefs.getUserName());
    }

    @Test
    public void launchCounterIncrements() {
        int first = prefs.incrementLaunchCount();
        assertEquals(first + 1, prefs.incrementLaunchCount());
    }

    @Test
    public void defaultsAreReturned() {
        assertEquals(false, prefs.isDarkMode());
        assertEquals("date", prefs.getSortOrder());
    }
}
