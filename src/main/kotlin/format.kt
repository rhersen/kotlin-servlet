fun formatTime(s: String?): String {
    if (s == null) return ""

    val t = s.indexOf("T")

    when {
        t == -1 -> return s
        s.endsWith(":00") -> return s.substring(t + 1, t + 6)
        else -> return s.substring(t + 1)
    }
}