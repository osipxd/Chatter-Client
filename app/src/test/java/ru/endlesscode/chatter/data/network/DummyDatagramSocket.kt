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

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.whenever
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException

class DummyDatagramSocket : DatagramSocket() {

    var responseTime: Long = 500
    var deltaTime: Long = 100

    private var currentWaitTime: Long = 0

    fun getContainer(): DataContainer? {
        return null
    }

    override fun receive(packet: DatagramPacket) {
        currentWaitTime = 0
        runBlocking {
            var container = getContainer()
            while (true) {
                delay(deltaTime)
                currentWaitTime += deltaTime

                if (container == null) {
                    container = getContainer()
                }

                if (currentWaitTime >= responseTime && container != null) {
                    val data = container.toByteArray()
                    val currData = packet.data
                    data.forEachIndexed { index, byte -> currData[index] = byte }
                    break
                }

                if (currentWaitTime >= soTimeout) throw SocketTimeoutException()
            }
        }
    }

    fun sendDataFromServer(data: DataContainer) {
        whenever(this.getContainer()).doReturn(data).thenReturn(null)
    }
}
