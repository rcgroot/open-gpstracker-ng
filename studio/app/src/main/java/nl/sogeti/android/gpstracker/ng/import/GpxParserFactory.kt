package nl.sogeti.android.gpstracker.ng.import

import nl.renedegroot.android.test.xml_parsing.element
import nl.renedegroot.android.test.xml_parsing.xml


class GpxParserFactory {

    fun createParse() =
            xml {
                element("gpx") {
                    element("metadata") {
                        element("name"){}
                        element("desc"){}
                        element("author"){}
                        element("copyright"){}
                        element("link"){}
                        element("keywords"){}
                        element("bounds"){}
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
        element("ele"){}
        element("time"){}
        element("magvar"){}
        element("extensions"){}
    }
}
