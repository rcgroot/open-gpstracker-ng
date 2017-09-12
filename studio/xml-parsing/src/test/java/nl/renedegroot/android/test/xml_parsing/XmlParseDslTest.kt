package nl.renedegroot.android.test.xml_parsing

import org.hamcrest.core.Is.`is`
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.nio.charset.Charset

@RunWith(RobolectricTestRunner::class)
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
            tag("gpx") {

            }
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
            tag("gpx") {
                tag("metadata") {
                    tag("name") { text { output.add(it) } }
                    tag("author") { text { output.add(it) } }
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
            tag("gpx") {
                tag("metadata") {
                    tag("license") {
                        attribute("author") { output.add(it) }
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
                <license>license://any/uri</license>
            </copyright>
            """)
        val xml = xml {
            tag("copyright") {
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

    private fun streamFromString(string: String): ByteArrayInputStream {
        return ByteArrayInputStream(string.toByteArray(Charset.forName("UTF-8")))
    }
}
