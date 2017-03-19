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
package nl.sogeti.android.gpstracker.ng.track.map

import android.content.Context
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_LOGGING
import nl.sogeti.android.gpstracker.integration.ServiceManagerInterface
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentControllerFactory
import nl.sogeti.android.gpstracker.ng.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.rules.MockAppComponentTestRule
import nl.sogeti.android.gpstracker.ng.utils.DefaultResultHandler
import nl.sogeti.android.gpstracker.ng.utils.Waypoint
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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

    @Before
    fun setup() {
        viewModel = TrackMapViewModel()
        sut = TrackMapPresenter(viewModel)
        Mockito.`when`(contentControllerFactory.createContentController(nl.sogeti.android.gpstracker.ng.rules.any(), nl.sogeti.android.gpstracker.ng.rules.any())).thenReturn(contentController)
        sut.contentControllerFactory = contentControllerFactory
        sut.serviceManager = serviceManager
        `when`(trackSelection.trackUri).thenReturn(trackUri)
        `when`(trackSelection.trackName).thenReturn("selected")
        sut.trackSelection = trackSelection
        sut.context = context
    }

    @Test
    fun testStart() {
        // Act
        sut.didStart()
        // Assert
        verify(trackSelection).addListener(sut)
        verify(contentController).registerObserver(trackUri)
    }

    @Test
    fun testStop() {
        // Arrange
        sut.didStart()
        // Act
        sut.willStop()
        // Assert
        verify(trackSelection).removeListener(sut)
    }

    @Test
    fun didConnectLogging() {
        // Act
        sut.didSelectTrack(trackUri, "somename")
        sut.didConnectToService(trackUri, "somename", STATE_LOGGING)
        // Assert
        assertThat(viewModel.isRecording.get(), `is`(true))
        assertThat(viewModel.name.get(), `is`("somename"))
    }

    @Test
    fun didConnectLoggingOtherTrack() {
        // Act
        val otherUri = mock(Uri::class.java)
        sut.didSelectTrack(otherUri, "othername")
        sut.didConnectToService(trackUri, "somename", STATE_LOGGING)
        // Assert
        assertThat(viewModel.isRecording.get(), `is`(false))
        assertThat(viewModel.name.get(), `is`("othername"))
    }

    @Test
    fun startedRecording() {
        // Arrange
        val otherUri = mock(Uri::class.java)
        sut.didSelectTrack(otherUri, "othername")
        // Act
        sut.didChangeLoggingState(trackUri, "somename", STATE_LOGGING)
        // Assert
        assertThat(viewModel.isRecording.get(), `is`(true))
        verify(trackSelection).selectTrack(trackUri, "somename")
    }

    @Test
    fun testContentChange() {
        // Act
        sut.onChangeUriContent(trackUri, trackUri)
        // TODO
    }

    @Test
    fun testSetName() {
        // Arrange
        val uri = mock(Uri::class.java)
        val reader = TrackReader(context, uri, viewModel)
        `when`(handler.name).thenReturn("Input")
        // Act
        reader.updateViewModelWithHandler(handler)
        // Assert
        Assert.assertThat(viewModel.name.get(), `is`("Input"))
    }

    @Test
    fun testSetSingleListOfWaypoints() {
        // Arrange
        val uri = mock(Uri::class.java)
        val reader = TrackReader(context, uri, viewModel)
        val waypoint = Waypoint(latitude = 1.2, longitude = 3.4, time = 1)
        `when`(handler.waypoints).thenReturn(mutableListOf(mutableListOf(waypoint, waypoint)))
        // Act
        reader.updateViewModelWithHandler(handler)
        // Assert
        Assert.assertThat(viewModel.waypoints.get()[0][0].latitude, `is`(1.2))
        Assert.assertThat(viewModel.waypoints.get()[0][0].longitude, `is`(3.4))
    }

    @Test
    fun testSetSingleEmptyList() {
        // Arrange
        val uri = mock(Uri::class.java)
        val reader = TrackReader(context, uri, viewModel)
        `when`(handler.waypoints).thenReturn(mutableListOf<MutableList<Waypoint>>())
        // Act
        reader.updateViewModelWithHandler(handler)
        // Assert
        Assert.assertThat(viewModel.waypoints.get().count(), `is`(0))
    }

    @Test
    fun testSetSingleEmptyListOfWaypoints() {
        // Arrange
        val uri = mock(Uri::class.java)
        val reader = TrackReader(context, uri, viewModel)
        `when`(handler.waypoints).thenReturn(mutableListOf(mutableListOf<Waypoint>()))
        // Act
        reader.updateViewModelWithHandler(handler)
        // Assert
        Assert.assertThat(viewModel.waypoints.get().count(), `is`(0))
    }


    @Test
    fun testSetSingleEmptyFullCombinatie() {
        // Arrange
        val uri = mock(Uri::class.java)
        val reader = TrackReader(context, uri, viewModel)
        val empty = mutableListOf<Waypoint>()
        val waypoint = Waypoint(latitude = 1.2, longitude = 3.4, time = 1)
        val items = mutableListOf(waypoint, waypoint)
        `when`(handler.waypoints).thenReturn(mutableListOf(empty, items))
        // Act
        reader.updateViewModelWithHandler(handler)
        // Assert
        Assert.assertThat(viewModel.waypoints.get().count(), `is`(1))
        Assert.assertThat(viewModel.waypoints.get()[0].count(), `is`(2))
    }
}