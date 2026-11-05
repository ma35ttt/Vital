package com.vital.ai

/** Result of a chat call, so a real API error (bad key, retired model) reaches the UI. */
sealed interface AiChatResult {
    data class Ok(val text: String) : AiChatResult
    data class Failure(val message: String) : AiChatResult
}
