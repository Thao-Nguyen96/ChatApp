package com.nxt.chatwithfirebase.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.nxt.chatwithfirebase.NewMessageActivity
import com.nxt.chatwithfirebase.NewMessageActivity.Companion.USER_KEY
import com.nxt.chatwithfirebase.R
import com.nxt.chatwithfirebase.RegisterActivity
import com.nxt.chatwithfirebase.messages.ChatLogActivity.Companion.TAG
import com.nxt.chatwithfirebase.models.ChatMessage
import com.nxt.chatwithfirebase.models.User
import com.nxt.chatwithfirebase.view.LatestMessageRow
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_message.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_message)

        recyclerview_latest_message.adapter = adapter
        recyclerview_latest_message.addItemDecoration(DividerItemDecoration(this,
            DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)

            val row = item as LatestMessageRow
            intent.putExtra(USER_KEY, row.chatPartnerUser)

            startActivity(intent)
        }

        listenForLatestMessage()
        //nap nguoi dung hien tai
        fetchCurrentUser()

        verifyUserIsLoggerIn()
    }

    val latestMessageMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerviewMessage() {

        adapter.clear()

        latestMessageMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessage() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-message/$fromId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                latestMessageMap[snapshot.key!!] = chatMessage!!

                refreshRecyclerviewMessage()

                adapter.add(LatestMessageRow(chatMessage))
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                adapter.add(LatestMessageRow(chatMessage!!))
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    val adapter = GroupAdapter<ViewHolder>()
    private fun fetchCurrentUser() {

        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/user/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                Log.d("LatestMessage", "current user ${currentUser?.profileImageUrl}")
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun verifyUserIsLoggerIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nave_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}