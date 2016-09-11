//package nl.sogeti.android.gpstracker.ng.tracks
//
//import android.content.ContentUris
//import android.support.test.rule.ActivityTestRule
//import android.support.test.runner.AndroidJUnit4
//import android.support.v7.app.AppCompatActivity
//import junit.framework.Assert
//import nl.sogeti.android.gpstracker.integration.ContentConstants.Tracks.TRACKS_URI
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.Mockito.mock
//import org.mockito.Mockito.verify
//import org.mockito.MockitoAnnotations
//
//@RunWith(AndroidJUnit4::class)
//class TrackListFragmentTest : AppCompatActivity() {
//
//    @Rule
//    public val wrapperActivity = ActivityTestRule<TrackListFragmentTest>(TrackListFragmentTest::class.java)
//    private var sut: TrackListFragment? = null
//
//    @Before
//    fun setup() {
//        MockitoAnnotations.initMocks(this)
//        sut = TrackListFragment()
//        wrapperActivity.activity.supportFragmentManager.beginTransaction().add(sut, "SUT").commitNow()
//    }
//
//    @Test
//    fun testResumeStartsPresenter() {
//        // Prepare
//        val sut = this.sut!!
//
//        // Execute
//        sut.onResume()
//
//        // Verify
//        Assert.assertNotNull(sut.tracksPresenter.context)
//    }
//
//    @Test
//    fun testPauseStopsPresenter() {
//        // Prepare
//        val sut = this.sut!!
//
//        // Execute
//        sut.onResume()
//
//        // Verify
//        Assert.assertNull(sut.tracksPresenter.context)
//    }
//
//    @Test
//    fun testTrackSelectionInvokesListener() {
//        // Prepare
//        val sut = this.sut!!
//        val mockListener = mock(TrackListFragment.Listener::class.java)
//        val uri = ContentUris.withAppendedId(TRACKS_URI, 2)
//        sut.listener = mockListener
//
//        // Execute
//        sut.onTrackSelected(uri)
//
//        // Verify
//        verify(mockListener).onTrackSelected(uri)
//    }
//}