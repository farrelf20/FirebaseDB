package com.example.androidlatihan15_firebasedb

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Log.e
import android.util.Log.w
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import de.hdodenhof.circleimageview.CircleImageView

class MainMessage : AppCompatActivity(),
    GoogleApiClient.OnConnectionFailedListener {

    class MessageViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var messageTextView: TextView
        var messageImageView: ImageView
        var messengerTextView: TextView
        var messengerImageCircle: CircleImageView

        init {
            messageTextView = v.findViewById(R.id.tv_message)
            messageImageView = v.findViewById(R.id.imView_message)
            messengerTextView = v.findViewById(R.id.tv_messenger)
            messengerImageCircle = v.findViewById(R.id.messengerImageView)
        }
    }

    val MESSAGE_CHILD = "messages"
    private val REQUEST_INVITE = 1
    private val REQUEST_IMAGE = 2
    private val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    val ANONYMOUS = "anonymous"
    private var mUsername: String? = null
    private var mPhotoUrl: String? = null
    private var mGoogleApiClient: GoogleApiClient? = null

    private var btnSend: Button? = null
    private var rcView: RecyclerView? = null
    private var llManager: LinearLayoutManager? = null
    private var progressbar: ProgressBar? = null
    private var et_message: EditText? = null
    private var img_message: ImageView? = null

    private var fAuth: FirebaseAuth? = null
    private var fUserAuth: FirebaseUser? = null
    private var dbRef: DatabaseReference? = null
    private var firebaseAdapter:
            FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_message)

        mUsername = ANONYMOUS
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API)
            .build()

        progressbar = findViewById(R.id.progresBar)
        rcView = findViewById(R.id.messageRecyclerView)
        llManager = LinearLayoutManager(this)
        llManager!!.stackFromEnd = true
        rcView!!.layoutManager = llManager
        dbRef = FirebaseDatabase.getInstance().reference

        val parser = SnapshotParser<FriendlyMessage> { dataSnapshot ->
            val friendlyMessage = dataSnapshot.getValue(FriendlyMessage::class.java)
            if (friendlyMessage != null) {
                friendlyMessage.setId(dataSnapshot.key)
            }
            friendlyMessage!!
        }
        val messageRef = dbRef!!.child(MESSAGE_CHILD)
        val options = FirebaseRecyclerOptions.Builder<FriendlyMessage>()
            .setQuery(messageRef, parser)
            .build()
        firebaseAdapter = object : FirebaseRecyclerAdapter<FriendlyMessage,
                MessageViewHolder>(options) {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MessageViewHolder {
                val inflater = LayoutInflater.from(p0.context)
                return MessageViewHolder(
                    inflater.inflate(
                        R.layout.item_message, p0, false
                    )
                )
            }

            override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: FriendlyMessage) {
                progressbar!!.visibility = ProgressBar.INVISIBLE
                if (model.getText() != null) {
                    holder.messageTextView.text = model.getText()
                    holder.messageTextView.visibility = TextView.VISIBLE
                    holder.messageImageView.visibility = ImageView.GONE
                } else if (model.getImageUrl() != null) {
                    val imageUrl = model.getImageUrl()
                    if (imageUrl!!.startsWith("gs://")) {
                        val storageReference = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(imageUrl)
                        storageReference.downloadUrl.addOnCompleteListener {
                            if (it.isSuccessful) {
                                val downloadUrl = it.result!!.toString()
                                Glide.with(holder.messageImageView.context)
                                    .load(downloadUrl)
                                    .into(holder.messageImageView)
                            } else {
                                Log.e(
                                    "TAG_ERROR", "error with :" +
                                            "${it.exception}"
                                )
                            }
                        }
                    } else {
                        Glide.with(holder.messageImageView.context)
                            .load(model.getImageUrl()!!)
                            .into(holder.messageImageView)
                    }
                    holder.messageImageView.visibility = ImageView.VISIBLE
                    holder.messageTextView.visibility = TextView.GONE
                }
                holder.messengerTextView.text = model.getName()
                if (model.getPhotoUrl() == null) {
                    holder.messengerImageCircle.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@MainMessage, android.R.drawable.ic_menu_gallery
                        )
                    )
                } else {
                    Glide.with(this@MainMessage).load(model.getPhotoUrl())
                        .into(holder.messengerImageCircle)
                }
            }
        }
        firebaseAdapter!!.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    val friendlyMessageCount = firebaseAdapter!!.itemCount
                    val lastVisiblePosition = llManager!!
                        .findLastCompletelyVisibleItemPosition()
                    if ((lastVisiblePosition == -1 || (
                                positionStart >= (friendlyMessageCount - 1) &&
                                        lastVisiblePosition == (positionStart - 1)))
                    ) {
                        rcView!!.scrollToPosition(positionStart)
                    }
                }
            })
        rcView!!.adapter = firebaseAdapter
        et_message = findViewById(R.id.et_messsage)
        et_message!!.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    btnSend!!.isEnabled = p0.toString().trim {
                        it <= ' '
                    }.length > 0
                }

                override fun afterTextChanged(s: Editable?) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })
        btnSend = findViewById(R.id.sendButtom)
        btnSend!!.setOnClickListener {
            val friendlyMessagex = FriendlyMessage(
                et_message!!.text.toString(),
                mUsername!!,
                mPhotoUrl!!,
                null
            )
            dbRef!!.child(MESSAGE_CHILD).push().setValue(friendlyMessagex)
            et_message!!.setText("")
        }
        img_message = findViewById(R.id.addMessage)
        img_message!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE)
        }
        fAuth = FirebaseAuth.getInstance()
        fUserAuth = fAuth!!.currentUser
        if (fUserAuth == null) {
            startActivity(
                Intent(
                    this@MainMessage,
                    MainActivity::class.java
                )
            )
            finish()
            return
        } else {
            mUsername = fUserAuth!!.displayName
            if (fUserAuth!!.photoUrl != null) {
                mPhotoUrl = fUserAuth!!.photoUrl.toString()
            }
        }
    }

    override fun onPause() {
        firebaseAdapter!!.stopListening()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        firebaseAdapter!!.startListening()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(
            this, "Google Play Services Error",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK)
                if (data != null) {
                    val uri = data.data
                    val tempMessage = FriendlyMessage(
                        null, mUsername!!,
                        mPhotoUrl!!, LOADING_IMAGE_URL
                    )
                    dbRef!!.child(MESSAGE_CHILD).push()
                        .setValue(tempMessage, object : DatabaseReference.CompletionListener {
                            override fun onComplete(p0: DatabaseError?, p1: DatabaseReference) {
                                if (p0 == null) {
                                    val key = p1.key
                                    val storageReference = FirebaseStorage.getInstance()
                                        .getReference(fUserAuth!!.uid)
                                        .child(key!!)
                                        .child(uri.lastPathSegment!!)
                                    putImageInStorage(storageReference, uri, key)
                                } else {
                                    e("TAGERROR", "${p0.toException()}")
                                }
                            }

                        })
                }
        }
    }

    private fun putImageInStorage(storageReference: StorageReference, uri: Uri?, key: String) {
        storageReference.putFile(uri!!)
            .addOnCompleteListener(this@MainMessage,
                object : OnCompleteListener<UploadTask.TaskSnapshot> {
                    override fun onComplete(p0: Task<UploadTask.TaskSnapshot>) {
                        if (p0.isSuccessful) {
                            p0.result!!.metadata!!.reference!!.downloadUrl
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        val friendMSG = FriendlyMessage(
                                            null, mUsername!!, mPhotoUrl!!,
                                            it.result.toString()
                                        )
                                        dbRef!!.child(MESSAGE_CHILD).child(key)
                                            .setValue(friendMSG)
                                    } else {
                                        w(
                                            "TAGWARN", "image uploaded" +
                                                    "but data doesn't", it.exception
                                        )
                                    }
                                }
                        }
                    }

                })
    }
}