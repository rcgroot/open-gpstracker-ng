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
package nl.renedegroot.android.test.xml_parsing

import nl.renedegroot.android.test.utils.withResources
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedInputStream
import java.io.InputStream
import java.lang.Exception

fun xml(init: XML.() -> Unit): XML {
    val xml = XML()
    xml.init()
    return xml
}

fun element(name: ElementName, minOccurs: Int = 1, maxOccurs: Int = 1, init: Element.() -> Unit): Element {
    val tag = Element(name, minOccurs, maxOccurs)
    tag.init()
    return tag
}

@XmlParseDslMarker
class XML {
    private var root: Element? = null

    fun element(name: ElementName, minOccurs: Int = 1, maxOccurs: Int = 1, init: Element.() -> Unit): Element {
        val tag = Element(name, minOccurs, maxOccurs)
        tag.init()
        root = tag
        return tag
    }

    fun parse(inputStream: InputStream) {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val xmlParser = factory.newPullParser()
        withResources {
            inputStream.use()
            val bis = BufferedInputStream(inputStream).use()
            val ur = UnicodeReader(bis, "UTF-8").use()
            xmlParser.setInput(ur)

            var next = 0
            while (next != XmlPullParser.START_DOCUMENT) {
                next = xmlParser.next()
            }

            root?.parse(xmlParser, xmlParser.next())

            while (next != XmlPullParser.END_DOCUMENT) {
                next = xmlParser.next()
            }
        }
    }
}

class Element(private val name: ElementName, private var minOccurs: Int, private var maxOccurs: Int) : Parser {
    private val tags = mutableListOf<Parser>()
    private val attributes = mutableListOf<Attribute>()
    private var text: Text = Text {}
    private var occurred = 0

    fun element(name: String, minOccurs: Int = 1, maxOccurs: Int = 1, init: Element.() -> Unit): Element {
        val tag = Element(name, minOccurs, maxOccurs)
        tag.init()
        tags.add(tag)
        return tag
    }

    fun attribute(name: String, action: (String) -> Unit): Attribute {
        val attribute = Attribute(name, action)
        attributes.add(attribute)
        return attribute
    }

    fun text(action: (String) -> Unit): Text {
        val text = Text(action)
        this.text = text
        return text
    }

    fun ignore(name: String, minOccurs: Int = 1, maxOccurs: Int = 1): IgnoreElement {
        val tag = IgnoreElement(name, minOccurs, maxOccurs)
        tags.add(tag)
        return tag
    }

    override fun matches(xmlParser: XmlPullParser, next: Int): Boolean {
        return occurred < minOccurs || (occurred < maxOccurs && xmlParser.name == name)
    }

    override fun parse(xmlParser: XmlPullParser, firstNext: Int) {
        var next = firstNext
        if (next == XmlPullParser.START_TAG && xmlParser.name == name) {
            xmlParser.parseAttributes(attributes)

            next = xmlParser.nextIgnoringWhiteSpace()
            tags.forEach {
                while (it.matches(xmlParser, next)) {
                    it.parse(xmlParser, next)
                    next = xmlParser.nextIgnoringWhiteSpace()
                }
            }
            if (text.matches(xmlParser, next)) {
                text.parse(xmlParser, next)
                next = xmlParser.next()
            }
        } else {
            throw XmlParseException(buildErrorMessage(xmlParser, "START_TAG", name, next))
        }

        if (next != XmlPullParser.END_TAG || xmlParser.name != name) {
            throw XmlParseException(buildErrorMessage(xmlParser, "END_TAG", name, next))
        }
        occurred++
    }
}

class IgnoreElement(private val name: ElementName, private var minOccurs: Int, private var maxOccurs: Int) : Parser {
    private var occurred = 0

    override fun matches(xmlParser: XmlPullParser, next: Int): Boolean {
        return occurred < minOccurs || (occurred < maxOccurs && xmlParser.name == name)
    }

    override fun parse(xmlParser: XmlPullParser, firstNext: Int) {
        var next = firstNext
        if (next == XmlPullParser.START_TAG && xmlParser.name == name) {
            while (!(xmlParser.name == name && next == XmlPullParser.END_TAG)) {
                next = xmlParser.next()
            }
        } else {
            throw XmlParseException(buildErrorMessage(xmlParser, "START_TAG", name, next))
        }

        if (next != XmlPullParser.END_TAG || xmlParser.name != name) {
            throw XmlParseException(buildErrorMessage(xmlParser, "END_TAG", name, next))
        }
        occurred++

    }
}

private fun buildErrorMessage(xmlParser: XmlPullParser, expectedEvent: String, expectedString: String, next: Int) =
        "At line ${xmlParser.lineNumber} expected to find $expectedEvent of '$expectedString' but found '${xmlParser.state(next)}'"

@XmlParseDslMarker
class Attribute(val name: AttributeName, val action: (String) -> Unit)

class Text(private val action: (String) -> Unit) : Parser {

    override fun matches(xmlParser: XmlPullParser, next: Int): Boolean {
        return next == XmlPullParser.TEXT
    }

    override fun parse(xmlParser: XmlPullParser, firstNext: Int) {
        if (matches(xmlParser, firstNext)) {
            action(xmlParser.text)
        } else {
            throw XmlParseException("At line ${xmlParser.lineNumber} expected to find TEXT but found '${xmlParser.state(firstNext)}'")
        }
    }
}

@DslMarker
annotation class XmlParseDslMarker

@XmlParseDslMarker
internal interface Parser {

    fun matches(xmlParser: XmlPullParser, next: Int): Boolean

    fun parse(xmlParser: XmlPullParser, firstNext: Int)
}

class XmlParseException(message: String) : Exception(message)

private typealias ElementName = String

private typealias AttributeName = String
private fun XmlPullParser.state(latest: Int): String {
    return when (latest) {
        XmlPullParser.START_TAG -> "START_TAG of '$name'"
        XmlPullParser.END_TAG -> "END_TAG of '$name'"
        XmlPullParser.END_DOCUMENT -> "END_DOCUMENT of document"
        XmlPullParser.START_DOCUMENT -> "START_DOCUMENT of document"
        XmlPullParser.TEXT -> "TEXT of '$text'"
        else -> "Unknown state"
    }
}

private fun XmlPullParser.nextIgnoringWhiteSpace(): Int {
    var last = next()
    while (last == XmlPullParser.TEXT && text.isBlank()) {
        last = next()
    }

    return last
}

private fun XmlPullParser.nextIgnoringText(): Int {
    var last = next()
    while (last == XmlPullParser.TEXT) {
        last = next()
    }

    return last
}

private fun XmlPullParser.parseAttributes(attributes: List<Attribute>) {
    val actions = mutableMapOf<String, (String) -> Unit>()
    attributes.forEach {
        actions[it.name] = it.action
    }
    for (i in 0 until attributeCount) {
        val attributeName = getAttributeName(i)
        val action = actions[attributeName]
        action?.invoke(getAttributeValue(i))
    }
}

