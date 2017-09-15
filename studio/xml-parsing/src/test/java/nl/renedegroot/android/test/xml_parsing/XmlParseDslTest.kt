package nl.renedegroot.android.test.xml_parsing

import org.hamcrest.core.Is.`is`
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.nio.charset.Charset

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class XmlParseDslTest {

    @Test
    fun singleDocument() {
        // Arrange
        val stream = streamFromString("")
        val parser = xml {
        }
        // Act
        parser.parse(stream)
    }

    @Test
    fun singleTagDocument() {
        // Arrange
        val stream = streamFromString("""<gpx></gpx>""")
        val parser = xml {
            element("gpx") {}
        }
        // Act
        parser.parse(stream)
    }

    @Test
    fun singleMetaData() {
        // Arrange
        val output = mutableListOf<String>()
        val stream = streamFromString("""
            <gpx>
                <metadata>
                    <name>trackname</name>
                    <author>name of the author</author>
                </metadata>
            </gpx>""")
        val xml = xml {
            element("gpx") {
                element("metadata") {
                    element("name") { text { output.add(it) } }
                    element("author") { text { output.add(it) } }
                }
            }
        }
        // Act
        xml.parse(stream)
        // Assert
        Assert.assertThat(output.size, `is`(2))
        Assert.assertThat(output[0], `is`("trackname"))
        Assert.assertThat(output[1], `is`("name of the author"))
    }

    @Test
    fun singleAttribute() {
        // Arrange
        val output = mutableListOf<String>()
        val stream = streamFromString("""
            <gpx>
                <metadata>
                    <copyright author="test-case">
                        <license>license://any/uri</license>
                    </copyright>
                </metadata>
            </gpx>""")
        val xml = xml {
            element("gpx") {
                element("metadata") {
                    element("copyright") {
                        attribute("author") { output.add(it) }
                        element("license") {}
                    }
                }
            }
        }
        // Act
        xml.parse(stream)
        // Assert
        Assert.assertThat(output.size, `is`(1))
        Assert.assertThat(output[0], `is`("test-case"))
    }

    @Test
    fun doubleReverseAttribute() {
        // Arrange
        val output = mutableListOf<String>()
        val stream = streamFromString("""
            <copyright author="test-case" year="second">
            </copyright>
            """)
        val xml = xml {
            element("copyright") {
                attribute("year") { output.add(it) }
                attribute("author") { output.add(it) }
            }
        }
        // Act
        xml.parse(stream)
        // Assert
        Assert.assertThat(output.size, `is`(2))
        Assert.assertThat(output[0], `is`("test-case"))
        Assert.assertThat(output[1], `is`("second"))
    }

    @Test
    fun optionalElementMissing() {
        // Arrange
        val output = mutableListOf<String>()
        val stream = streamFromString("""
            <copyright author="test-case" year="second">
            </copyright>
            """)
        val xml = xml {
            element("copyright") {
                element("license", 0) {
                    text { output.add(it) }
                }
            }
        }
        // Act
        xml.parse(stream)
        // Assert
        Assert.assertThat(output.size, `is`(0))
    }

    @Test
    fun optionalElementPresent() {
        // Arrange
        val output = mutableListOf<String>()
        val stream = streamFromString("""
            <copyright author="test-case" year="second">
                <license>license://any/uri</license>
            </copyright>
            """)
        val xml = xml {
            element("copyright") {
                element("license", 0) { text { output.add(it) } }
            }
        }
        // Act
        xml.parse(stream)
        // Assert
        Assert.assertThat(output.size, `is`(1))
        Assert.assertThat(output[0], `is`("license://any/uri"))
    }

    @Test(expected = XmlParseException::class)
    fun missingReqiuredElement() {
        // Arrange
        val output = mutableListOf<String>()
        val stream = streamFromString("""
            <copyright author="test-case" year="second">
            </copyright>
            """)
        val xml = xml {
            element("copyright") {
                element("license", 1) { text { output.add(it) } }
            }
        }
        // Act
        xml.parse(stream)
        // Assert
        Assert.assertTrue(false)
    }

    @Test(expected = XmlParseException::class)
    fun toManyElements() {
        // Arrange
        val output = mutableListOf<String>()
        val stream = streamFromString("""
            <copyright author="test-case" year="second">
                <license>license://any/uri</license>
                <license>license://any/uri</license>
            </copyright>
            """)
        val xml = xml {
            element("copyright") {
                element("license", maxOccurs = 1) { text { output.add(it) } }
            }
        }
        // Act
        xml.parse(stream)
        // Assert
        Assert.assertTrue(false)
    }

    @Test
    fun multipleManyElements() {
        // Arrange
        val output = mutableListOf<String>()
        val stream = streamFromString("""
            <copyright author="test-case" year="second">
                <license>license://any/1</license>
                <license>license://any/2</license>
                <license>license://any/3</license>
            </copyright>
            """)
        val xml = xml {
            element("copyright") {
                element("license", 2, 4) {
                    text { output.add(it) }
                }
            }
        }
        // Act
        xml.parse(stream)
        // Assert
        Assert.assertThat(output.size, `is`(3))
        Assert.assertThat(output[0], `is`("license://any/1"))
        Assert.assertThat(output[1], `is`("license://any/2"))
        Assert.assertThat(output[2], `is`("license://any/3"))
    }

    private fun streamFromString(string: String): ByteArrayInputStream {
        return ByteArrayInputStream(string.toByteArray(Charset.forName("UTF-8")))
    }
}
