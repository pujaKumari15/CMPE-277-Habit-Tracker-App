package com.ofalvai.habittracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ofalvai.habittracker.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response


@Composable
fun ChatGPTScreen() {
    var messageText by remember { mutableStateOf(TextFieldValue()) }
    val messageList = remember { mutableStateListOf<String>() }
    val client = OkHttpClient()

    fun addToChat(question: String) {
        messageList.add(question)
    }

    fun addResponse(response: String) {
        messageList.removeAt(messageList.size - 1)
       // addToChat(response, Message.SENT_BY_BOT)
        addToChat(response)
    }

    fun callAPI(question: String) {
        messageList.add("Typing... ")
        val jsonBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("max_tokens", 4000)
            put("temperature", 1)
            val promptArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", question)
                })
            }
            put("messages", promptArray)
        }
        val body = jsonBody.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer sk-proj-esXAVC59adJ66cAqqIO0T3BlbkFJTvQ2H7M9sqEkHogPPXwP")
            .header("Assistant-ID", "asst_chatgpt")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                addResponse("Failed to load response due to $e")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    if (response.isSuccessful) {
                        try {
                            val jsonObject = JSONObject(responseBody.string())
                            val jsonArray = jsonObject.getJSONArray("choices")
                            val result =
                                jsonArray.getJSONObject(0).getJSONObject("message")
                                    .getString("content")
                            addResponse(result.trim())
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } else {
                        addResponse("Failed to load response due to ${responseBody.toString()}")
                    }
                }
            }
        })
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Adjust padding as needed
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to AI Assistant",
                fontSize = 28.sp,
                color = Color.Black
            )

             LazyColumn {
                 items(messageList) { message ->
                   MessageItem(message)
             }
             }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BasicTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RectangleShape)
                        .padding(end = 8.dp) // Adjust padding as needed
                )

                IconButton(
                    onClick = {
                        val question = messageText.text.trim()
                        addToChat(question)
                        messageText = TextFieldValue()
                        callAPI(question)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_send_24),
                        contentDescription = null
                    )
                }

            }
        }
    }

}


@Composable
fun MessageItem(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        //elevation = 4.dp,
        //colors = CardColors. // Adjust as needed
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = message,
                color = Color.Black, // Adjust as needed
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
