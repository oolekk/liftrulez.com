package com.liftrulez.lib
import scala.collection.mutable.LinkedHashMap

class EasyCache[K, V](key: K, value: V, maxSize: Int) {
  require(maxSize > 1)
  var isFull = false
  val store = LinkedHashMap() += ((key, value))

  def put(k: K, v: V) {
    if (isFull) {
      store.remove(store.head._1)
      store += ((k, v))
    } else {
      store += ((k, v))
      if (store.size == maxSize) isFull = true
    }
  }
  def get(k: K) = store.get(k)
}
