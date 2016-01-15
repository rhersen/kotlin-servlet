fun formatTime(unformatted: String?): String {
    if (unformatted == null)
        return ""

    val zeroSeconds = Regex(""".*T(\d\d:\d\d):00""").matchEntire(unformatted)

    if (zeroSeconds != null)
        return zeroSeconds.groups[1]?.value.toString()

    val nonZeroSeconds = Regex(""".*T(\d\d:\d\d:\d\d)""").matchEntire(unformatted)

    if (nonZeroSeconds != null)
        return nonZeroSeconds.groups[1]?.value.toString()

    return unformatted
}