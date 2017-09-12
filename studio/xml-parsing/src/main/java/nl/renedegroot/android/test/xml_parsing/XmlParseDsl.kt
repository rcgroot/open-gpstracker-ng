package nl.renedegroot.android.test.xml_parsing

import nl.renedegroot.android.test.utils.withResources
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedInputStream
import java.io.InputStream

fun xml(init: XML.() -> Unit): XML {
    val xml = XML()
    xml.init()
    return xml
}

@XmlParseDslMarker
class XML {
    private var root: Tag? = null

    fun tag(name: String, init: Tag.() -> Unit): Tag {
        val tag = Tag(name)
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

            var latest = 0
            while (latest != XmlPullParser.START_DOCUMENT) {
                latest = xmlParser.next()
            }

            root?.parse(xmlParser)

            while (latest != XmlPullParser.END_DOCUMENT) {
                latest = xmlParser.next()
            }
        }
    }
}

class Tag(private val name: TagName) : Parser {
    private val tags = mutableListOf<Parser>()
    private val attributes = mutableListOf<Attribute>()
    private var text: Text? = null

    fun tag(name: String, init: Tag.() -> Unit): Tag {
        val tag = Tag(name)
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

    override fun parse(xmlParser: XmlPullParser) {
        xmlParser.parseTag(name) {
            it.parseAttributes(attributes)
            tags.forEach {
                it.parse(xmlParser)
            }
            text?.parse(it)
        }
    }
}

@XmlParseDslMarker
class Attribute(val name: String, val action: (String) -> Unit)

class Text(val action: (String) -> Unit) : Parser {

    override fun parse(xmlParser: XmlPullParser) {
        xmlParser.parseText(action)
    }
}

@DslMarker
annotation class XmlParseDslMarker

@XmlParseDslMarker
internal interface Parser {

    fun parse(xmlParser: XmlPullParser)
}

private typealias TagName = String
private typealias AttributeName = String

private fun XmlPullParser.parseTag(name: TagName, parse: (XmlPullParser) -> Unit) {
    var latest = 0
    while (latest != XmlPullParser.START_TAG && this.name != name) {
        latest = next()
    }
    parse(this)
    while (latest != XmlPullParser.END_TAG && this.name != name) {
        latest = next()
    }
}

private fun XmlPullParser.parseText(action: (String) -> Unit) {
    var latest = 0
    while (latest != XmlPullParser.TEXT) {
        latest = next()
    }
    action(text)
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

