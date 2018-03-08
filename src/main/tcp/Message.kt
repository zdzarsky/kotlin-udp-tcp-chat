package main.tcp

import java.io.Serializable

data class Message(val username : String, val content : String) : Serializable