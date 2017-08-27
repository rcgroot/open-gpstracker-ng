package nl.sogeti.android.gpstracker.ng.ui.fragments;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import nl.sogeti.android.gpstracker.ng.robots.TrackListRobot;
import nl.sogeti.android.gpstracker.ng.tracklist.TrackListFragment;
import nl.sogeti.android.gpstracker.ng.util.FragmentTestRule;
import nl.sogeti.android.gpstracker.v2.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

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
        new TrackListRobot()
                .isTrackListDisplayed();
    }
}