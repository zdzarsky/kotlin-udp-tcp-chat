package main

import java.io.Serializable
import java.net.Inet4Address

data class HandshakeMessage(val address: Inet4Address, val port : Int, val username : String) : Serializable