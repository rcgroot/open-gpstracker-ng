package nl.sogeti.android.gpstracker.control;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.ViewGroup;

import org.junit.Before;
import org.junit.Test;
import static nl.sogeti.android.gpstracker.integration.ExternalConstants.STATE_LOGGING;
import static nl.sogeti.android.gpstracker.integration.ExternalConstants.STATE_PAUSED;
import static nl.sogeti.android.gpstracker.integration.ExternalConstants.STATE_STOPPED;
import static nl.sogeti.android.gpstracker.integration.ExternalConstants.STATE_UNKNOWN;

import org.mockito.Mockito;
import nl.sogeti.android.gpstracker.data.Track;

public class ControlHandlerTest {

    private Track mockTrack;
    private ControlHandler.Listener mockListener;
    private ControlHandler sut;

    @Before
    public void setup() {
        mockTrack = Mockito.mock(Track.class);
        mockListener = Mockito.mock(ControlHandler.Listener.class);
        sut = new ControlHandler(mockListener, mockTrack);
    }

    @Test
    public void setStateUnknown() {
        // Prepare
        ViewGroup container = createButtonViewGroup();

        // Execute
        sut.setState(container, STATE_UNKNOWN);

        // Verify
        Mockito.verify(container.getChildAt(0)).setVisibility(View.GONE);
        Mockito.verify(container.getChildAt(1)).setVisibility(View.VISIBLE);
        Mockito.verify(container.getChildAt(1)).setEnabled(false);
    }

    @Test
    public void setStateStopped() {
        // Prepare
        ViewGroup container = createButtonViewGroup();

        // Execute
        sut.setState(container, STATE_STOPPED);

        // Verify
        Mockito.verify(container.getChildAt(0)).setVisibility(View.GONE);
        Mockito.verify(container.getChildAt(1)).setVisibility(View.VISIBLE);
        Mockito.verify(container.getChildAt(1)).setEnabled(true);
    }

    @Test
    public void setStatePause() {
        // Prepare
        ViewGroup container = createButtonViewGroup();

        // Execute
        sut.setState(container, STATE_PAUSED);

        // Verify
        Mockito.verify(container.getChildAt(0)).setVisibility(View.VISIBLE);
        Mockito.verify(container.getChildAt(1)).setVisibility(View.VISIBLE);
        Mockito.verify(container.getChildAt(1)).setEnabled(true);
    }

    @Test
    public void setStateLogging() {
        // Prepare
        ViewGroup container = createButtonViewGroup();

        // Execute
        sut.setState(container, STATE_LOGGING);

        // Verify
        Mockito.verify(container.getChildAt(0)).setVisibility(View.VISIBLE);
        Mockito.verify(container.getChildAt(1)).setVisibility(View.VISIBLE);
        Mockito.verify(container.getChildAt(1)).setEnabled(true);
    }

    @Test
    public void leftClickDuringUnknown() {
        // Prepare
        Mockito.when(mockTrack.getState()).thenReturn(STATE_UNKNOWN);

        // Execute
        sut.onClickLeft(null);

        // Verify
        Mockito.verifyZeroInteractions(mockListener);
    }

    @Test
    public void leftClickDuringStopped() {
        // Prepare
        Mockito.when(mockTrack.getState()).thenReturn(STATE_STOPPED);

        // Execute
        sut.onClickLeft(null);

        // Verify
        Mockito.verifyZeroInteractions(mockListener);
    }

    @Test
    public void leftClickDuringLogging() {
        // Prepare
        Mockito.when(mockTrack.getState()).thenReturn(STATE_LOGGING);

        // Execute
        sut.onClickLeft(null);

        // Verify
        Mockito.verify(mockListener).stopLogging();
    }

    @Test
    public void leftClickDuringPaused() {
        // Prepare
        Mockito.when(mockTrack.getState()).thenReturn(STATE_PAUSED);

        // Execute
        sut.onClickLeft(null);

        // Verify
        Mockito.verify(mockListener).stopLogging();
    }

    @Test
    public void rightClickDuringUnknown() {
        // Prepare
        Mockito.when(mockTrack.getState()).thenReturn(STATE_UNKNOWN);

        // Execute
        sut.onClickRight(null);

        // Verify
        Mockito.verifyZeroInteractions(mockListener);
    }

    @Test
    public void rightClickDuringStopped() {
        // Prepare
        Mockito.when(mockTrack.getState()).thenReturn(STATE_STOPPED);

        // Execute
        sut.onClickRight(null);

        // Verify
        Mockito.verify(mockListener).startLogging();
    }

    @Test
    public void rightClickDuringLogging() {
        // Prepare
        Mockito.when(mockTrack.getState()).thenReturn(STATE_LOGGING);

        // Execute
        sut.onClickRight(null);

        // Verify
        Mockito.verify(mockListener).pauseLogging();
    }

    @Test
    public void rightClickDuringPaused() {
        // Prepare
        Mockito.when(mockTrack.getState()).thenReturn(STATE_PAUSED);

        // Execute
        sut.onClickRight(null);

        // Verify
        Mockito.verify(mockListener).resumeLogging();
    }

    /* Helpers */

    @NonNull
    private ViewGroup createButtonViewGroup() {
        ViewGroup container = Mockito.mock(ViewGroup.class);
        FloatingActionButton left = Mockito.mock(FloatingActionButton.class);
        FloatingActionButton right = Mockito.mock(FloatingActionButton.class);
        Mockito.when(container.getChildAt(0)).thenReturn(left);
        Mockito.when(container.getChildAt(1)).thenReturn(right);

        return container;
    }


}
