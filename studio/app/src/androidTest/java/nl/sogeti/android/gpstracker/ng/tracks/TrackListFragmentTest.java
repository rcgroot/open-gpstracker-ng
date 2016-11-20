package nl.sogeti.android.gpstracker.ng.tracks;

import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import nl.sogeti.android.gpstracker.ng.util.FragmentTestRule;
import nl.sogeti.android.gpstracker.ng.utils.TrackUriExtensionKt;


@RunWith(AndroidJUnit4.class)
public class TrackListFragmentTest {

    @Rule
    public FragmentTestRule<TrackListFragment> wrapperFragment = new FragmentTestRule<>(TrackListFragment.class, false, false);
    private TrackListFragment sut = null;

    @Before
    public void setup() {
        sut = wrapperFragment.getFragment();
    }

    @Test
    public void testResumeStartsPresenter() {
        // Execute
        wrapperFragment.launchFragment(null);

        // Verify
        Assert.assertNotNull(sut.getTracksPresenter().getContext());
    }

    @Test
    public void testPauseStopsPresenter() {
        // Execute
        wrapperFragment.launchFragment(null);
        wrapperFragment.finishFragment();

        // Verify
        Assert.assertNull(sut.getTracksPresenter().getContext());
    }

    @Test
    public void testTrackSelectionInvokesListener() {
        // Prepare
        final Uri[] uries = new Uri[1];
        Uri uri = TrackUriExtensionKt.trackUri(2);
        TrackListFragment.Listener myListener = new TrackListFragment.Listener() {
            @Override
            public void onTrackSelected(@NotNull Uri uri) {
                uries[0] = uri;
            }
        };
        sut.setListener(myListener);

        // Execute
        sut.onTrackSelected(uri);

        // Verify
        Assert.assertEquals(uries[0], uri);
    }
}