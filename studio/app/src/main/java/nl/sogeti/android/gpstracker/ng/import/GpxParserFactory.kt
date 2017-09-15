package nl.sogeti.android.gpstracker.ng.import

import nl.renedegroot.android.test.xml_parsing.element
import nl.renedegroot.android.test.xml_parsing.xml
import kotlin.Int.Companion.MAX_VALUE


class GpxParserFactory {

    fun createParse() =
            xml {
                element("gpx") {
                    attribute("version"){}
                    attribute("creator"){}
                    element("metadata", 0) {
                        element("name", 0) {}
                        element("desc", 0) {}
                        element("author", 0) {}
                        element("copyright", 0) {}
                        element("link", 0, MAX_VALUE) {}
                        element("keywords", 0) {}
                        element("bounds", 0) {}
                        element("extensions", 0) {}
                    }
                    wptType("wpt")
                    element("rte") {}
                    element("trk") {}
                    element("trk") {}
                }
            }

    private fun wptType(tagName: String) = element(tagName) {
        attribute("lat") {}
        attribute("lon") {}
        element("ele", 0) {}
        element("time", 0) {}
        element("magvar",0) {}
        element("geoidheight",0) {}
        element("name",0) {}
        element("cmt",0) {}
        element("desc",0) {}
        element("src",0) {}
        element("link",0, MAX_VALUE) {}
        element("sym",0) {}
        element("type",0) {}
        element("fix",0) {}
        element("sat",0) {}
        element("hdop",0) {}
        element("vdop",0) {}
        element("pdop",0) {}
        element("ageofdgpsdata",0) {}
        element("dgpsid",0) {}
        element("extensions",0) {}
    }
}
