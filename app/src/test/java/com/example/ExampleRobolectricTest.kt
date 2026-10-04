package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CitationParser
import com.example.data.model.SupportedLanguages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Gemini AI", appName)
  }

  @Test
  fun `verify supported regional languages`() {
    val languages = SupportedLanguages.languages
    assertTrue(languages.any { it.code == "ur" })
    assertTrue(languages.any { it.code == "pa" })
    assertTrue(languages.any { it.code == "ps" })
    assertTrue(languages.any { it.code == "sd" })
    assertTrue(languages.any { it.code == "skr" })
    assertTrue(languages.any { it.code == "bal" })
    assertTrue(languages.any { it.code == "roman_ur" })

    val urduOption = SupportedLanguages.getByCode("ur")
    assertNotNull(urduOption)
    assertEquals("اردو", urduOption.nativeName)
  }

  @Test
  fun `verify citation parser`() {
    val sampleJson = """
      [{"title": "BBC Urdu", "uri": "https://www.bbc.com/urdu"}, {"title": "Dawn News", "uri": "https://www.dawn.com"}]
    """.trimIndent()
    val citations = CitationParser.parseCitations(sampleJson)
    assertEquals(2, citations.size)
    assertEquals("BBC Urdu", citations[0].title)
    assertEquals("https://www.bbc.com/urdu", citations[0].uri)
  }

  @Test
  fun `verify veo and gemini model constants`() {
    val veoModel = "veo-3.1-fast-generate-preview"
    val transcribeModel = "gemini-3.5-transcribe"
    val imageModel = "gemini-3.1-flash-image-preview"
    val proModel = "gemini-3.1-pro-preview"
    val flashModel = "gemini-3.5-flash"
    val flashLiteModel = "gemini-3.1-flash-lite-preview"

    assertNotNull(veoModel)
    assertNotNull(transcribeModel)
    assertNotNull(imageModel)
    assertNotNull(proModel)
    assertNotNull(flashModel)
    assertNotNull(flashLiteModel)
  }
}
