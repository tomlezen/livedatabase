package com.tlz.livedatabase

import android.arch.lifecycle.*
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView

/**
 * Created by tomlezen.
 * Data: 2018/5/8.
 * Time: 11:44.
 */
fun <T> LiveData<T>.observe(owner: LifecycleOwner, observer: (T?) -> Unit) {
  observe(owner, { observer(it) })
}

fun <T> LiveData<T>.observeNotNull(owner: LifecycleOwner, observer: (T) -> Unit) {
  filterNull().observe(owner, { observer(it!!) })
}

fun <T> LiveData<T>.observeForever(observer: (T?) -> Unit) {
  observeForever({ observer(it) })
}

fun <T> LiveData<T>.observeNotNullForever(observer: (T) -> Unit) {
  filterNull().observeForever({ observer(it!!) })
}

fun <T> LiveData<T>.doOnValue(func: (T?) -> Unit) =
    MediatorLiveData<T>().apply {
      addSource(this@doOnValue) {
        value = it
        func.invoke(it)
      }
    }

fun <T> LiveData<T>.doOnActive(func: () -> Unit) =
    MediatorLiveData<T>().apply {
      addSource(this@doOnActive) {
        value = it
      }
      addSource(object : LiveData<T>() {
        override fun onActive() {
          func.invoke()
        }
      }) {}
    }

fun <T> LiveData<T>.doOnInactive(func: () -> Unit) =
    MediatorLiveData<T>().apply {
      addSource(this@doOnInactive) {
        value = it
      }
      addSource(object : LiveData<T>() {
        override fun onInactive() {
          func.invoke()
        }
      }) {}
    }

fun <T> LiveData<T>.merge(vararg sources: LiveData<T>): LiveData<T> {
  if (sources.isEmpty()) return this
  return MediatorLiveData<T>().apply {
    val observer = Observer<T> {
      value = it
    }
    for (source in sources) {
      addSource(source, observer)
    }
  }
}

fun <T, R> LiveData<T>.map(func: (T) -> R): LiveData<R> =
    Transformations.map(this, func)

fun <T, R> LiveData<T>.switchMap(func: (T) -> LiveData<R>): LiveData<R> =
    Transformations.switchMap(this, func)

fun <T1, T2, R> LiveData<T1>.combine(source: LiveData<T2>, combiner: (T1, T2) -> R): LiveData<R> =
    MediatorLiveData<R>().apply {
      var emits = 0
      val sources = listOf(this@combine, source)
      val size = sources.size
      val emptyVal = Any()
      val values = arrayOfNulls<Any?>(2)

      fun reset(){
        emits = 0
        (0 until size).forEach { values[it] = emptyVal }
      }

      reset()
      (0 until size).forEach {
        val observer = Observer<Any> { t ->
          var combine = emits == size
          if (!combine) {
            if (values[it] == emptyVal) emits++
            combine = emits == size
          }
          values[it] = t

          if (combine) {
            value = combiner.invoke(values[0] as T1, values[1] as T2)
            reset()
          }
        }
        addSource(sources[it] as LiveData<Any>, observer)
      }
    }

fun <T> LiveData<T>.filter(func: (T?) -> Boolean): LiveData<T> =
    MediatorLiveData<T>().apply {
      addSource<T>(this@filter) { if (func(it)) value = it }
    }

fun <T> LiveData<T>.filterNull(): LiveData<T> = filter { it != null }

fun <T> LiveData<T>.buffer(count: Int, skip: Int = 0): LiveData<List<T>> =
    MediatorLiveData<List<T>>().apply {
      val buffer = mutableListOf<T>()
      val result = MediatorLiveData<List<T>>()
      var counter = 0
      result.addSource(this@buffer) {
        if (counter < count) {
          buffer.add(it!!)
        }
        counter++
        if (counter == skip) {
          counter = 0
          result.value = buffer
        }
      }
    }

fun <T> LiveData<T>.take(count: Int): LiveData<T> {
  if (count <= 0) return object : LiveData<T>() {}
  var counter = 0
  return takeUntil { ++counter >= count }
}

fun <T> LiveData<T>.takeWhile(predicate: (T?) -> Boolean): LiveData<T> =
    MediatorLiveData<T>().apply {
      addSource(this@takeWhile) {
        if (predicate.invoke(it)) {
          value = it
        } else {
          removeSource(this@takeWhile)
        }
      }
    }

fun <T> LiveData<T>.takeUntil(predicate: (T?) -> Boolean): LiveData<T> =
    MediatorLiveData<T>().apply {
      addSource(this@takeUntil) {
        if (predicate.invoke(it)) {
          removeSource(this@takeUntil)
        }
        value = it
      }
    }


fun <T : View> T.bindEnable(owner: LifecycleOwner, liveData: LiveData<Boolean>) {
  liveData.observeNotNull(owner) { isEnabled = it }
}

fun <T : View> T.bindClikable(owner: LifecycleOwner, liveData: LiveData<Boolean>) {
  liveData.observeNotNull(owner) { isClickable = it }
}

fun <T : View> T.bindAlpha(owner: LifecycleOwner, liveData: LiveData<Float>) {
  liveData.observeNotNull(owner) { alpha = it }
}

fun <T : View> T.bindFocus(owner: LifecycleOwner, liveData: LiveData<Boolean>) {
  liveData.observeNotNull(owner) {
    if (it) {
      requestFocus()
    } else {
      clearFocus()
    }
  }
}

fun <T : View> T.bindFocusable(owner: LifecycleOwner, liveData: LiveData<Boolean>) {
  liveData.observeNotNull(owner) { isFocusable = it }
}

fun <T : View> T.bindSelect(owner: LifecycleOwner, liveData: LiveData<Boolean>) {
  liveData.observeNotNull(owner) { isSelected = it }
}

fun <T : View> T.bindVisible(owner: LifecycleOwner, liveData: LiveData<Boolean>) {
  liveData.observeNotNull(owner) { visibility = if (it == true) View.VISIBLE else View.GONE }
}

fun <T : View> T.bindVisible2(owner: LifecycleOwner, liveData: LiveData<Int>) {
  liveData.observeNotNull(owner) { visibility = it }
}

fun <T : View> T.bindBackgroundResource(owner: LifecycleOwner, liveData: LiveData<Int>) {
  liveData.observeNotNull(owner) { setBackgroundResource(it) }
}

fun <T : TextView> T.bindText(owner: LifecycleOwner, liveData: LiveData<CharSequence>) {
  liveData.observe(owner) { text = it }
}

fun <T : View> T.bindBackgroundColor(owner: LifecycleOwner, liveData: LiveData<Int>) {
  liveData.observeNotNull(owner) { setBackgroundColor(it) }
}

fun <T : ImageView> T.bindImageBitmap(owner: LifecycleOwner, liveData: LiveData<Bitmap>) {
  liveData.observe(owner) { setImageBitmap(it) }
}

fun <T : ImageView> T.bindImageDrawable(owner: LifecycleOwner, liveData: LiveData<out Drawable>) {
  liveData.observe(owner) { setImageDrawable(it) }
}

fun <T : ImageView> T.bindImageResource(owner: LifecycleOwner, liveData: LiveData<Int>) {
  liveData.observeNotNull(owner) { setImageResource(it) }
}

fun <T : ImageView> T.bindImageLevel(owner: LifecycleOwner, liveData: LiveData<Int>) {
  liveData.observeNotNull(owner) { setImageLevel(it) }
}