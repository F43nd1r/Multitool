package com.faendir.lightning_launcher.multitool.proxy

import com.faendir.lightning_launcher.multitool.util.LightningObjectFactory.EvalFunction
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.util.*

/**
 * @author lukas
 * @since 04.07.18
 */
object ProxyFactory {

    internal fun <T : Proxy> lightningProxy(lightningObject: Any, interfaceClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return java.lang.reflect.Proxy.newProxyInstance(ProxyFactory::class.java.classLoader, arrayOf<Class<*>>(interfaceClass), JavaProxyInvocationHandler(lightningObject)) as T
    }

    internal fun evalProxy(eval: EvalFunction): Lightning {
        return java.lang.reflect.Proxy.newProxyInstance(ProxyFactory::class.java.classLoader, arrayOf<Class<*>>(Lightning::class.java), EvalProxyInvocationHandler(eval)) as Lightning
    }

    fun <T : Proxy> cast(proxy: Proxy, interfaceClass: Class<T>): T {
        return lightningProxy(proxy.real, interfaceClass)
    }

    private class JavaProxyInvocationHandler internal constructor(lightningObject: Any) : BaseProxyInvocationHandler(lightningObject) {
        private val clazz: Class<*>
        private val invokeOn: Any?

        init {
            val isClass = lightningObject is Class<*>
            clazz = if (isClass) lightningObject as Class<*> else lightningObject.javaClass
            invokeOn = if (isClass) null else lightningObject
        }

        @Throws(Exception::class)
        override fun doInvoke(methodName: String, parameterTypes: Array<Class<*>>, parameters: Array<Any?>): Any? = clazz.getMethod(methodName, *parameterTypes).invoke(invokeOn, *parameters)
    }

    private class EvalProxyInvocationHandler internal constructor(private val eval: EvalFunction) : BaseProxyInvocationHandler(eval) {
        override fun doInvoke(methodName: String, parameterTypes: Array<Class<*>>, parameters: Array<Any?>): Any? = eval.eval(methodName, *parameters)
    }

    private abstract class BaseProxyInvocationHandler protected constructor(private val `object`: Any) : InvocationHandler {

        @Throws(Throwable::class)
        override fun invoke(proxy: Any, method: Method, arguments: Array<Any?>?): Any? {
            val args = arguments ?: arrayOfNulls(0)
            if ("getReal" == method.name) {
                return `object`
            }
            var result: Any?
            val parameterTypes = method.parameterTypes
            try {
                for (i in parameterTypes.indices) {
                    val type = parameterTypes[i]
                    if (Proxy::class.java.isAssignableFrom(type)) {
                        args[i] = (args[i] as Proxy).real
                        parameterTypes[i] = findClassWithSimpleNameInHierarchy(type.simpleName, args[i]?.javaClass)
                    }
                }
                result = doInvoke(method.name, parameterTypes, args)
            } catch (e: NoSuchMethodException) {
                throw NoSuchMethodException(`object`.javaClass.name + "#" + method.name + " " + Arrays.toString(parameterTypes))
            }

            if(result != null) {
                if (Proxy::class.java.isAssignableFrom(method.returnType)) {
                    @Suppress("UNCHECKED_CAST")
                    result = lightningProxy(result, method.returnType as Class<out Proxy>)
                } else if (method.returnType.isArray && Proxy::class.java.isAssignableFrom(method.returnType.componentType!!)) {
                    @Suppress("UNCHECKED_CAST")
                    val componentType = method.returnType.componentType as Class<out Proxy>
                    @Suppress("UNCHECKED_CAST")
                    result = (result as Array<Any>).map { `object` -> lightningProxy(`object`, componentType) }.toTypedArray()
                }
            }
            return result
        }

        @Throws(Exception::class)
        protected abstract fun doInvoke(methodName: String, parameterTypes: Array<Class<*>>, parameters: Array<Any?>): Any?

        private fun findClassWithSimpleNameInHierarchy(name: String, check: Class<*>?): Class<*>? {
            when {
                check == null -> return null
                check.simpleName == name -> return check
                else -> {
                    for (i in check.interfaces) {
                        val result = findClassWithSimpleNameInHierarchy(name, i)
                        if (result != null) {
                            return result
                        }
                    }
                    return findClassWithSimpleNameInHierarchy(name, check.superclass)
                }
            }
        }
    }
}
