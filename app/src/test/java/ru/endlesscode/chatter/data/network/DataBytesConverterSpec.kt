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

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import ru.endlesscode.chatter.data.json.bytesToData
import ru.endlesscode.chatter.data.test.FileHelper
import ru.endlesscode.chatter.di.DI
import ru.endlesscode.chatter.entity.remote.AliveData
import java.util.*
import kotlin.test.assertEquals


@RunWith(JUnitPlatform::class)
class DataBytesConverterSpec : Spek({
    val converter = DI.converter

    on("deserialization") {
        it("should return right AliveContainer") {
            val result = FileHelper.readContainerJson("AliveOut")
            val uuid = UUID.fromString("5cf3839c-8fbf-41eb-8a99-4fbaeb1c3213")
            val excepted = AliveContainer(AliveData(uuid))
            val actual: DataContainer = converter.bytesToData(result.toByteArray())

            assertEquals(excepted, actual)
        }
    }
})
