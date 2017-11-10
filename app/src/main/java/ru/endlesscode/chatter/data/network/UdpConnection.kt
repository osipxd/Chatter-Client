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
import kotlinx.coroutines.experimental.cancelAndJoin
import kotlinx.coroutines.experimental.channels.ProducerJob
import kotlinx.coroutines.experimental.channels.ProducerScope
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withTimeout
import ru.endlesscode.chatter.data.json.DataBytesConverter
import ru.endlesscode.chatter.data.json.bytesToData
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class UdpConnection(
        override val serverAddress: String,
        override val serverPort: Int,
        private val converter: DataBytesConverter,
        private val udpChannel: DatagramChannel = DatagramChannel.open()
) : ServerConnection {

    private val remoteAddress: InetSocketAddress
        get() = InetSocketAddress(InetAddress.getByName(serverAddress), serverPort)

    private var sendJob: Job? = null
    private var receiveJob: Job? = null

    companion object {
        private const val TAG = "UdpConnection"
        private const val TIMEOUT: Long = 1000

        private fun allocateBuffer(): ByteBuffer = ByteBuffer.allocate(1024)
    }

    override val dataChannel by lazy { newDataChannel() }

    private fun newDataChannel(): ProducerJob<DataContainer> = produce {
        try {
            listenChannelWhileActive()
        } finally {
            stop()
        }
    }

    private suspend fun ProducerScope<DataContainer>.listenChannelWhileActive() {
        log("Starting data listener...")
        checkConnection()

        log("Data listener successfully started!")
        val buffer = Companion.allocateBuffer()
        while (isActive) listenChannel(buffer)
    }

    private suspend fun ProducerScope<DataContainer>.listenChannel(buffer: ByteBuffer) {
        buffer.clear()

        withTimeout(TIMEOUT) {
            udpChannel.receive(buffer)
            val container: DataContainer = converter.bytesToData(buffer.array())
            offer(container)
            log("Data received: $container")
        }
    }

    override fun offerData(data: DataContainer): Job {
        val job = launch { sendData(data) }
        sendJob = job

        return job
    }

    @VisibleForTesting
    internal suspend fun sendData(data: DataContainer) {
        checkConnection()

        val buffer = Companion.allocateBuffer()
        buffer.put(converter.dataToBytes(data))
        buffer.flip()

        try {
            udpChannel.write(buffer)
            log("Data sent: $data")
        } catch (e: Exception) {
            println(e.printStackTrace())
        }
    }

    private fun checkConnection() {
        if (udpChannel.isConnected) return

        udpChannel.connect(remoteAddress)
        log("Connected to: $remoteAddress")
    }

    private suspend fun stop() {
        log("Stopping channel listener...")
        receiveJob?.cancelAndJoin()
        log("Channel listener stopped.")

        log("Awaiting end of data sending...")
        sendJob?.join()
        log("All data sent.")

        udpChannel.close()
        log("Connection closed.")
    }

    private fun log(message: String) {
        println("[$TAG] $message")
    }
}
