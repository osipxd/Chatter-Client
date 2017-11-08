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

package ru.endlesscode.chatter.data.json

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import ru.endlesscode.chatter.data.network.AliveContainer
import ru.endlesscode.chatter.data.network.ConfirmContainer
import ru.endlesscode.chatter.data.network.DataContainer
import ru.endlesscode.chatter.data.network.ErrorContainer
import ru.endlesscode.chatter.data.network.MessageContainer
import ru.endlesscode.chatter.data.network.NoticeContainer
import ru.endlesscode.chatter.data.test.FileHelper
import ru.endlesscode.chatter.di.DI
import ru.endlesscode.chatter.entity.remote.AliveData
import ru.endlesscode.chatter.entity.remote.ErrorData
import ru.endlesscode.chatter.entity.remote.MessageInData
import ru.endlesscode.chatter.entity.remote.MessageOutData
import ru.endlesscode.chatter.entity.remote.NoticeData
import java.util.*
import kotlin.test.assertEquals


class DataBytesConverterSpec : Spek({
    val converter = DI.converter

    context("deserialization") {
        val uuid = UUID.fromString("5cf3839c-8fbf-41eb-8a99-4fbaeb1c3213")
        val time: Long = 1509226961923
        var json = ""
        var excepted: DataContainer? = null

        it("should return right AliveContainer (in)") {
            json = FileHelper.readContainerJson("AliveIn")
            excepted = AliveContainer(null)
        }

        it("should return right MessageContainer (out)") {
            json = FileHelper.readContainerJson("AliveOut")
            excepted = AliveContainer(AliveData(uuid))
        }

        it("should return right ConfirmContainer") {
            json = FileHelper.readContainerJson("Confirm")
            excepted = ConfirmContainer(time)
        }

        it("should return right ErrorContainer") {
            json = FileHelper.readContainerJson("Error")
            excepted = ErrorContainer(ErrorData("wrong_uuid"))
        }

        it("should return right MessageContainer (in)") {
            json = FileHelper.readContainerJson("MessageIn")
            val data = MessageInData(from = "space1flash", text = "Hi all!")
            excepted = MessageContainer(data, time)
        }

        it("should return right MessageContainer (out)") {
            json = FileHelper.readContainerJson("MessageOut")
            val data = MessageOutData(uuid = uuid, text = "Hi all!")
            excepted = MessageContainer(data, time)
        }

        it("should return right NoticeContainer") {
            json = FileHelper.readContainerJson("Notice")
            val data = NoticeData(reason = "error.no_user", args = arrayOf("osipxd"))
            excepted = NoticeContainer(time, data)
        }

        afterEachTest {
            val actual: DataContainer = converter.bytesToData(json.toByteArray())
            assertEquals(excepted, actual)
        }
    }
})
