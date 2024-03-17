package tech.jknair.readsms

import java.util.Locale

object MessagePreprocessor {

    fun preprocessMessage(message: String): String {
        val processedMessage = message.lowercase(Locale.ROOT)
            .replace("[\\n\\r]".toRegex(), " ")
            .replace("(\\d),(\\d)".toRegex(), "$1$2")
            .replace(",", " ")
            .replace("[^\\u20A0-\\u20CFa-z0-9\\s.]".toRegex(), " ")
            .replace("(\\d)([\\u20A0-\\u20CF])".toRegex(), "$1 $2")
            .replace("([\\u20A0-\\u20CF])(\\d)".toRegex(), "$1 $2")
            .replace("(?<!\\d)\\.".toRegex(), " ")
            .replace("\\.(?!\\d)".toRegex(), " ")
            .replace("([a-z])(\\d)".toRegex(), "$1 $2")
            .replace("(\\d)([a-z])".toRegex(), "$1 $2")
            .replace("(?<=\\s)[a-z](?=\\s)".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
        return processedMessage.trim()
    }

}
