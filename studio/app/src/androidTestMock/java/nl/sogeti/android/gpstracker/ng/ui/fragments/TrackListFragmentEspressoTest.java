package nl.sogeti.android.gpstracker.ng.ui.fragments;

import android.Manifest;
import android.support.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import nl.sogeti.android.gpstracker.ng.robots.TrackListRobot;
import nl.sogeti.android.gpstracker.ng.tracklist.TrackListFragment;
import nl.sogeti.android.gpstracker.ng.util.FragmentTestRule;

public class TrackListFragmentEspressoTest {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
        new TrackListRobot()
                .isTrackListDisplayed();
    }
}
