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

package ru.endlesscode.chatter.data.messages

import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import ru.endlesscode.chatter.data.network.DataContainer
import ru.endlesscode.chatter.data.network.MessageContainer
import ru.endlesscode.chatter.data.network.ServerConnection
import ru.endlesscode.chatter.entity.local.Message
import ru.endlesscode.chatter.entity.remote.MessageInData
import ru.endlesscode.chatter.entity.remote.MessageOutData
import java.util.*


class NetworkMessagesRepository(
        private val connection: ServerConnection
) : MessagesRepository {

    override val messageChannel = Channel<Message>(CONFLATED)

    private val jobQueue = mutableListOf<Job>()
    private val isQueueFree: Boolean
        get() = jobQueue.isEmpty()

    init {
        launch {
            connection.dataChannel.consumeEach {
                handleContainer(it)
            }
        }
    }

    override fun offerMessage(message: Message): Job {
        val job = launch(start = CoroutineStart.LAZY) {
            sendMessage(message)
        }

        if (isQueueFree) {
            jobQueue.add(job)
            sendNextMessage()
        } else {
            jobQueue.add(job)
        }

        return job
    }

    private fun sendNextMessage() {
        jobQueue.first().start()
    }

    private suspend fun sendMessage(message: Message) {
        // TODO: Send real UUID
        val data = MessageOutData(UUID.randomUUID(), message.text)
        connection.offerData(MessageContainer(data)).join()
        onMessageSent()
    }

    private fun onMessageSent() {
        jobQueue.removeAt(0)
        if (!isQueueFree) sendNextMessage()
    }

    private fun handleContainer(container: DataContainer) {
        when (container) {
            is MessageContainer -> handleMessageContainer(container)
        }
    }

    private fun handleMessageContainer(container: MessageContainer) {
        val data = container.data as MessageInData
        val message = MessageIn(data.from, data.text, container.time)
        messageChannel.offer(message)
    }

    override suspend fun finish() {
        messageChannel.close()
        connection.stop()
    }
}
