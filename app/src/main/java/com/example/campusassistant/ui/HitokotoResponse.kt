package com.example.campusassistant

// 这里的变量名必须和上面 JSON 里的键名（Key）完全一致
data class HitokotoResponse(
    val hitokoto: String, // 句子的内容
    val from: String      // 句子的出处
)