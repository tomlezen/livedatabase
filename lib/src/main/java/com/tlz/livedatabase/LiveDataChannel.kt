package com.tlz.livedatabase

import android.arch.lifecycle.*
import kotlinx.coroutines.experimental.channels.LinkedListChannel
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel

/**
 * Created by tomlezen.
 * Data: 2018/5/8.
 * Time: 11:31.
 */
class LiveDataChannel<T>(private val liveData: LiveData<T>) :
    LinkedListChannel<T?>(), SubscriptionReceiveChannel<T?>, Observer<T?>, LifecycleObserver {

  override fun onChanged(t: T?) {
    offer(t)
  }

  override fun afterClose(cause: Throwable?) = liveData.removeObserver(this)

  @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
  fun onDestroy() = close()

}

fun <T> LiveData<T>.observeChannel(owner: LifecycleOwner): LiveDataChannel<T> {
  val channel = LiveDataChannel(this)
  observe(owner, channel)
  owner.lifecycle.addObserver(channel)
  return channel
}

fun <T> LiveData<T>.observeChannel(): LiveDataChannel<T> {
  val channel = LiveDataChannel(this)
  observeForever(channel)
  return channel
}