package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

data class WebCitation(
    val title: String,
    val uri: String
)

object CitationParser {
    fun parseCitations(jsonString: String?): List<WebCitation> {
        if (jsonString.isNullOrBlank()) return emptyList()
        val list = mutableListOf<WebCitation>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val title = obj.optString("title", "Web Source")
                val uri = obj.optString("uri", "")
                if (uri.isNotBlank()) {
                    list.add(WebCitation(title = title, uri = uri))
                }
            }
        } catch (_: Exception) {}
        return list
    }

    fun parseSearchQueries(jsonString: String?): List<String> {
        if (jsonString.isNullOrBlank()) return emptyList()
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }
}
