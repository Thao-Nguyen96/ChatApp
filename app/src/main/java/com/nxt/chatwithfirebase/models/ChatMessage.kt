package com.nxt.chatwithfirebase.models

class ChatMessage(
    val id: String,
    val text: String,
    val fromId: String,
    val toId: String,
    val timeStamps: Long,
){
    constructor() : this("", "","", "", -1)
}