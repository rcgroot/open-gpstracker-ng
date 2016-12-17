package nl.sogeti.android.gpstracker.ng.tracklist.summary;

import android.content.Context;
import android.net.Uri;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import nl.sogeti.android.gpstracker.ng.utils.TrackUriExtensionKt;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SummaryManagerTest {

    Uri uri = TrackUriExtensionKt.trackUri(5);
    @Mock
    ExecutorService mockExecutor = null;
    @Mock
    Context mockContext = null;
    summaryManager sut = summaryManager.INSTANCE;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        sut.setExecutor(mockExecutor);
    }

    @After
    public void tearDown() {
        while (sut.isRunning()) {
            sut.stop();
        }
    }

    @Test
    public void testStartSetsRunning() {
        // Execute
        sut.start();

        // Verify
        assertTrue(sut.isRunning());
    }

    @Test
    public void testBeginsStopped() {
        // Verify
        assertFalse(sut.isRunning());
    }

    @Test
    public void testTwiceStartSingleStop() {
        // Execute
        sut.start();
        sut.start();
        sut.stop();

        // Verify
        assertTrue(sut.isRunning());
    }

    @Test
    public void testStopStoppedIllegal() {
        // Prepare
        IllegalStateException caught = null;

        // Execute
        try {
            sut.stop();
        } catch (IllegalStateException e) {
            caught = e;
        }

        // Verify
        assertNotNull(caught);
    }

    @Test
    public void testCoreCount() {
        // Execute
        int threads = sut.numberOfThreads();

        // Verify
        assertThat(threads, Matchers.greaterThan(1));
    }

    @Test
    public void testBackgroundPriority() {
        // Prepare
        ThreadFactory factory = new summaryManager.BackgroundThreadFactory();

        // Execute
        Thread thread = factory.newThread(null);

        // Verify
        assertThat(thread.getPriority(), Matchers.lessThanOrEqualTo(android.os.Process.THREAD_PRIORITY_BACKGROUND));
    }

    @Test
    public void testCallWhenStopped() {
        // Prepare
        final List<Summary> callback = new LinkedList<>();

        // Execute
        sut.collectSummaryInfo(mockContext, uri, new Function1<Summary, Unit>() {
            @Override
            public Unit invoke(Summary summary) {
                callback.add(summary);
                return null;
            }
        });

        // Verify
        assertThat(callback.size(), Matchers.is(0));
        verify(mockExecutor, times(0)).submit(org.mockito.Matchers.any(Runnable.class));
    }

    @Test
    public void testExecutionWhenStopped() {
        // Prepare
        final List<Summary> callback = new LinkedList<>();

        // Execute
        sut.executeTrackCalculation(mockContext, uri, new Function1<Summary, Unit>() {
            @Override
            public Unit invoke(Summary summary) {
                callback.add(summary);
                return null;
            }
        });

        // Verify
        assertThat(callback.size(), is(0));
    }
}