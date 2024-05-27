package com.example.mynote.network

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

const val MOONSHOT_API_KEY = "sk-qfo83b9ulfObDyKzTXyZEuWhYSPuwqQtJ1TvGFPDhVVRYBbT"

interface LLMApiService {

//    使用 kimi 提供的 api
//    curl 交互例子如下：
//    curl https://api.moonshot.cn/v1/chat/completions \
//    -H "Content-Type: application/json" \
//    -H "Authorization: Bearer $MOONSHOT_API_KEY" \
//    -d '{
//        "model": "moonshot-v1-8k",
//        "messages": [
//            {"role": "system", "content": "你是 Kimi，由 Moonshot AI 提供的人工智能助手，你更擅长中文和英文的对话。你会为用户提供安全，有帮助，准确的回答。同时，你会拒绝一切涉及恐怖主义，种族歧视，黄色暴力等问题的回答。Moonshot AI 为专有名词，不可翻译成其他语言。"},
//            {"role": "user", "content": "你好，我叫李雷，1+1等于多少？"}
//        ],
//        "temperature": 0.3
//   }'
//    响应为
//    {"id":"chatcmpl-a405f33477df40138669b01792ffc62d","object":"chat.completion","created":1716812084,"model":"moonshot-v1-8k","choices":[{"index":0,"message":{"role":"assistant","content":"你好，李雷！1+1 等于 2。这是一个基本的数学加法问题。如果你有其他问题，欢迎继续提。"},"finish_reason":"stop"}],"usage":{"prompt_tokens":89,"completion_tokens":31,"total_tokens":120}}%
    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer $MOONSHOT_API_KEY" // 将 YOUR_API_KEY 替换为实际的 API 密钥
    )
    @POST("v1/chat/completions")
    suspend fun chat(@Body request: ChatRequest): Response<ChatResponse>
}

@Serializable
data class Message(
    val role: String,
    val content: String
)

val systemPrompt = Message(
    role = "system",
    content = "你是 Kimi，由 Moonshot AI 提供的人工智能助手，你更擅长中文和英文的对话。你会为用户提供安全，有帮助，准确的回答。同时，你会拒绝一切涉及恐怖主义，种族歧视，黄色暴力等问题的回答。Moonshot AI 为专有名词，不可翻译成其他语言。"
)

const val prompt = "请为下面的文字生成一段摘要："

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Float
)

@Serializable
data class ChatResponse(
//    val id: String,
    val choices: List<Choice>
)

// TODO 是否需要完整的字段
@Serializable
data class Choice(
    val message: Message
)