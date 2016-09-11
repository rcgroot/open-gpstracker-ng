package nl.sogeti.android.gpstracker.ng.tracks;

import android.content.ContentUris;
import android.net.Uri;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.app.AppCompatActivity;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import static nl.sogeti.android.gpstracker.integration.ContentConstants.Tracks.TRACKS_URI;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class TrackListFragmentTest extends AppCompatActivity {

    @Rule
    public ActivityTestRule<TrackListFragmentTest> wrapperActivity = new ActivityTestRule<>(TrackListFragmentTest.class);
    private TrackListFragment sut = null;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        sut = new TrackListFragment();
        wrapperActivity.launchActivity(null);
        wrapperActivity.getActivity().getSupportFragmentManager().beginTransaction().add(sut, "SUT").commitNow();
    }

    @Test
    public void testResumeStartsPresenter() {
        // Execute
        sut.onResume();

        // Verify
        Assert.assertNotNull(sut.getTracksPresenter().getContext());
    }

    @Test
    public void testPauseStopsPresenter() {
        // Execute
        sut.onResume();

        // Verify
        Assert.assertNull(sut.getTracksPresenter().getContext());
    }

    @Test
    public void testTrackSelectionInvokesListener() {
        // Prepare
        TrackListFragment.Listener mockListener = mock(TrackListFragment.Listener.class);
        Uri uri = ContentUris.withAppendedId(TRACKS_URI, 2);
        sut.setListener(mockListener);

        // Execute
        sut.onTrackSelected(uri);

        // Verify
        verify(mockListener).onTrackSelected(uri);
    }
}