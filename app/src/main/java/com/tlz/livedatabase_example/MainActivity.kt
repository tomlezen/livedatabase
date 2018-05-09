package com.tlz.livedatabase_example

import android.arch.lifecycle.MutableLiveData
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.tlz.livedatabase.*

class MainActivity : AppCompatActivity() {

  private val liveData = MutableLiveData<Int>()
  private val liveData2 = MutableLiveData<Int>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)


    liveData.combine(liveData2, { v1, v2 -> v1 + v2 })
        .doOnValue { println("doOnValue") }
        .doOnActive { println("doOnActive") }
        .doOnInactive { println("doOnInactive") }
        .take(5)
        .observeForever {
          println(it)
        }

    (0 until 10).forEach {
      liveData.value = it
      liveData2.value = it + 1
    }
  }
}
