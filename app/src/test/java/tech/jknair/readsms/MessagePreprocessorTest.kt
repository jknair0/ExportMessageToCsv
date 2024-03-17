package tech.jknair.readsms

import org.junit.Assert.*
import org.junit.Test

class MessagePreprocessorTest {

    @Test
    fun test() {
        val input = """
            Premium+Overdue!+Premium+of+Rs.17370+was+due+on+07-03-2022+for+Tata+AIA+Life+Insurance+Policy+C228585035.

            As+per+RBI+circular,+effective+1st+Oct+2021,+all+Standing+Instruction+(SI)+registered+via+CARD/UPI+will+only+be+processed+as+per+the+new+guidelines.+You+can+click+https://rrj.nu/F4Hmb4l9+to+pay+now+to+avoid+late+payment+fees+and+lapsation.+Ignore+if+paid.+T&C+apply.
        """.trimIndent()

        val expectedOutput = "premium overdue premium of rs 17370 was due on 07 03 2022 for tata aia life insurance policy 228585035 as per rbi circular effective 1 st oct 2021 all standing instruction si registered via card upi will only be processed as per the new guidelines you can click https rrj nu 4 hmb 4 9 to pay now to avoid late payment fees and lapsation ignore if paid apply"
        val actualOutput = MessagePreprocessor.preprocessMessage(input)
        println(expectedOutput)
        println(actualOutput)
        assertEquals(expectedOutput, actualOutput)
    }

}