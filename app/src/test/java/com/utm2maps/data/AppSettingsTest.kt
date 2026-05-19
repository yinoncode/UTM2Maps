package com.utm2maps.data

import kotlin.test.Test
import kotlin.test.assertEquals

class AppSettingsTest {
    @Test
    fun defaultInterfaceLanguageIsHebrew() {
        assertEquals(InterfaceLanguage.HEBREW, AppSettings().interfaceLanguage)
    }

    @Test
    fun interfaceLanguageEnumRoundTripsByName() {
        val saved = InterfaceLanguage.ENGLISH.name
        assertEquals(InterfaceLanguage.ENGLISH, enumValueOf<InterfaceLanguage>(saved))
    }
}
