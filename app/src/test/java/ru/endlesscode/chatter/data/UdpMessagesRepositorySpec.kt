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

import com.nhaarman.mockito_kotlin.clearInvocations
import com.nhaarman.mockito_kotlin.spy
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import ru.endlesscode.chatter.data.messages.MessageIn
import ru.endlesscode.chatter.data.messages.NetworkMessagesRepository
import ru.endlesscode.chatter.data.network.DummyDatagramChannel
import ru.endlesscode.chatter.data.network.UdpConnection
import ru.endlesscode.chatter.di.DI
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class UdpMessagesRepositorySpec : Spek({
    val channel = spy(DummyDatagramChannel())
    val connection = spy(UdpConnection(
            serverAddress = "localhost",
            serverPort = 4242,
            converter = DI.converter,
            udpChannel = channel
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
            runBlocking {
                val message = MessageIn("client", "message for server", 1322018752992L)
                val job = repository.offerMessage(message)
                delay(50)
                assertTrue(job.isCompleted)
            }
        }
    }

    on("receiving message") {
        it("should receive message") {
            runBlocking {
                val message = MessageIn("server", "message from server", 1322018752992L)
                repository.messageChannel.offer(message)
                val receivedMessage = repository.messageChannel.receive()
                assertEquals(message, receivedMessage)
            }
        }
    }

    afterGroup {
        runBlocking {
            repository.finish()
        }
    }
})
