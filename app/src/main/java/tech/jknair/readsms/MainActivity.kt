package tech.jknair.readsms

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.provider.Telephony
import android.provider.Telephony.Sms.Inbox.ADDRESS
import android.provider.Telephony.Sms.Inbox.BODY
import android.provider.Telephony.Sms.Inbox.DATE
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.jknair.readsms.ui.theme.ReadSmsTheme
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReadSmsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    MessageListOwner()
                }
            }
        }
    }
}

suspend fun readMessages(context: Context): List<MessageEntry> = withContext(Dispatchers.IO) {
    val messages = mutableListOf<MessageEntry>()
    context.contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                val senderName = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(ADDRESS)) ?: "Not Found"
                val messageBody = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(BODY)) ?: "NA"
                val date = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(DATE)) ?: Date().time
                messages.add(MessageEntry(senderName, messageBody, date))
            } while (cursor.moveToNext())
        } else {
            // empty box, no SMS
        }
    }
    messages
}

suspend fun writeContacts(messages: List<MessageEntry>) = withContext(Dispatchers.IO) {
    val dir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (dir.exists()) {
        val contactsFile = File(dir, "messages.csv")
        if (contactsFile.exists()) {
            contactsFile.delete()
        }
        contactsFile.createNewFile()
        FileOutputStream(contactsFile).use { fos ->
            fos.write("received,contact_name,message\n".toByteArray())
            for (message in messages) {
                val urlEncodedMessage = URLEncoder.encode(message.message, "utf-8")
                fos.write("${message.messageReceivedAt},${message.name},$urlEncodedMessage\n".toByteArray())
            }
            fos.flush()
        }
    }
}

@Composable
private fun MessageListOwner() {
    val messages = remember { mutableStateListOf<MessageEntry>() }
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit, block = {
        messages.clear()
        messages.addAll(readMessages(context))
    })

    MessageList(messages)
}

@Composable
fun MessageList(messages: List<MessageEntry>) {
    val writeContactsToFile = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    Column {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages.size) { index ->
                val message = messages[index]
                Message(message)
            }
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = {
                writeContactsToFile.launch {
                    loading = true
                    writeContacts(messages)
                    loading = false
                }
            }) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                Text(text = "Export")
            }
        }
    }
}

@Composable
fun Message(message: MessageEntry) {

    val simpleDateFormat = SimpleDateFormat("dd, MMM yyyy hh:mm:ss a", Locale.getDefault())
    val formattedDate = simpleDateFormat.format(Date(message.messageReceivedAt))

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row {
            Text(
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.body1,
                text = message.name,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.body1,
            )
        }
        Text(
            style = MaterialTheme.typography.body2,
            text = message.message
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    ReadSmsTheme {
        MessageList(
            listOf(
                MessageEntry(
                    "jk",
                    "Hello",
                    Date().time
                ),
                MessageEntry(
                    "sample",
                    "Hello1",
                    Date().time
                ),
                MessageEntry(
                    "sample1",
                    "lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum ",
                    Date().time
                ),
            )
        )
    }
}