package nl.sogeti.android.gpstracker.ng.ui.fragments;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import nl.sogeti.android.gpstracker.ng.tracklist.TrackListFragment;
import nl.sogeti.android.gpstracker.ng.util.FragmentTestRule;

public class TrackListFragmentEspressoTest {

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
        Assert.assertNotNull(sut.getTrackListPresenter().getContext());
    }
}