/*
 * This file is part of Chatter, licensed under the MIT License (MIT).
 *
 * Copyright (c) Osip Fatkullin <osip.fatkullin@gmail.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.endlesscode.chatter.data

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.NotYetConnectedException

class UdpConnection(
        override val serverAddress: String,
        override val serverPort: Int,
        override val handleMessage: (String) -> Unit = { }
) : ServerConnection {

    private val remoteAddress: InetSocketAddress
        get() = InetSocketAddress(InetAddress.getByName(serverAddress), serverPort)

    private val channel = DatagramChannel.open()
    private var sendJob: Job? = null
    private var receiveJob: Job? = null

    companion object {
        private const val TIMEOUT = 2 * 1000
    }

    override fun start() {
        if (channel.isConnected) return

        launch {
            try {
                channel.connect(remoteAddress)
                channel.socket().soTimeout = TIMEOUT
                log("Connected to: $remoteAddress")

                startListenChannel()
            } catch (e: Exception) {
                stop()
            }
        }
    }

    private fun startListenChannel() {
        receiveJob = launch {
            log("Starting messages listener...")
            while (!channel.isConnected) {
                log("Connection lost, retrying in 2 seconds.")
                sleep(TIMEOUT.toLong())
                if (sendJob!!.isCancelled) {
                    log("Listening was cancelled.")
                    return@launch
                }
            }

            log("Messages listener successfully started!")
            val buffer = allocateBuffer()
            while (true) {
                buffer.clear()
                val packet = DatagramPacket(buffer.array(), buffer.array().size)

                try {
                    channel.socket().receive(packet)
                    val message = String(buffer.array().trim(), Charsets.UTF_8)
                    log("Message received: $message")
                    handleMessage(message)
                } catch (e: Exception) {
                    if (receiveJob!!.isCancelled) {
                        log("Listening was cancelled.")
                        break
                    }
                }
            }
        }
    }

    fun sendMessageAsync(message: String) {
        sendJob = if (sendJob == null) {
            launch { sendMessage(message) }
        } else {
            launch { }
        }
    }

    private suspend fun sendMessage(message: String) {
        log("Sending message: $message")
        while (!channel.isConnected) {
            log("Connection lost, retrying in 2 seconds.")
            sleep(TIMEOUT.toLong())
            if (sendJob!!.isCancelled) {
                log("Sending was cancelled.")
            }
        }

        val buffer = allocateBuffer()
        buffer.put(message.toByteArray())
        buffer.flip()

        try {
            channel.write(buffer)
            log("Message successfully sent!")
        } catch (e: NotYetConnectedException) {
            println(e.printStackTrace())
        }
    }

    private fun allocateBuffer(): ByteBuffer = ByteBuffer.allocate(1024)

    suspend override fun stop() {
        log("Stopping channel listener...")
        receiveJob?.cancel()
        receiveJob?.join()
        log("Channel listener stopped.")
        log("Awaiting end of message sending...")
        sendJob?.join()
        log("All messages sent.")

        channel.close()
        log("Connection closed.")
    }

    override fun log(message: String) {
        println(message)
    }

    fun ByteArray.trim(): ByteArray = this.filterNot { it == 0.toByte() }.toByteArray()
}
