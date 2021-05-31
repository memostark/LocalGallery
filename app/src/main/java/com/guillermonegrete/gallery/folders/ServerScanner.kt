package com.guillermonegrete.gallery.folders

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.*

/**
 * Find server using UDP,
 * Taken from: https://michieldemey.be/blog/network-discovery-using-udp-broadcast/
 * */
class ServerScanner {

    suspend fun search(): String? {
        return withContext(Dispatchers.IO) {
            val className = this.javaClass.simpleName
            try {

                //Open a random port to send the package
                val c = DatagramSocket()
                c.broadcast = true


                val sendData = "DISCOVER_FUIFSERVER_REQUEST".toByteArray()
                //Try the 255.255.255.255 first
                try {
                    val sendPacket = DatagramPacket(
                        sendData,
                        sendData.size,
                        InetAddress.getByName("255.255.255.255"),
                        80
                    )
                    c.send(sendPacket)
                } catch (e: Exception) {
                }

                // Broadcast the message over all the network interfaces
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val networkInterface = interfaces.nextElement()

                    if (networkInterface.isLoopback || !networkInterface.isUp) continue // Don't want to broadcast to the loopback interface

                    for (interfaceAddress in networkInterface.interfaceAddresses) {
                        val broadcast = interfaceAddress.broadcast ?: continue
                        // Send the broadcast package!

                        try {
                            val sendPacket =
                                DatagramPacket(sendData, sendData.size, broadcast, 5005)
                            c.send(sendPacket)
                        } catch (e: Exception) {
                        }
                        println(this.javaClass.simpleName + ">>> Request packet sent to: " + broadcast.hostAddress + "; Interface: " + networkInterface.displayName)
                    }
                }
                println("$className>>> Done looping over all network interfaces. Now waiting for a reply!")

                //Wait for a response
                val recvBuf = ByteArray(1024)
                val receivePacket = DatagramPacket(recvBuf, recvBuf.size)

                c.soTimeout = 2000

                try {
                    c.receive(receivePacket)

                    //We have a response
                    println(className + ">>> Broadcast response from server: " + receivePacket.address.hostAddress)

                    //Check if the message is correct
                    val message = String(receivePacket.data, 0, receivePacket.length).trim()
                    if (message == "DISCOVER_FUIFSERVER_RESPONSE") {
                        println("Got: ${receivePacket.address}:${receivePacket.port}")
                        return@withContext receivePacket.address.toString()
                    }
                } catch (e: SocketTimeoutException) {
                    println(e.message)
                }

                c.close()
            } catch (ex: IOException) {
                println( ex.message)
            }

            return@withContext null
        }
    }

}