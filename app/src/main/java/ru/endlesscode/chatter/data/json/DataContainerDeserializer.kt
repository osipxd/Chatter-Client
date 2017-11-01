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

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import ru.endlesscode.chatter.data.network.*
import java.lang.reflect.Type

class DataContainerDeserializer : JsonDeserializer<DataContainer> {

    companion object {
        private const val TYPE = "type"
    }

    override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
    ): DataContainer {
        val obj = json.asJsonObject
        if (!obj.has(TYPE))
            throw IllegalArgumentException("JSON should contain field \"type\"")

        val typeName = obj.get(TYPE).asString
        val type = when (typeName) {
            DataContainer.Type.ALIVE -> AliveContainer::class.java
            DataContainer.Type.MESSAGE -> MessageContainer::class.java
            DataContainer.Type.CONFIRM -> ConfirmContainer::class.java
            DataContainer.Type.NOTICE -> NoticeContainer::class.java
            DataContainer.Type.ERROR -> ErrorContainer::class.java
            else -> throw IllegalArgumentException("Unknown container type: $typeName")
        }

        return context.deserialize(obj, type)
    }
}
