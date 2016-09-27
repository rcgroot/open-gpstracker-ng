package nl.sogeti.android.gpstracker.ng.about;

import android.support.test.espresso.Espresso;
import android.support.test.runner.AndroidJUnit4;
import android.webkit.WebView;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import nl.sogeti.android.gpstracker.ng.util.FragmentTestRule;
import nl.sogeti.android.gpstracker.ng.util.WebViewIdlingResource;
import nl.sogeti.android.gpstracker.v2.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class AboutFragmentTest {


    @Rule
    public FragmentTestRule<AboutFragment> wrapperFragment = new FragmentTestRule<>(AboutFragment.class);
    private AboutFragment sut = null;
    private WebViewIdlingResource webIdlingResource;

    @Before
    public void setUp() {
        sut = wrapperFragment.getFragment();
        WebView webview = (WebView) sut.getDialog().findViewById(R.id.fragment_about_webview);
        webIdlingResource = new WebViewIdlingResource(webview);
        Espresso.registerIdlingResources(webIdlingResource);
    }

    @After
    public void tearDown() {
        if (webIdlingResource!=null) {
            Espresso.unregisterIdlingResources(webIdlingResource);
            webIdlingResource = null;
        }
    }

    @Test
    public void showAboutInfo() {
        // Verify
        onView(withId(R.id.fragment_about_version))
                .check(matches(isDisplayed()));
        onView(withId(R.id.fragment_about_webview))
                .check(matches(isDisplayed()));
    }
}
