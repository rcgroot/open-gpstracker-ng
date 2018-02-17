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
package nl.sogeti.android.gpstracker.ng.features.tracklist

import android.content.Context
import android.net.Uri
import nl.renedegroot.android.test.utils.any
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentControllerFactory
import nl.sogeti.android.gpstracker.ng.features.tracklist.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.features.util.MockAppComponentTestRule
import nl.sogeti.android.gpstracker.ng.base.model.TrackSelection
import nl.sogeti.android.gpstracker.service.util.tracksUri
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import java.util.concurrent.Executor

class TrackListPresenterTest {

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    @get:Rule
    var appComponentRule = MockAppComponentTestRule()
    @Mock
    lateinit var view: TrackListViewModel.View
    lateinit var viewModel: TrackListViewModel
    @Mock
    lateinit var trackSelection: TrackSelection
    @Mock
    lateinit var contentControllerFactory: ContentControllerFactory
    @Mock
    lateinit var summaryManager: SummaryManager
    @Mock
    lateinit var contentController: ContentController
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var executor: Executor
    @Mock
    lateinit var navigation: TrackListNavigation
    @Mock
    lateinit var notification: ImportNotification
    lateinit var sut: TrackListPresenter

    @Before
    fun setUp() {
        viewModel = TrackListViewModel()
        sut = TrackListPresenter(viewModel, view, navigation)
        sut.trackSelection = trackSelection
        sut.contentControllerFactory = contentControllerFactory
        sut.summaryManager = summaryManager
        sut.executor = executor
        sut.notification = notification
        `when`(contentControllerFactory.createContentController(any(), any()))
                .thenReturn(contentController)
    }

    @Test
    fun testStart() {
        // Act
        sut.start(context)
        // Assert
        verify(summaryManager).start()
        verify(contentController).registerObserver(tracksUri())
    }

    @Test
    fun testStop() {
        // Arrange
        sut.start(context)
        // Act
        sut.willStop()
        // Assert
        verify(contentController).unregisterObserver()
    }

    @Test
    fun testTrackSelection() {
        // Arrange
        val selectedUri = mock(Uri::class.java)
        sut.start(context)
        // Act
        sut.didSelectTrack(selectedUri, "testname")
        // Assert
        verify(trackSelection).selectTrack(selectedUri, "testname")
        verify(navigation).finishTrackSelection()
    }
}
