package com.aimp.compose.lyrics

data class LyricLine(val timeMs: Long, val text: String)

object LrcParser {
    private val lineRegex = Regex("^\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?](.*)$")

    fun parseSynced(input: String?): List<LyricLine> {
        if (input.isNullOrBlank()) return emptyList()
        return input.lines().mapNotNull { raw ->
            val m = lineRegex.find(raw.trim()) ?: return@mapNotNull null
            val (mm, ss, frac, text) = m.destructured
            val ms = mm.toLong() * 60_000 + ss.toLong() * 1_000 +
                (frac.ifBlank { "0" }.padEnd(3, '0').toLong())
            LyricLine(ms, text.trim())
        }.sortedBy { it.timeMs }
    }

    fun parsePlain(input: String?): List<LyricLine> =
        input?.lines()?.mapIndexed { i, text -> LyricLine(i * 0L, text) } ?: emptyList()
}
