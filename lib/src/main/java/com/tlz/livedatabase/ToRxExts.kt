package com.tlz.livedatabase

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import io.reactivex.*
import io.reactivex.android.MainThreadDisposable

/**
 * Created by tomlezen.
 * Data: 2018/5/8.
 * Time: 16:06.
 */

class LiveDataObserver<T>(private val data: LiveData<T>, private val observer: Observer<in T>) : MainThreadDisposable(), android.arch.lifecycle.Observer<T> {

  override fun onDispose() {
    data.removeObserver(this)
  }

  override fun onChanged(t: T?) {
    observer.onNext(t!!)
  }
}

class LiveDataObservable<T>(private val owner: LifecycleOwner?, private val data: LiveData<T>) : Observable<T>() {

  override fun subscribeActual(observer: Observer<in T>) {
    val liveDataObserver = LiveDataObserver(data, observer)
    observer.onSubscribe(liveDataObserver)
    if (owner == null) {
      data.observeForever(liveDataObserver)
    } else {
      data.observe(owner, liveDataObserver)
    }
  }
}

class LiveDataSingleObserver<T>(private val data: LiveData<T>, private val observer: SingleObserver<in T>) : MainThreadDisposable(), android.arch.lifecycle.Observer<T> {
  override fun onDispose() {
    data.removeObserver(this)
  }

  override fun onChanged(t: T?) {
    observer.onSuccess(t!!)
    data.removeObserver(this)
  }
}

class LiveDataSingle<T>(private val owner: LifecycleOwner?, private val data: LiveData<T>) : Single<T>() {

  override fun subscribeActual(observer: SingleObserver<in T>) {
    val liveDataObserver = LiveDataSingleObserver(data, observer)
    if (owner == null) {
      data.observeForever(liveDataObserver)
    } else {
      data.observe(owner, liveDataObserver)
    }
  }

}

fun <T> LiveData<T>.toRxObservable(owner: LifecycleOwner?): Observable<T> =
    LiveDataObservable(owner, this)

fun <T> LiveData<T>.toRxFlowable(owner: LifecycleOwner?, strategy: BackpressureStrategy): Flowable<T> =
    toRxObservable(owner).toFlowable(strategy)

fun <T> LiveData<T>.toRxSingle(owner: LifecycleOwner?): Single<T> =
    LiveDataSingle(owner, this)

fun <T> LiveData<T>.toRxMaybe(owner: LifecycleOwner?): Maybe<T> =
    toRxSingle(owner).toMaybe()

