package com.nxt.chatwithfirebase.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.nxt.chatwithfirebase.NewMessageActivity.Companion.USER_KEY
import com.nxt.chatwithfirebase.R
import com.nxt.chatwithfirebase.models.ChatMessage
import com.nxt.chatwithfirebase.models.User
import com.nxt.chatwithfirebase.view.ChatFromItem
import com.nxt.chatwithfirebase.view.ChatToItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "CHAT_LOG"
    }

    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter = adapter

        // val userName = intent.getStringExtra(USER_KEY)
        toUser = intent.getParcelableExtra<User>(USER_KEY)
        supportActionBar?.title = toUser?.userName

        listenForMessage()

        send_button_chat_log.setOnClickListener {
            Log.d(TAG, "Attempt to message")
            performSendMessage()
        }
    }

    private fun listenForMessage() {

        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-message/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)



                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        println(chatMessage)
                        val currentUser = LatestMessageActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text, currentUser!!))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }
                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun performSendMessage() {
        //how do we actually send a message to firebase
        val text = edittext_chat_log.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(USER_KEY)
        val toId = user?.uid

        if (fromId == null) return
        if (toId == null) return

        // val reference = FirebaseDatabase.getInstance().getReference("/message").push()
        val reference =
            FirebaseDatabase.getInstance().getReference("/user-message/$fromId/$toId").push()

        val toReference =
            FirebaseDatabase.getInstance().getReference("/user-message/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System
            .currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "SAVE OUR CHAT MESSAGE: ${reference.key}")
                edittext_chat_log.text.clear()
                //tu cuon tin nhan den vi tri cuoi cung
                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

        toReference.setValue(chatMessage)

        val latestMessageRef =
            FirebaseDatabase.getInstance().getReference("/latest-message/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef =
            FirebaseDatabase.getInstance().getReference("/latest-message/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }
}