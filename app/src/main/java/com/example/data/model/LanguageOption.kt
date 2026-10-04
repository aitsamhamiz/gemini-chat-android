package com.example.data.model

data class LanguageOption(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val systemInstruction: String,
    val speechLocale: String
)

object SupportedLanguages {
    val languages = listOf(
        LanguageOption(
            code = "auto",
            nativeName = "خودکار (Auto Detect)",
            englishName = "Auto-Detect",
            systemInstruction = "You automatically detect the language of the user's message (Urdu, Roman Urdu, Punjabi, Pashto, Sindhi, Saraiki, Balochi, English, Hindi, Arabic, etc.) and respond fluently in that exact same language and script.",
            speechLocale = "ur-PK"
        ),
        LanguageOption(
            code = "ur",
            nativeName = "اردو",
            englishName = "Urdu",
            systemInstruction = "آپ کو تمام جوابات خالص اور شائستہ اردو زبان (Urdu) میں دینے ہیں۔ زبان کا اسلوب باوقار، علمی اور فصیح ہونا چاہیے۔",
            speechLocale = "ur-PK"
        ),
        LanguageOption(
            code = "roman_ur",
            nativeName = "Roman Urdu (رومن اردو)",
            englishName = "Roman Urdu",
            systemInstruction = "Respond in Roman Urdu (Urdu written in Latin/English alphabet, e.g., 'Aap ka sawal bohot acha hai, main aap ki madad kar sakta hoon'). Keep it natural and colloquial Pakistani conversational style.",
            speechLocale = "en-US"
        ),
        LanguageOption(
            code = "en",
            nativeName = "English",
            englishName = "English",
            systemInstruction = "Respond in clear, professional English with precise structure, code examples where applicable, and step-by-step clarity.",
            speechLocale = "en-US"
        ),
        LanguageOption(
            code = "pa",
            nativeName = "پنجابی (Punjabi)",
            englishName = "Punjabi (Shahmukhi)",
            systemInstruction = "تسی صارف نوں پنجابی زبان (شاہ مکھی رسم الخط) وچ جواب دیو۔ انداز مٹھا، پرخلوص تے روایتی پنجابی ہووے۔",
            speechLocale = "pa-IN"
        ),
        LanguageOption(
            code = "ps",
            nativeName = "پښتو (Pashto)",
            englishName = "Pashto",
            systemInstruction = "تاسو باید کارونکي ته په روانه او خوږه پښتو ژبه کې ځواب ورکړئ. انداز دې باوقار او پښتني ادب سره مل وي.",
            speechLocale = "ps-AF"
        ),
        LanguageOption(
            code = "sd",
            nativeName = "سنڌي (Sindhi)",
            englishName = "Sindhi",
            systemInstruction = "توهان صارف کي مٺي سنڌي ٻوليءَ (Sindhi script) ۾ سٺا ۽ معلوماتي جواب ڏيو. اسلوب اديباڻو ۽ شائستو هجي.",
            speechLocale = "ur-PK"
        ),
        LanguageOption(
            code = "skr",
            nativeName = "سرائیکی (Saraiki)",
            englishName = "Saraiki",
            systemInstruction = "تساں صارف کوں سرائیکی زبان وچ مٺے تے پیارے انداز نال جواب ڈیو۔ وسیب دی ثقافت تے روایات دا احترام کرو۔",
            speechLocale = "ur-PK"
        ),
        LanguageOption(
            code = "bal",
            nativeName = "بلوچی (Balochi)",
            englishName = "Balochi",
            systemInstruction = "شما باید پہ بلوچی زبان ءَ جواب بدئے ات۔ بلوچی دود ءُ ربیدگ ءُ شرف ءِ خیال بدار ات۔",
            speechLocale = "ur-PK"
        ),
        LanguageOption(
            code = "ar",
            nativeName = "العربية (Arabic)",
            englishName = "Arabic",
            systemInstruction = "يجب عليك الرد باللغة العربية الفصحى الواضحة والراقية.",
            speechLocale = "ar-SA"
        ),
        LanguageOption(
            code = "hi",
            nativeName = "हिन्दी (Hindi)",
            englishName = "Hindi",
            systemInstruction = "आप उपयोगकर्ता को शुद्ध और स्पष्ट हिन्दी भाषा में उत्तर दें।",
            speechLocale = "hi-IN"
        )
    )

    fun getByCode(code: String): LanguageOption {
        return languages.firstOrNull { it.code == code } ?: languages[0]
    }
}
