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
package nl.sogeti.android.gpstracker.ng.features.trackedit

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.view.View
import android.widget.AdapterView
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.features.util.MockAppComponentTestRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit

class TrackEditPresenterTest {

    lateinit var sut: TrackEditPresenter
    @get:Rule
    val mockitoRule = MockitoJUnit.rule()
    @get:Rule
    var appComponentRule = MockAppComponentTestRule()
    @Mock
    lateinit var trackUri: Uri
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var contentResolver: ContentResolver
    @Mock
    lateinit var summaryManager: SummaryManager
    @Mock
    lateinit var cursor: Cursor
    @Mock
    lateinit var contentController: ContentController

    @Before
    fun setUp() {
        sut = TrackEditPresenter(summaryManager, contentController)
        `when`(trackUri.lastPathSegment).thenReturn("6")
        sut.viewModel.selectedPosition.set(1)
        `when`(context.contentResolver).thenReturn(contentResolver)
        `when`(contentResolver.query(any(), any(), any(), any(), any())).thenReturn(cursor)
    }

    @Test
    fun testOk() {
        // Arrange
        sut.start()
        sut.viewModel.selectedPosition.set(1)
        // Act
        sut.ok(trackUri, "mockName")
        // Assert
        assertThat(sut.viewModel.dismissed.get(), `is`(true))
        verify(summaryManager).removeFromCache(trackUri)
    }

    @Test
    fun testCancel() {
        // Act
        sut.cancel()
        // Assert
        assertThat(sut.viewModel.dismissed.get(), `is`(true))
    }

    @Test
    fun testSelect() {
        // Arrange
        val adapterView = mock(AdapterView::class.java)
        val view = mock(View::class.java)
        // Act
        sut.onItemSelectedListener.onItemSelected(adapterView, view, 4, 5)
        // Assert
        assertThat(sut.viewModel.selectedPosition.get(), `is`(4))
    }

    @Test
    fun testSelectNothing() {
        // Arrange
        val adapterView = mock(AdapterView::class.java)
        // Act
        sut.onItemSelectedListener.onNothingSelected(adapterView)
        // Assert
        assertThat(sut.viewModel.selectedPosition.get(), `is`(-1))
    }
}
