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

import com.nhaarman.mockito_kotlin.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.api.dsl.xit
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import ru.endlesscode.chatter.data.messages.MessageImpl
import ru.endlesscode.chatter.data.messages.NetworkMessagesRepository
import ru.endlesscode.chatter.data.network.DummyDatagramChannel
import ru.endlesscode.chatter.data.network.DummyDatagramSocket
import ru.endlesscode.chatter.data.network.MessageContainer
import ru.endlesscode.chatter.data.network.UdpConnection
import ru.endlesscode.chatter.di.DI
import ru.endlesscode.chatter.entity.local.Message
import ru.endlesscode.chatter.entity.remote.MessageInData
import kotlin.test.assertEquals


@RunWith(JUnitPlatform::class)
class UdpMessagesRepositorySpec : Spek({
    val socket = spy(DummyDatagramSocket())
    val channel = spy(DummyDatagramChannel(socket))
    val connection = spy(UdpConnection(
            serverAddress = "localhost",
            serverPort = 4242,
            channel = channel,
            converter = DI.converter
    ))
    val repository = NetworkMessagesRepository(connection)

    beforeEachTest {
        clearInvocations(connection)
    }

    context("sending message") {
        beforeEachTest {
            clearInvocations(connection)
        }

        it("should send if queue is empty") {
            val message = MessageImpl("client", "message for server", 1322018752992L)
            repository.sendMessage(message)
            verify(connection).sendDataAsync(any())
        }

        it("should send after sending all messages in queue") {
            val message = MessageImpl("client", "another message for server", 1322018752992L)
            repository.sendMessage(message)
            verify(connection, times(0)).sendDataAsync(any())
            runBlocking { delay((channel.sendTime * 1.2).toLong()) }

            verify(connection).sendDataAsync(any())
        }
    }

    on("receiving message") {
        var receivedMessage: Message? = null
        repository.setMessageListener { receivedMessage = it }

        xit("should receive message") {
            val message = MessageImpl("server", "message from server", 1322018752992L)
            val messageData = MessageInData(message.from, message.text)
            socket.sendDataFromServer(MessageContainer(messageData))
            runBlocking { delay((socket.responseTime * 1.5).toLong()) }
            assertEquals(message, receivedMessage)
        }
    }

    afterGroup {
        runBlocking {
            repository.finish()
        }
    }
})
