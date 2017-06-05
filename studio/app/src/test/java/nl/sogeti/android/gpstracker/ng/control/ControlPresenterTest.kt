/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2016 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced
 ** Distributed Software Engineering |  or transmitted in any form or by any
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the
 ** 4131 NJ Vianen                   |  purpose, without the express written
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of OpenGPSTracker.
 *
 *   OpenGPSTracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenGPSTracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nl.sogeti.android.gpstracker.ng.control

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import nl.sogeti.android.gpstracker.integration.ServiceConstants.*
import nl.sogeti.android.gpstracker.integration.ServiceManager
import nl.sogeti.android.gpstracker.ng.rules.MockAppComponentTestRule
import nl.sogeti.android.gpstracker.ng.rules.any
import nl.sogeti.android.gpstracker.ng.trackedit.NameGenerator
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import java.util.concurrent.Executor

class ControlPresenterTest {

    lateinit var sut: ControlPresenter
    @get:Rule
    var appComponentRule = MockAppComponentTestRule()
    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    lateinit var viewModel: ControlViewModel
    @Mock
    lateinit var serviceManager: ServiceManager
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var trackUri: Uri
    @Mock
    lateinit var nameGenerator: NameGenerator
    @Mock
    lateinit var resolver: ContentResolver

    @Before
    fun setup() {
        viewModel = ControlViewModel()
        sut = ControlPresenter(viewModel)
        sut.setServiceManager(serviceManager)
        sut.asyncExecutor = Executor { it.run() }
        sut.nameGenerator = nameGenerator
        `when`(nameGenerator.generateName(any(), any())).thenReturn("Name")
        `when`(context.contentResolver).thenReturn(resolver)
        sut.start(context)
    }

    @Test
    fun leftClickDuringUnknown() {
        // Arrange
        reset(serviceManager)
        viewModel.state.set(STATE_UNKNOWN)

        // Act
        sut.onClickLeft()

        // Assert
        verifyZeroInteractions(serviceManager)
    }

    @Test
    fun leftClickDuringStopped() {
        // Arrange
        reset(serviceManager)
        viewModel.state.set(STATE_STOPPED)

        // Act
        sut.onClickLeft()

        // Assert
        verifyZeroInteractions(serviceManager)
    }

    @Test
    fun leftClickDuringLogging() {
        // Arrange
        viewModel.state.set(STATE_LOGGING)

        // Act
        sut.onClickLeft()

        // Assert
        verify<ServiceManager>(serviceManager).stopGPSLogging(context)
    }

    @Test
    fun leftClickDuringPaused() {
        // Arrange
        viewModel.state.set(STATE_PAUSED)

        // Act
        sut.onClickLeft()

        // Assert
        verify<ServiceManager>(serviceManager).stopGPSLogging(context)
    }

    @Test
    fun rightClickDuringUnknown() {
        // Arrange
        reset(serviceManager)
        viewModel.state.set(STATE_UNKNOWN)

        // Act
        sut.onClickRight()

        // Assert
        verifyZeroInteractions(serviceManager)
    }

    @Test
    fun rightClickDuringStopped() {
        // Arrange
        viewModel.state.set(STATE_STOPPED)

        // Act
        sut.onClickRight()

        // Assert
        verify<ServiceManager>(serviceManager).startGPSLogging(context, null)
    }

    @Test
    fun rightClickDuringLogging() {
        // Arrange
        viewModel.state.set(STATE_LOGGING)

        // Act
        sut.onClickRight()

        // Assert
        verify<ServiceManager>(serviceManager).pauseGPSLogging(context)
    }

    @Test
    fun rightClickDuringPaused() {
        // Arrange
        viewModel.state.set(STATE_PAUSED)

        // Act
        sut.onClickRight()

        // Assert
        verify<ServiceManager>(serviceManager).resumeGPSLogging(context)
    }

    @Test
    fun connectService() {
        // Act
        sut.didConnectToService(trackUri, "somename", STATE_LOGGING)
        // Assert
        assertThat(viewModel.state.get(), `is`(STATE_LOGGING))
    }

    @Test
    fun changedService() {
        // Act
        sut.didChangeLoggingState(trackUri, "somename", STATE_PAUSED)
        // Assert
        assertThat(viewModel.state.get(), `is`(STATE_PAUSED))
    }

    @Test
    fun stopLoggingEmptyTrack() {
        // Arrange
        `when`(serviceManager.trackId).thenReturn(11L)
        val contentResolver = mock(ContentResolver::class.java)
        `when`(context.contentResolver).thenReturn(contentResolver)
        val cursor = mock(Cursor::class.java)
        `when`(cursor.moveToFirst()).thenReturn(false)
        `when`(contentResolver.query(any(), any(), any(), any(), any())).thenReturn(cursor)
        // Act
        sut.stopLogging(context)
        // Assert
        verify(contentResolver).delete(any(), any(), any())
    }

    @Test
    fun stopLoggingFilledTrack() {
        // Arrange
        `when`(serviceManager.trackId).thenReturn(11L)
        val contentResolver = mock(ContentResolver::class.java)
        `when`(context.contentResolver).thenReturn(contentResolver)
        val cursor = mock(Cursor::class.java)
        `when`(cursor.moveToFirst()).thenReturn(true)
        `when`(cursor.getLong(0)).thenReturn(23)
        `when`(contentResolver.query(any(), any(), any(), any(), any())).thenReturn(cursor)
        // Act
        sut.stopLogging(context)
        // Assert
        verify(contentResolver, never()).delete(any(), any(), any())
    }
}
