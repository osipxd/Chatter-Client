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

import com.nhaarman.mockito_kotlin.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import kotlin.test.assertEquals


@RunWith(JUnitPlatform::class)
class UdpConnectionSpec : Spek({
    val socket = spy(TestDatagramSocket())
    val channel = spy(TestDatagramChannel(socket))
    val connection = UdpConnection(
            serverAddress = "localhost",
            serverPort = 4242,
            channel = channel
    )

    context("sending message") {
        beforeGroup {
            connection.start()
        }

        beforeEachTest {
            clearInvocations(socket)
            clearInvocations(channel)
        }

        it("should successfully send message") {
            val message = "Test message"
            runBlocking {
                connection.sendMessage(message)
            }
            verify(channel).messageSent(message)
        }

        it("should successfully receive message") {
            val message = "Test message"
            var receivedMessage = ""
            connection.handleMessage = { receivedMessage = it }

            whenever(socket.getMessage()).doReturn(message).thenReturn(null)
            runBlocking { delay(1000) }
            assertEquals(receivedMessage, message)
        }

        afterGroup {
            runBlocking {
                connection.stop()
            }
        }
    }
})
