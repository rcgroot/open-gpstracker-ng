package nl.sogeti.android.gpstracker.ng.tracks.summary

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class SummaryManagerTest {

    val sut = summaryManager

    @Before
    fun tearDown() {
        while (sut.isRunning()) {
            sut.stop()
        }
    }

    @Test
    fun testStartSetsRunning() {
        // Execute
        sut.start()

        // Verify
        assertTrue(sut.isRunning())
    }

    @Test
    fun testBeginsStopped() {
        // Verify
        assertFalse(sut.isRunning())
    }

    @Test
    fun testTwiceStartSingleStop() {
        // Execute
        sut.start()
        sut.start()
        sut.stop()

        // Verify
        assertTrue(sut.isRunning())
    }

    @Test
    fun testStopStoppedIlligal(){
        // Prepare
        var caught : IllegalStateException? = null

        // Execute
        try {
            sut.stop()
        }
        catch (e:IllegalStateException) {
            caught = e
        }

        // Verify
        assertNotNull(caught)
    }



}