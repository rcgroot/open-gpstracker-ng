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
package nl.sogeti.android.gpstracker.ng.features.map

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import nl.renedegroot.android.test.utils.any
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.base.location.LocationFactory
import nl.sogeti.android.gpstracker.ng.base.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.features.map.rendering.TrackTileProvider
import nl.sogeti.android.gpstracker.ng.features.map.rendering.TrackTileProviderFactory
import nl.sogeti.android.gpstracker.ng.features.util.LoggingStateController
import nl.sogeti.android.gpstracker.ng.features.util.MockAppComponentTestRule
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants.STATE_LOGGING
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit

class TrackMapPresenterTest {

    lateinit var sut: TrackMapPresenter
    @get:Rule
    var mockitoRule = MockitoJUnit.rule()!!
    @get:Rule
    var appComponentRule = MockAppComponentTestRule()
    @Mock
    lateinit var contentController: ContentController
    @Mock
    lateinit var trackUri: Uri
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
    @Mock
    lateinit var locationFactory: LocationFactory
    @Mock
    lateinit var loggingStateController: LoggingStateController

    @Before
    fun setup() {
        sut = TrackMapPresenter(trackReaderFactory, trackTileProviderFactory, locationFactory, loggingStateController, trackSelection, contentController)
        `when`(trackSelection.trackUri).thenReturn(trackUri)
        `when`(trackSelection.trackName).thenReturn("selected")
        `when`(trackReaderFactory.createTrackReader(any(), any())).thenReturn(trackReader)

        `when`(trackTileProviderFactory.createTrackTileProvider(any(), any())).thenReturn(trackTileProvider)
        sut.start()
    }

    @Test
    fun testStart() {
        // Assert
        verify(trackSelection).addListener(sut)
    }

    @Test
    fun testRegistration() {
        // Act
        sut.trackUri = trackUri
        // Assert
        verify(contentController).registerObserver(trackUri)
    }


    @Test
    fun testStop() {
        // Act
        sut.onCleared()
        // Assert
        verify(trackSelection).removeListener(sut)
    }

    @Test
    fun startedRecording() {
        // Arrange
        val otherUri = mock(Uri::class.java)
        sut.onTrackSelection(otherUri, "othername")
        // Act
        sut.didChangeLoggingState(context, trackUri, "somename", STATE_LOGGING)
        // Assert
        assertThat(sut.recordingUri, `is`(trackUri))
        verify(trackSelection).selectTrack(trackUri, "somename")
    }
}
