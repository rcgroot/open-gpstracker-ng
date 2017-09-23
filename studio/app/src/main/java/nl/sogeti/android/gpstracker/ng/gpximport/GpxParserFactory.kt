/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
-
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
package nl.sogeti.android.gpstracker.ng.gpximport

import android.content.ContentValues
import nl.renedegroot.android.test.xml_parsing.Element
import nl.renedegroot.android.test.xml_parsing.xml
import kotlin.Int.Companion.MAX_VALUE

class GpxParserFactory {

    fun createParse(contentValues: MutableList<ContentValues>) =
            xml {
                element("gpx") {
                    attribute("version") {}
                    attribute("creator") {}
                    element("metadata", 0) {
                        element("name", 0) {}
                        element("desc", 0) {}
                        element("author", 0) {}
                        element("copyright", 0) {}
                        element("link", 0, MAX_VALUE) {}
                        element("time", 0) {}
                        element("keywords", 0) {}
                        element("bounds", 0) {}
                        ignore("extensions", 0)
                    }
                    wptType("wpt", 0, MAX_VALUE)
                    rteType("rte", 0, MAX_VALUE)
                    trkType("trk", 0, MAX_VALUE)
                    ignore("extensions", 0)
                }
            }

    private fun Element.trkType(name: String, minOccurs: Int = 1, maxOccurs: Int = 1) = element(name, minOccurs, maxOccurs) {
        element("name", 0) {}
        element("cmt", 0) {}
        element("desc", 0) {}
        element("src", 0) {}
        element("link", 0, MAX_VALUE) {}
        element("number", 0) {}
        element("type", 0) {}
        ignore("extensions", 0)
        element("trkseg", 0, MAX_VALUE) {
            wptType("trkpt", 0, MAX_VALUE)
            ignore("extensions", 0)
        }
    }

    private fun Element.rteType(name: String, minOccurs: Int = 1, maxOccurs: Int = 1) = element(name, minOccurs, maxOccurs) {
        element("name", 0) {}
        element("cmt", 0) {}
        element("desc", 0) {}
        element("src", 0) {}
        element("link", 0, MAX_VALUE) {}
        element("number", 0) {}
        element("type", 0) {}
        ignore("extensions", 0)
        wptType("wpt", 0, MAX_VALUE)
    }

    private fun Element.wptType(name: String, minOccurs: Int = 1, maxOccurs: Int = 1) = element(name, minOccurs, maxOccurs) {
        attribute("lat") {}
        attribute("lon") {}
        element("ele", 0) {}
        element("time", 0) {}
        element("magvar", 0) {}
        element("geoidheight", 0) {}
        element("name", 0) {}
        element("cmt", 0) {}
        element("desc", 0) {}
        element("src", 0) {}
        element("link", 0, MAX_VALUE) {}
        element("sym", 0) {}
        element("type", 0) {}
        element("fix", 0) {}
        element("sat", 0) {}
        element("hdop", 0) {}
        element("vdop", 0) {}
        element("pdop", 0) {}
        element("ageofdgpsdata", 0) {}
        element("dgpsid", 0) {}
        ignore("extensions", 0)
    }
}
