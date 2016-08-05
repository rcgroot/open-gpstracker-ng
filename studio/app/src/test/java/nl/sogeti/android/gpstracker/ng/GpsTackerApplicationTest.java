package nl.sogeti.android.gpstracker.ng;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import timber.log.Timber;

public class GpsTackerApplicationTest {

    private GpsTackerApplication sut;

    @Before
    public void setup() {
        sut = new GpsTackerApplication();
        Timber.uprootAll();
    }

    @Test
    public void onCreateDebug() throws Exception {
        // Prepare
        sut.debug = true;

        // Execute
        sut.onCreate();

        // Verify
        Assert.assertEquals(Timber.treeCount(), 1);
    }

    @Test
    public void onCreateRelease() throws Exception {
        // Prepare
        sut.debug = false;

        // Execute
        sut.onCreate();

        // Verify
        Assert.assertEquals(Timber.treeCount(), 0);
    }
}