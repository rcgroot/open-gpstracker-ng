package nl.renedegroot.android.gpx

fun gpx(init: GPX.() -> Unit): GPX {
    val gpx = GPX()
    gpx.init()
    return gpx
}

class GPX : Tag("gpx") {
    fun metadata(init: Metadata.() -> Unit): Metadata {
        return initTag(init, Metadata())
    }

    fun waypoint(init: Waypoint.() -> Unit): Waypoint {
        return initTag(init, Waypoint())
    }
}

class Metadata : Tag("metadata") {
}

class Waypoint : Tag("waypoint") {

    fun elevation(init: Elevation.() -> Unit): Elevation {
        return initTag(init, Elevation())
    }

    fun time(init: Time.() -> Unit): Time {
        return initTag(init, Time())
    }
}

class Elevation : TagWithText("elevation") {
}

class Time : TagWithText("time") {
}


interface Element {
//    fun render(builder: StringBuilder, indent: String)
}

@DslMarker
annotation class GpxTagMarker

@GpxTagMarker
abstract class Tag(val name: String) : Element {
    val children = arrayListOf<Element>()
    val attributes = hashMapOf<String, String>()

    internal fun <T : Tag> initTag(init: T.() -> Unit, tag: T): T {
        tag.init()
        children.add(tag)

        return tag;
    }
}

abstract class TagWithText(name: String) : Tag(name) {
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}

class TextElement(val text: String) : Element {
}
