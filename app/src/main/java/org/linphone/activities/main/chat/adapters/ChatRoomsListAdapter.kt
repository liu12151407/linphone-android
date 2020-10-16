/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.activities.main.chat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.linphone.R
import org.linphone.activities.main.chat.viewmodels.ChatRoomViewModel
import org.linphone.activities.main.viewmodels.ListTopBarViewModel
import org.linphone.core.ChatRoom
import org.linphone.databinding.ChatRoomListCellBinding
import org.linphone.utils.Event
import org.linphone.utils.SelectionListAdapter

class ChatRoomsListAdapter(
    selectionVM: ListTopBarViewModel,
    private val viewLifecycleOwner: LifecycleOwner
) : SelectionListAdapter<ChatRoom, RecyclerView.ViewHolder>(selectionVM, ChatRoomDiffCallback()) {
    val selectedChatRoomEvent: MutableLiveData<Event<ChatRoom>> by lazy {
        MutableLiveData<Event<ChatRoom>>()
    }

    val toggledPositionForSelectionEvent: MutableLiveData<Event<Int>> by lazy {
        MutableLiveData<Event<Int>>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ChatRoomListCellBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.chat_room_list_cell, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ChatRoomListCellBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chatRoom: ChatRoom) {
            with(binding) {
                val chatRoomViewModel = ChatRoomViewModel(chatRoom)
                viewModel = chatRoomViewModel

                binding.lifecycleOwner = viewLifecycleOwner

                // This is for item selection through ListTopBarFragment
                selectionListViewModel = selectionViewModel
                selectionViewModel.isEditionEnabled.observe(viewLifecycleOwner, {
                    position = adapterPosition
                })

                binding.setClickListener {
                    if (selectionViewModel.isEditionEnabled.value == true) {
                        selectionViewModel.onToggleSelect(adapterPosition)
                    } else {
                        selectedChatRoomEvent.value = Event(chatRoom)
                        chatRoom.markAsRead()
                    }
                }

                executePendingBindings()
            }
        }
    }
}

private class ChatRoomDiffCallback : DiffUtil.ItemCallback<ChatRoom>() {
    override fun areItemsTheSame(
        oldItem: ChatRoom,
        newItem: ChatRoom
    ): Boolean {
        return oldItem.localAddress.weakEqual(newItem.localAddress) &&
                oldItem.peerAddress.weakEqual(newItem.peerAddress)
    }

    override fun areContentsTheSame(
        oldItem: ChatRoom,
        newItem: ChatRoom
    ): Boolean {
        return newItem.unreadMessagesCount == 0
    }
}