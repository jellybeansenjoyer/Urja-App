package com.example.urja.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.urja.R
import com.example.urja.util.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class TweetActivity : AppCompatActivity() {
   lateinit private var tweetProgressLayout:LinearLayout
   lateinit private var tweetImage: ImageView
   lateinit private var tweetText: TextView
   private val firebaseDB = FirebaseFirestore.getInstance()
   private val firebaseStorage = FirebaseStorage.getInstance().reference
   private var imageUrl: String? = null
   private var userId: String? = null
   private var userName: String? = null


   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_tweet)


      if (intent.hasExtra(PARAM_USER_ID) && intent.hasExtra(PARAM_USER_NAME)) {
         userId = intent.getStringExtra((PARAM_USER_ID))
         userName = intent.getStringExtra((PARAM_USER_NAME))
      } else {
         Toast.makeText(this, "Error creating tweet", Toast.LENGTH_SHORT).show()
         finish()
      }

      tweetProgressLayout.setOnTouchListener { v, event -> true }
   }


   // Add Tweet Image
   fun addImage(view: View) {
      /** The Intent refers to the intent action for picking the Photo from the device's storage */
      val intent = Intent(Intent.ACTION_PICK)
      intent.type = "image/*"
      // when starting for activityResult, override onActivityResult()
      startActivityForResult(intent, REQUEST_CODE_PHOTO)
   }

   /** Process onActivityResult */
   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      super.onActivityResult(requestCode, resultCode, data)
      if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
         storeImage(data?.data)
      }
   }

   private fun storeImage(imageUri: Uri?) {
      imageUri?.let {
         tweetProgressLayout.visibility = View.VISIBLE

         // Create 'TweetImages' folder in STORAGE
         val filePath = firebaseStorage.child(DATA_TWEET_IMAGES).child(userId!!)

         // Save into Storage
         filePath.putFile(imageUri)
            .addOnSuccessListener {

               // Get url from Storage >> DB
               filePath.downloadUrl
                  .addOnSuccessListener { uri ->

                     imageUrl = uri.toString()
                     tweetImage.loadUrl(imageUrl, R.drawable.logo)

                     tweetProgressLayout.visibility = View.GONE
                  }
                  .addOnFailureListener {
                     onTweetImageFail()
                  }
            }
            .addOnFailureListener {
               onTweetImageFail()
            }
      }
   }

   private fun onTweetImageFail() {
      Toast.makeText(this, "Image tweet failed. Try again..", Toast.LENGTH_SHORT)
         .show()

      tweetProgressLayout.visibility = View.GONE
   }


   // -----------------------------------------//
   // Post a Tweet
   fun postTweet(view: View) {
      tweetProgressLayout.visibility = View.VISIBLE

      // GET: data
      val text = tweetText.text.toString()
      val hashtags = getHashtags(text)

      // POST: to update DB
      val tweetId = firebaseDB.collection(DATA_TWEETS).document() // Create Tweets Collection
      val tweet = Tweet(      // Tweet model
         tweetId.id,
         arrayListOf(userId!!),
         userName,
         text,
         imageUrl,
         System.currentTimeMillis(),
         hashtags,
         arrayListOf()
      )

      tweetId.set(tweet)
         .addOnCompleteListener { finish() }
         .addOnFailureListener {
            it.printStackTrace()
            tweetProgressLayout.visibility = View.GONE
            Toast.makeText(this, "Failed to post tweet", Toast.LENGTH_SHORT).show()
         }
   }

   private fun getHashtags(source: String): ArrayList<String> {
      val hashtags = arrayListOf<String>()
      var text = source   // assign source into variable

      while (text.contains("#")) {
         var hashtag = ""
         val hash = text.indexOf("#")
         text = text.substring(hash + 1)

         val firstSpace = text.indexOf(" ")
         val firstHash = text.indexOf("#")

         // Filter source of '#' and 'space'  -- Can use RegEx
         if (firstSpace == -1 && firstHash == -1) {
            hashtag = text.substring(0)
         } else if (firstSpace != -1 && firstSpace < firstHash) {
            hashtag = text.substring(0, firstSpace)
            text = text.substring(firstSpace + 1)
         } else {
            hashtag = text.substring(0, firstHash)
            text = text.substring(firstHash)
         }

         // Add filtered source into List
         if (!hashtag.isNullOrEmpty()) {
            hashtags.add(hashtag)
         }
      }

      return hashtags
   }


   //--- Intent
   companion object {
      val PARAM_USER_ID = "UserId"
      val PARAM_USER_NAME = "UserName"

      fun newIntent(context: Context, userId: String?, userName: String?): Intent {
         val intent = Intent(context, TweetActivity::class.java)
         intent.putExtra(PARAM_USER_ID, userId)
         intent.putExtra(PARAM_USER_NAME, userName)

         return intent
      }
   }
}
