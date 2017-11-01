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

package ru.endlesscode.chatter.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import dagger.Lazy
import kotlinx.android.synthetic.main.screen_chat.*
import ru.endlesscode.chatter.App
import ru.endlesscode.chatter.R
import ru.endlesscode.chatter.entity.local.Message
import ru.endlesscode.chatter.presentation.presenter.ChatPresenter
import ru.endlesscode.chatter.presentation.view.ChatView
import ru.endlesscode.chatter.ui.adapter.ChatAdapter
import javax.inject.Inject

class ChatFragment : MvpAppCompatFragment(), ChatView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ChatPresenter

    @Inject
    lateinit var adapter: Lazy<ChatAdapter>

    private lateinit var messagesContainer: RecyclerView
    private val message by lazy { messageEdit }

    @ProvidePresenter
    fun providePresenter(): ChatPresenter = presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        App.appComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_chat, container, false)
        this.messagesContainer = view.findViewById(R.id.messagesContainer)
        messagesContainer.apply {
            setHasFixedSize(true)

            val layoutManager = LinearLayoutManager(context)
            layoutManager.stackFromEnd = true
            this.layoutManager = layoutManager

            if (adapter == null) {
                adapter = this@ChatFragment.adapter.get()
            }
        }

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerListeners()
    }

    private fun registerListeners() {
        sendButton.setOnClickListener { presenter.onSendPressed(message.text.toString()) }
    }

    override fun showError(errorMessage: String) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun initMessages(messages: List<Message>) {
        adapter.get().apply {
            initItems(messages)
            notifyDataSetChanged()
        }
    }

    override fun clearInput() {
        message.setText("")
    }

    override fun showNewMessage(position: Int) {
        adapter.get().notifyItemInserted(position)
    }

    override fun scrollTo(position: Int) {
        messagesContainer.scrollToPosition(position)
    }
}
