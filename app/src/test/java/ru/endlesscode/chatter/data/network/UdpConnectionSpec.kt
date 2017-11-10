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

import com.nhaarman.mockito_kotlin.clearInvocations
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import ru.endlesscode.chatter.di.DI
import ru.endlesscode.chatter.entity.remote.AliveData
import java.util.*
import kotlin.test.assertEquals


class UdpConnectionSpec : Spek({
    val channel = spy(DummyDatagramChannel())
    val connection = UdpConnection(
            serverAddress = "localhost",
            serverPort = 4242,
            converter = DI.converter,
            udpChannel = channel
    )

    given("a UdpConnection") {
        beforeEachTest {
            clearInvocations(channel)
        }

        it("should successfully send data") {
            val data = AliveContainer(AliveData(UUID.randomUUID()))
            runBlocking {
                connection.sendData(data)
            }
            verify(channel).dataSent(data)
        }

        it("should successfully receive data") {
            val data: DataContainer = AliveContainer(AliveData(UUID.randomUUID()))
            var receivedData: DataContainer? = null

            channel.sendDataFromServer(data)
            runBlocking {
                receivedData = connection.dataChannel.receive()
            }
            assertEquals(data, receivedData)
        }
    }
})
