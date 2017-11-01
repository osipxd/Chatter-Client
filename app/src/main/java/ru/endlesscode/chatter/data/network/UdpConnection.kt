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

package ru.endlesscode.chatter.data.network

import android.support.annotation.VisibleForTesting
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import ru.endlesscode.chatter.data.json.DataBytesConverter
import ru.endlesscode.chatter.data.json.bytesToData
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class UdpConnection(
        override val serverAddress: String,
        override val serverPort: Int,
        private val converter: DataBytesConverter,
        private val channel: DatagramChannel = DatagramChannel.open()
) : ServerConnection {

    override var handleData: (DataContainer) -> Unit = { }

    private val remoteAddress: InetSocketAddress
        get() = InetSocketAddress(InetAddress.getByName(serverAddress), serverPort)

    private var sendJob: Job? = null
    private var receiveJob: Job? = null

    companion object {
        private const val TAG = "UdpConnection"
        private const val TIMEOUT = 2 * 1000
    }

    override fun start() {
        if (channel.isConnected) return

        launch {
            try {
                channel.connect(remoteAddress)
                channel.socket().soTimeout = TIMEOUT
                log("Connected to: $remoteAddress")

                startChannelListening()
            } catch (e: Exception) {
                stop()
            }
        }
    }

    private fun startChannelListening() {
        receiveJob = launch {
            log("Starting data listener...")
            if (receiveJob?.waitConnection() == false) {
                return@launch
            }

            log("Data listener successfully started!")
            val buffer = allocateBuffer()
            while (listenChannel(buffer)) {
                // Listen channel while returns true
            }
        }
    }

    private fun listenChannel(buffer: ByteBuffer): Boolean {
        buffer.clear()
        val packet = DatagramPacket(buffer.array(), buffer.array().size)

        try {
            channel.socket().receive(packet)
            val container: DataContainer = converter.bytesToData(packet.data)
            handleData(container)
            log("Data received: $container")
        } catch (e: Exception) {
            val job = receiveJob
            if (job!!.isCancelled) {
                log("Listening was cancelled.")
                return false
            }
        }

        return true
    }

    override fun sendDataAsync(data: DataContainer): Job {
        val job = launch { sendData(data) }
        sendJob = job

        return job
    }

    @VisibleForTesting
    internal suspend fun sendData(data: DataContainer) {
        if (sendJob?.waitConnection() == false) {
            return
        }

        val buffer = allocateBuffer()
        buffer.put(converter.dataToBytes(data))
        buffer.flip()

        try {
            channel.write(buffer)
            log("Data sent: $data")
        } catch (e: Exception) {
            println(e.printStackTrace())
        }
    }

    private fun allocateBuffer(): ByteBuffer = ByteBuffer.allocate(1024)

    private suspend fun Job.waitConnection(): Boolean {
        while (!channel.isConnected) {
            delay(TIMEOUT.toLong())
            if (this.isCancelled) {
                log("Job was cancelled.")
                return false
            }
        }

        return true
    }

    suspend override fun stop() {
        log("Stopping channel listener...")
        receiveJob?.cancel()
        receiveJob?.join()
        log("Channel listener stopped.")
        log("Awaiting end of data sending...")
        sendJob?.join()
        log("All data sent.")

        channel.close()
        log("Connection closed.")
    }

    private fun log(message: String) {
        println("[$TAG] $message")
    }
}
