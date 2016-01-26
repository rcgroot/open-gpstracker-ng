package nl.sogeti.android.gpstracker.data;


import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import static nl.sogeti.android.gpstracker.integration.ExternalConstants.STATE_UNKNOWN;

public class TrackTest {

    private Track sut;

    @Before
    public void setup() {
        sut = new Track("TestCase");
    }

    @Test
    public void testInit() {
        // Verify
        Assert.assertEquals("TestCase", sut.getName());
        Assert.assertEquals(STATE_UNKNOWN, sut.getState());
    }

    @Test
    public void testName() {
        // Execute
        sut.setName("Test");

        // Verify
        Assert.assertEquals("Test", sut.getName());
    }

    @Test
    public void testState() {
        // Execute
        sut.setState(88);

        // Verify
        Assert.assertEquals(88, sut.getState());
    }
}
