/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
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
package nl.sogeti.android.gpstracker.ng.map

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_LOGGING
import nl.sogeti.android.gpstracker.integration.ServiceManagerInterface
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentControllerFactory
import nl.sogeti.android.gpstracker.ng.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.rules.MockAppComponentTestRule
import nl.sogeti.android.gpstracker.ng.rules.any
import nl.sogeti.android.gpstracker.ng.map.rendering.TrackTileProvider
import nl.sogeti.android.gpstracker.ng.map.rendering.TrackTileProviderFactory
import nl.sogeti.android.gpstracker.ng.utils.DefaultResultHandler
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit

class TrackMapPresenterTest {

    lateinit var sut: TrackMapPresenter
    lateinit var viewModel: TrackMapViewModel
    @get:Rule
    var mockitoRule = MockitoJUnit.rule()!!
    @get:Rule
    var appComponentRule = MockAppComponentTestRule()
    @Mock
    lateinit var handler: DefaultResultHandler
    @Mock
    lateinit var contentController: ContentController
    @Mock
    lateinit var contentControllerFactory: ContentControllerFactory
    @Mock
    lateinit var trackUri: Uri
    @Mock
    lateinit var serviceManager: ServiceManagerInterface
    @Mock
    lateinit var trackSelection: TrackSelection
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var trackReaderFactory: TrackReaderFactory
    @Mock
    lateinit var trackReader: TrackReader
    @Mock
    lateinit var trackTileProviderFactory: TrackTileProviderFactory
    @Mock
    lateinit var trackTileProvider: TrackTileProvider

    @Before
    fun setup() {
        viewModel = TrackMapViewModel()
        sut = TrackMapPresenter(viewModel)
        sut.serviceManager = serviceManager
        `when`(trackSelection.trackUri).thenReturn(trackUri)
        `when`(trackSelection.trackName).thenReturn("selected")
        sut.trackSelection = trackSelection
        `when`(trackReaderFactory.createTrackReader(any(), any(), any())).thenReturn(trackReader)
        sut.trackReaderFactory = trackReaderFactory
        sut.contentControllerFactory = contentControllerFactory

        Mockito.`when`(contentControllerFactory.createContentController(nl.sogeti.android.gpstracker.ng.rules.any(), nl.sogeti.android.gpstracker.ng.rules.any())).thenReturn(contentController)
        `when`(trackTileProviderFactory.createTrackTileProvider(any(), any())).thenReturn(trackTileProvider)
        sut.trackTileProviderFactory = trackTileProviderFactory
        sut.start(context)
    }

    @Test
    fun testStart() {
        // Act
        sut.start(context)
        // Assert
        verify(trackSelection).addListener(sut)
        verify(contentController).registerObserver(trackUri)
    }

    @Test
    fun testStop() {
        // Act
        sut.willStop()
        // Assert
        verify(trackSelection).removeListener(sut)
    }

    @Test
    fun didConnectLogging() {
        // Act
        sut.onTrackSelection(trackUri, "somename")
        sut.didConnectToService(trackUri, "somename", STATE_LOGGING)
        // Assert
        assertThat(sut.recordingUri, `is`(trackUri))
        assertThat(viewModel.name.get(), `is`("somename"))
    }

    @Test
    fun didConnectLoggingOtherTrack() {
        // Act
        val otherUri = mock(Uri::class.java)
        sut.onTrackSelection(otherUri, "othername")
        sut.didConnectToService(trackUri, "somename", STATE_LOGGING)
        // Assert
        assertThat(sut.recordingUri, `is`(trackUri))
        assertThat(viewModel.name.get(), `is`("othername"))
    }

    @Test
    fun startedRecording() {
        // Arrange
        val otherUri = mock(Uri::class.java)
        sut.onTrackSelection(otherUri, "othername")
        // Act
        sut.didChangeLoggingState(trackUri, "somename", STATE_LOGGING)
        // Assert
        assertThat(sut.recordingUri, `is`(trackUri))
        verify(trackSelection).selectTrack(trackUri, "somename")
    }

    @Test
    fun testContentChange() {
        // Arrange
        reset(trackReaderFactory)
        `when`(trackReaderFactory.createTrackReader(any(), any(), any())).thenReturn(trackReader)
        // Act
        sut.onChangeUriContent(trackUri, trackUri)
        // Assert
        verify(trackReaderFactory).createTrackReader(any(), any(), any())
    }

    @Test
    fun trackSelection() {
        // Arrange
        val trackSelection = mock(TrackSelection::class.java)
        sut.trackSelection = trackSelection
        val contentResolver = mock(ContentResolver::class.java)
        `when`(context.contentResolver).thenReturn(contentResolver)
        val cursor = mock(Cursor::class.java)
        `when`(cursor.moveToFirst()).thenReturn(true)
        mockColumn(cursor, "_ID", 1, 2)
        mockColumn(cursor, "name", 2, "cursorname")
        `when`(contentResolver.query(any(), any(), any(), any(), any())).thenReturn(cursor)
        // Act
        sut.didStart()
        // Assert
        verify(trackSelection).selectTrack(any(), ArgumentMatchers.matches("cursorname"))
    }

    private fun mockColumn(cursor: Cursor, name: String, i: Int, value: Long) {
        `when`(cursor.getColumnIndex(name)).thenReturn(i)
        `when`(cursor.getLong(i)).thenReturn(value)
    }

    private fun mockColumn(cursor: Cursor, name: String, i: Int, value: String) {
        `when`(cursor.getColumnIndex(name)).thenReturn(i)
        `when`(cursor.getString(i)).thenReturn(value)
    }
}
