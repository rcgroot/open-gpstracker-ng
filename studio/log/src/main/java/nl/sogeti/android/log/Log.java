/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2015 Sogeti Nederland B.V. All Rights Reserved.
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
package nl.sogeti.android.log;

import android.text.TextUtils;

import java.util.Locale;


/**
 * Single point of control for all logging to the turned on or off
 * <p/>
 * Use either String or Object as tag. Any non-string class will have its simple name used as tag name
 */
public class Log {
    public static final boolean DEBUG = true;
    private static final boolean ADD_LINE_NUMBERS = false;
    private static final boolean SHOW_THREAD_ID = true;

    private Log() {
    }

    private static String getTag(Object tag) {
        String tagName;
        if (tag instanceof String) {
            tagName = (String) tag;
        } else {
            tagName = tag.getClass().getSimpleName();
            if (TextUtils.isEmpty(tagName)) {
                tagName = tag.getClass().getCanonicalName();
            }

            if (TextUtils.isEmpty(tagName)) {
                tagName = tag.getClass().getName();
                if (tagName.contains(".")) {
                    tagName = tagName.substring(tagName.lastIndexOf(".") + 1);
                }
                if (tagName.contains("$")) {
                    tagName = tagName.substring(0, tagName.indexOf("$"));
                }
            }
        }
        if (tagName.length() > 23) {
            tagName = tagName.substring(0, 23);
        }
        return tagName;
    }

    private static String getFormattedMessage(Object tag, final String msg) {
        String result = msg;
        if (SHOW_THREAD_ID) {
            result = String.format(Locale.getDefault(), "(thread:%d) %s", Thread.currentThread().getId(), result);
        }

        if (ADD_LINE_NUMBERS) {
            int stackTraceLength = Thread.currentThread().getStackTrace().length;
            if (stackTraceLength > 0) {
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                StackTraceElement stackElement = null;

                for (int i = stackTraceLength - 1; i > 0; i--) {
                    if (stackTrace[i].getFileName().equals(tag + ".java")) {
                        stackElement = stackTrace[i];
                    }
                }
                if (stackElement != null) {
                    result = String.format(Locale.getDefault(), "%s (%s:%d)", result, stackElement.getFileName(),
                            stackElement.getLineNumber());
                }
            }
        }
        return result;
    }

    public static void v(final Object tagSource, final String msgSource) {
        if (DEBUG) {
            String tag = getTag(tagSource);
            String msg = getFormattedMessage(tag, msgSource);
            android.util.Log.v(tag, msg);
        }
    }

    public static void d(final Object tagSource, final String msgSource) {
        if (DEBUG) {
            String tag = getTag(tagSource);
            String msg = getFormattedMessage(tag, msgSource);
            android.util.Log.d(tag, msg);
        }
    }

    public static void d(final Object tagSource, final String msgSource, Object... args) {
        if (DEBUG) {
            String tag = getTag(tagSource);
            String msg = getFormattedMessage(tag, String.format(msgSource, (Object[]) args));
            android.util.Log.d(tag, msg);
        }
    }


    public static void d(final Object tagSource, final String msgSource, Exception e) {
        if (DEBUG) {
            String tag = getTag(tagSource);
            String msg = getFormattedMessage(tag, msgSource);
            android.util.Log.d(tag, msg, e);
        }
    }

    public static void i(final Object tagSource, final String msgSource) {
        if (DEBUG) {
            String tag = getTag(tagSource);
            String msg = getFormattedMessage(tag, msgSource);
            android.util.Log.i(tag, msg);
        }
    }

    public static void e(final Object tagSource, final String msgSource) {
        if (DEBUG) {
            String tag = getTag(tagSource);
            String msg = getFormattedMessage(tag, msgSource);
            android.util.Log.e(tag, msg);
        }
    }

    public static void e(final Object tagSource, final String msgSource, Throwable e) {
        if (DEBUG) {
            String tag = getTag(tagSource);
            String msg = getFormattedMessage(tag, msgSource);
            android.util.Log.e(tag, msg, e);
        }
    }

    public static void w(final Object tagSource, final String msgSource) {
        if (DEBUG) {
            String tag = getTag(tagSource);
            String msg = getFormattedMessage(tag, msgSource);
            android.util.Log.w(tag, msg);
        }
    }

    public static void w(final Object tagSource, final String msgSource, Throwable e) {
        if (DEBUG) {
            String tag = getTag(tagSource);
            String msg = getFormattedMessage(tag, msgSource);
            android.util.Log.w(tag, msg, e);
        }
    }
}
