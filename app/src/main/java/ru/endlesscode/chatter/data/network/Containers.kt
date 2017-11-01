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

import ru.endlesscode.chatter.entity.remote.AliveData
import ru.endlesscode.chatter.entity.remote.ErrorData
import ru.endlesscode.chatter.entity.remote.MessageData
import ru.endlesscode.chatter.entity.remote.NoticeData
import ru.endlesscode.chatter.extension.millisSinceEpoch


class AliveContainer(data: AliveData) : JsonDataContainer {
    override val data: AliveData? = data // Null when container received from server
    override val type = DataContainer.Type.ALIVE
    override val time: Long? = null
}


class MessageContainer(
        override val data: MessageData
) : JsonDataContainer {
    override val type = DataContainer.Type.MESSAGE
    override val time: Long = millisSinceEpoch()
}


class ConfirmContainer(
        override val time: Long
) : JsonDataContainer {
    override val type = DataContainer.Type.CONFIRM
    override val data: Any? = null
}


class NoticeContainer(
        override val time: Long,
        override val data: NoticeData
) : JsonDataContainer {
    override val type = DataContainer.Type.NOTICE
}


class ErrorContainer(
        override val data: ErrorData
) : JsonDataContainer {
    override val type: String = DataContainer.Type.ERROR
    override val time: Long? = null
}
