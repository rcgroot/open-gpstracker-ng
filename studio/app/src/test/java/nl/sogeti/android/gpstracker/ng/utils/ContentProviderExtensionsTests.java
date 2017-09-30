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
package nl.sogeti.android.gpstracker.ng.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoRule;

import java.util.List;

import kotlin.jvm.functions.Function1;
import nl.sogeti.android.gpstracker.ng.utils.ContentProviderExtensionsKt;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ContentProviderExtensionsTests {

    @Mock
    Cursor mockCursor;
    @Mock
    Uri mockUri;
    @Mock
    Context mockContext;
    @Mock
    ContentResolver mockContentResolver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        given(mockCursor.getColumnIndex("A_NAME")).willReturn(0);
        given(mockCursor.getColumnIndex("AN_OTHER_NAME")).willReturn(1);
        given(mockCursor.getColumnIndex("NULL_VALUE")).willReturn(2);
        given(mockCursor.getColumnIndex("NOT_EXIST")).willReturn(-1);
        given(mockCursor.getString(0)).willReturn("FirstValue");
        given(mockCursor.getString(1)).willReturn("SecondValue");
        given(mockCursor.getString(2)).willReturn(null);
        given(mockContext.getContentResolver()).willReturn(mockContentResolver);
    }

    @Test
    public void getAStringFromCursor() {
        // Execute
        String value = ContentProviderExtensionsKt.getString(mockCursor, "A_NAME");

        // Verify
        verify(mockCursor).getColumnIndex("A_NAME");
        verify(mockCursor).getString(0);
        Assert.assertEquals(value, "FirstValue");
    }

    @Test
    public void getAnOtherStringFromCursor() {
        // Execute
        String value = ContentProviderExtensionsKt.getString(mockCursor, "AN_OTHER_NAME");

        // Verify
        verify(mockCursor).getColumnIndex("AN_OTHER_NAME");
        verify(mockCursor).getString(1);
        Assert.assertEquals(value, "SecondValue");
    }

    @Test
    public void getAnNullStringFromCursor() {
        // Execute
        String value = ContentProviderExtensionsKt.getString(mockCursor, "NULL_VALUE");

        // Verify
        verify(mockCursor).getColumnIndex("NULL_VALUE");
        verify(mockCursor).getString(2);
        Assert.assertEquals(value, null);
    }

    @Test
    public void nullCursorMap() {
        // Prepare
        final int invoke = 0;
        given(mockContentResolver.query(mockUri, null, null, null, null)).willReturn(null);

        // Execute
        List<String> list = ContentProviderExtensionsKt.map(mockUri, mockContext, null, null, new Function1<Cursor, String>() {
            @Override
            public String invoke(Cursor cursor) {
                return "invoke_" + invoke;
            }
        });

        // Verify
        Assert.assertNotNull(list);
        Assert.assertEquals(list.size(), 0);
    }

    @Test
    public void emptyCursorMap() {
        // Prepare
        final int invoke = 0;
        given(mockContentResolver.query(mockUri, null, null, null, null)).willReturn(mockCursor);
        given(mockCursor.moveToFirst()).willReturn(false);

        // Execute
        List<String> list = ContentProviderExtensionsKt.map(mockUri, mockContext, null, null, new Function1<Cursor, String>() {
            @Override
            public String invoke(Cursor cursor) {
                return "invoke_" + invoke;
            }
        });

        // Verify
        Assert.assertNotNull(list);
        Assert.assertEquals(list.size(), 0);
        verify(mockCursor).close();
    }

    @Test
    public void SingleElementCursorMap() {
        // Prepare
        final int[] invoke = {0};
        given(mockContentResolver.query(mockUri, null, null, null, null)).willReturn(mockCursor);
        given(mockCursor.moveToFirst()).willReturn(true);
        given(mockCursor.moveToNext()).willReturn(false);

        // Execute
        List<String> list = ContentProviderExtensionsKt.map(mockUri, mockContext, null, null, new Function1<Cursor, String>() {
            @Override
            public String invoke(Cursor cursor) {
                invoke[0]++;
                return "invoke_" + invoke[0];
            }
        });

        // Verify
        Assert.assertNotNull(list);
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(invoke[0], 1);
        Assert.assertEquals(list.get(0), "invoke_1");
        verify(mockCursor).close();
    }

    @Test
    public void getAStringFromNonExistingCursor() {
        // Execute
        String value = ContentProviderExtensionsKt.getString(mockCursor, "NOT_EXIST");

        // Verify
        verify(mockCursor).getColumnIndex("NOT_EXIST");
        verify(mockCursor, times(0)).getString(anyInt());
        Assert.assertEquals(value, null);
    }
}
