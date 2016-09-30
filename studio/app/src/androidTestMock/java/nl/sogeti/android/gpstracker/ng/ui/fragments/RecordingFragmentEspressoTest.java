package nl.sogeti.android.gpstracker.ng.ui.fragments;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import nl.sogeti.android.gpstracker.ng.recording.RecordingFragment;
import nl.sogeti.android.gpstracker.ng.util.FragmentTestRule;
import nl.sogeti.android.gpstracker.ng.util.MockServiceManager;
import nl.sogeti.android.gpstracker.v2.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

public class RecordingFragmentEspressoTest {

    @Rule
    public FragmentTestRule<RecordingFragment> wrapperFragment = new FragmentTestRule<>(RecordingFragment.class);
    private RecordingFragment sut;
    private MockServiceManager mockServiceManager;

    @Before
    public void setUp() {
        mockServiceManager = new MockServiceManager();
        mockServiceManager.reset();
        sut = wrapperFragment.getFragment();
    }

    @After
    public void tearDown() {
        mockServiceManager.reset();
        mockServiceManager = null;
        sut = null;
    }

    @Test
    public void testStartUp() {
        // Verify
        onView(withId(R.id.fragment_recording_container)).check(matches(not(isDisplayed())));
    }
}
