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

package ru.endlesscode.chatter.model

import kotlinx.coroutines.experimental.channels.ActorJob
import kotlinx.coroutines.experimental.channels.ArrayChannel
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ProducerJob
import kotlinx.coroutines.experimental.launch
import kotlinx.sockets.aSocket
import kotlinx.sockets.adapters.openTextReceiveChannel
import kotlinx.sockets.adapters.openTextSendChannel
import java.lang.Thread.sleep
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer

class UdpConnection(
        override val serverAddress: String,
        override val serverPort: Int,
        override val messageHandler: Handler
) : ServerConnection {

    private val remoteAddress: InetSocketAddress
        get() = InetSocketAddress(InetAddress.getByName(serverAddress), serverPort)
    private val pool = runUnlimitedPool()

    override fun start() {
        launch {
            aSocket().udp().connect(remoteAddress).use { socket ->
                val sendChannel = socket.openTextSendChannel(Charsets.UTF_8, pool)
                val receiveChannel = socket.openTextReceiveChannel(Charsets.UTF_8, pool)

                val sendJob = sendMessages(sendChannel)
                val listenJob = listenChannel(receiveChannel)

                sendJob.join()
                listenJob.join()
            }
        }
    }

    private fun listenChannel(receiveChannel: ProducerJob<String>) = launch {
        while (true) {
            val message = receiveChannel.receive()
            println(message)
        }
    }

    private fun sendMessages(sendChannel: ActorJob<CharSequence>) = launch {
        while (true) {
            sleep(3000)
            sendChannel.send("Hello!")
        }
    }

    override fun stop() {

    }

    private fun runUnlimitedPool(): Channel<ByteBuffer> {
        val pool = ArrayChannel<ByteBuffer>(256)

        launch {
            while (true) {
                if (!pool.offer(ByteBuffer.allocate(8192))) break
            }
        }

        return pool
    }
}
