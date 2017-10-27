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

import com.nhaarman.mockito_kotlin.mock
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import ru.endlesscode.chatter.extension.toPrintable
import java.net.*
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.MembershipKey

class DummyDatagramChannel(private val socket: DummyDatagramSocket) : DatagramChannel(mock()) {

    val sendTime: Long = 300
    var connected = false

    private lateinit var remoteAddress: SocketAddress

    override fun write(buffer: ByteBuffer): Int {
        runBlocking { delay(sendTime) }
        messageSent(buffer.toPrintable())
        return 0
    }

    fun messageSent(message: String) {
        // Nothing
    }

    override fun write(p0: Array<out ByteBuffer>?, p1: Int, p2: Int): Long {
        TODO("mock")
    }

    override fun connect(remoteAddress: SocketAddress): DatagramChannel {
        this.remoteAddress = remoteAddress
        connected = true
        return this
    }

    override fun isConnected(): Boolean {
        return connected
    }

    override fun supportedOptions(): MutableSet<SocketOption<*>> {
        TODO("mock")
    }

    override fun socket(): DatagramSocket {
        return socket
    }

    override fun read(p0: ByteBuffer?): Int {
        TODO("mock")
    }

    override fun read(p0: Array<out ByteBuffer>?, p1: Int, p2: Int): Long {
        TODO("mock")
    }

    override fun join(p0: InetAddress?, p1: NetworkInterface?): MembershipKey {
        TODO("mock")
    }

    override fun join(p0: InetAddress?, p1: NetworkInterface?, p2: InetAddress?): MembershipKey {
        TODO("mock")
    }

    override fun implConfigureBlocking(p0: Boolean) {
        TODO("mock")
    }

    override fun getLocalAddress(): SocketAddress {
        TODO("mock")
    }

    override fun <T : Any?> getOption(p0: SocketOption<T>?): T {
        TODO("mock")
    }

    override fun disconnect(): DatagramChannel {
        connected = false
        return this
    }

    override fun <T : Any?> setOption(p0: SocketOption<T>?, p1: T): DatagramChannel {
        TODO("mock")
    }

    override fun implCloseSelectableChannel() {}

    override fun bind(p0: SocketAddress?): DatagramChannel {
        TODO("mock")
    }

    override fun receive(p0: ByteBuffer?): SocketAddress {
        TODO("mock")
    }

    override fun send(p0: ByteBuffer?, p1: SocketAddress?): Int {
        TODO("mock")
    }

    override fun getRemoteAddress(): SocketAddress {
        return remoteAddress
    }
}
