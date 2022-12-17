package com.example.urja.listeners

import com.example.urja.util.Tweet

interface
ITweetListener {
   fun onLayoutClick(tweet: Tweet?)
   fun onLike(tweet: Tweet?)
   fun onRetweet(tweet: Tweet?)
}