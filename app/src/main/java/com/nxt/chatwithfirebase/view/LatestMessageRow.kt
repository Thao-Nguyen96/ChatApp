package com.nxt.chatwithfirebase.view

import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nxt.chatwithfirebase.R
import com.nxt.chatwithfirebase.models.ChatMessage
import com.nxt.chatwithfirebase.models.User
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageRow(val chatMessage: ChatMessage) : Item<ViewHolder>() {

    var chatPartnerUser : User? = null

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.message_textview_latest_message.text = chatMessage.text

        val chatpartnerId: String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatpartnerId = chatMessage.toId
        } else {
            chatpartnerId = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/user/$chatpartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue(User::class.java)
                viewHolder.itemView.username_textview_latest_message.text = chatPartnerUser?.userName

                Glide.with(viewHolder.itemView.context).load(chatPartnerUser?.profileImageUrl)
                    .into(viewHolder.itemView.imageview_latest_message)
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })


    }

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

}