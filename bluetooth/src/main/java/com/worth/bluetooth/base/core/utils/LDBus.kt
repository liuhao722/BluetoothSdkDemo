package com.worth.bluetooth.base.core.utils

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.jvm.Throws

/**
 * liveDataBus 功能封装
 */
object LDBus {

    private val events: HashMap<String, EventMutableLiveData<Any?>> = HashMap()

    // 同步异步参数控制
    private const val sync = 1
    private const val async = 2

    private val map : HashMap<String, Function1<Any,Any>> = HashMap()
    private val map2 : HashMap<String, Function2<Any,Any,Any>> = HashMap()

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            try {
                when (msg.what) {
                    sync -> {
                        val e = msg.obj as LDBusEventMessage<*>
                        e.observer.value = e.data
                    }
                    async -> {
                        send(msg.obj as LDBusEvent<*>)
                    }
                }
            } catch (e: java.lang.Exception) {
                e.stackTrace
            }
        }
    }

    /**
     * 主线程立即发送消息
     */
    fun send(event: LDBusEvent<*>) = observer(event.key, Any::class.java).run {
        event.data?.let {
            if (isMainThread()) {
                value = event.data
            } else {
                handler.sendMessage(Message.obtain().apply {
                    what = sync
                    obj = LDBusEventMessage(this@run, event.data)
                })
            }
        }
    }

    fun observer(key: String, block : (Any)->Unit){
        map[key] = block
    }

    fun  sendSpecial(key: String, data : Any) {
        map[key]?.run {
            invoke(data)
        }
    }

    /**
     * 新增 增加两个回调参数的返回值类型--提供给app端使用的一个方法
     */
    fun observer2(key: String, block : (Any, Any)->Unit){
        map2[key] = block
    }

    /**
     * 新增 增加两个参数的发送类型--提供给app端使用的一个方法
     */
    fun  sendSpecial2(key: String, data : Any, params: Any) {
        map2[key]?.run {
            invoke(data, params)
        }
    }

    fun remove(key: String){
        map.remove(key)
    }

    /**
     * 异步线程立即发送内容
     */
    fun post(event: LDBusEvent<*>) = observer(event.key, Any::class.java).run {
        event.data?.run {
            postValue(event.data)
        }
    }

    /**
     * 延迟发送内容[event]接收者关键字，[delayTime]延迟时间 单位ms
     */
    fun postDelay(event: LDBusEvent<*>, delayTime: Long = 0) {
        handler.sendMessageDelayed(Message.obtain().apply {
            what = async
            obj = event
        }, delayTime)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> observer(key: String, type: Class<T>? = null): MutableLiveData<T> {
        // 不存在添加
        if (!events.containsKey(key)) {
            events[key] = EventMutableLiveData()
        }
        // 存在直接返回
        return events[key] as MutableLiveData<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> observer(event: LDBusEvent<T>): MutableLiveData<T> {
        // 不存在添加
        if (!events.containsKey(event.key)) {
            events[event.key] = EventMutableLiveData()
        }
        // 存在直接返回
        return events[event.key] as MutableLiveData<T>
    }
}

private class EventMutableLiveData<T> : MutableLiveData<T>() {

    private val observerMap: MutableMap<Observer<in T>, Observer<T>> = HashMap()

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, observer)
        try {
            hook(observer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
    override fun observeForever(observer: Observer<in T>) {
        if (!observerMap.containsKey(observer)) {
            observerMap[observer] = ObserverWrapper(observer)
        }
        super.observeForever(observer)
    }

    override fun removeObserver(observer: Observer<in T>) {
        val removeObserver: Observer<in T>? = if (observerMap.containsKey(observer)) {
            observerMap.remove(observer)
        } else {
            observer
        }
        removeObserver?.run {
            super.removeObserver(removeObserver)
        }
    }

    @Throws(java.lang.Exception::class)
    private fun hook(@NonNull observer: Observer<in T>) {
        //get wrapper's version
        val classLiveData = LiveData::class.java
        val fieldObservers: Field = classLiveData.getDeclaredField("mObservers")
        fieldObservers.isAccessible = true
        val objectObservers: Any? = fieldObservers.get(this)
        val classObservers: Class<*>? = objectObservers?.javaClass
        val methodGet: Method? = classObservers?.getDeclaredMethod("get", Any::class.java)
        methodGet?.isAccessible = true
        val objectWrapperEntry: Any? = methodGet?.invoke(objectObservers, observer)
        var objectWrapper: Any? = null
        if (objectWrapperEntry is Map.Entry<*, *>) {
            objectWrapper = objectWrapperEntry.value
        }
        if (objectWrapper == null) {
            throw NullPointerException("Wrapper can not be null!")
        }
        val classObserverWrapper: Class<*>? = objectWrapper.javaClass.superclass
        val fieldLastVersion: Field = classObserverWrapper!!.getDeclaredField("mLastVersion")
        fieldLastVersion.isAccessible = true
        //get livedata's version
        val fieldVersion: Field = classLiveData.getDeclaredField("mVersion")
        fieldVersion.isAccessible = true
        val objectVersion: Any? = fieldVersion.get(this)
        //set wrapper's version
        fieldLastVersion.set(objectWrapper, objectVersion)
    }

}

private class ObserverWrapper<T>(private var observer: Observer<in T>?) : Observer<T> {

    override fun onChanged(t: T) {
        observer?.run {
            if (isCall()) {
                return
            }
            onChanged(t)
        }
    }

    private fun isCall(): Boolean {
        val stackTrace: Array<StackTraceElement>? = Thread.currentThread().stackTrace
        if (stackTrace != null && stackTrace.isNotEmpty()) {
            for (element in stackTrace) {
                if ("android.arch.lifecycle.LiveData" == element.className && "observeForever" == element.methodName) {
                    return true
                }
            }
        }
        return false
    }
}

data class LDBusEvent<T>(val key: String, val type: Class<T>? = null, var data: Any? = null)

data class LDBusEventMessage<T>(val observer: MutableLiveData<T>, var data: Any?)

fun isMainThread() = Looper.getMainLooper().thread == Thread.currentThread()

class LDBusEntity(val key: String?){}