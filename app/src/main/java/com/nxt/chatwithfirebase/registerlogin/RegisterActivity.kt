package com.nxt.chatwithfirebase

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.nxt.chatwithfirebase.messages.LatestMessageActivity
import com.nxt.chatwithfirebase.models.User
import com.nxt.chatwithfirebase.registerlogin.LoginActivity
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button_register.setOnClickListener {
            //hàm đăng ki email, password
            performRegister()
        }

        already_have_account_text_view.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        select_photo_button_register.setOnClickListener {
            Log.d("RegisterActivity", "try to show select")

            //chuyen man hinh den phan xem anh
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedPhotoUri: Uri? = null

    //chọn ảnh trên máy ảo gàn vào button
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectphoto_imageview_register.setImageBitmap(bitmap)
            select_photo_button_register.alpha = 0f

            //    val bitmapDrawable = BitmapDrawable(bitmap)
            //   select_photo_button_register.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun performRegister() {

        val email = email_edittext_register.text.toString()
        val passWord = password_edittext_register.text.toString()

        if (email.isEmpty() || passWord.isEmpty()) {
            Toast.makeText(this, "please enter text in email/pw", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("Register", "Email : " + email)
        Log.d("Register", "Password : $passWord")

        //Firebase authentication to create a userand password

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, passWord)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                //else if successfully
                Log.d("Register", "successfully created user uid: ${it.result?.user!!.uid}")

                upLoadImageToFirebaseStorage()
            }
            //lỗi ngoại tệ email thiếu @gmail.com
            .addOnFailureListener {
                Log.d("Register", "Fail to crete user : ${it.message}")
            }
    }

    private fun upLoadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        //UID: id duy nhất
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("Register", "successfully upload image: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Register", "File location : $it ")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {

            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/user/$uid")
        val user = User(uid, username_edittext_register.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Register", "Finally we saved the user to firebase database")

                val intent = Intent(this, LatestMessageActivity::class.java)
                //back chuyen ve man hinh dien thoai
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
    }
}