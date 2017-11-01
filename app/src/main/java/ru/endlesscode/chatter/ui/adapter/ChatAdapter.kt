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

package ru.endlesscode.chatter.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import ru.endlesscode.chatter.R
import ru.endlesscode.chatter.entity.local.Message
import ru.endlesscode.chatter.extension.inflate
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ChatAdapter @Inject constructor() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var showLoader = false
        set(value) {
            field = value

            if (value) notifyItemInserted(loaderPosition)
            else notifyItemRemoved(loaderPosition)
        }

    private var messages = emptyList<Message>()
    private val loaderPosition
        get() = messages.size

    fun initItems(messages: List<Message>) {
        this.messages = messages
        showLoader = false
    }

    override fun getItemViewType(position: Int): Int =
            if (position == loaderPosition) ListItemTypes.LOADER else ListItemTypes.MESSAGE

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MessageViewHolder) {
            holder.init(messages[position])
        }
    }

    override fun onCreateViewHolder(group: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ListItemTypes.MESSAGE -> MessageViewHolder(group)
            ListItemTypes.LOADER -> LoaderViewHolder(group)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun getItemCount(): Int = if (showLoader) messages.size + 1 else messages.size

    inner class LoaderViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent.inflate(R.layout.item_loader))
}
