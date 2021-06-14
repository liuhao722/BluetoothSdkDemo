package com.worth.bluetooth.base.core.exts


fun String.Companion.empty() = ""
/**
 * 是否包含空格
 */
fun String.Companion.isSpace(s: String?): Boolean {
    if (s == null) return true
    var i = 0
    val len = s.length
    while (i < len) {
        if (!Character.isWhitespace(s[i])) {
            return false
        }
        ++i
    }
    return true
}