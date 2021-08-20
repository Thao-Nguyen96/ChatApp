package com.nxt.chatwithfirebase.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//them parcelable de put user
//chia nho user ra de chuyen du lieu
//ghi doi tuong vao buu kien

@Parcelize
class User(val uid: String, val userName: String, val profileImageUrl: String):Parcelable {
    //colection : suwax loi doc firebase
    constructor() : this("", "", "")
}