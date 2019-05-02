package com.faendir.lightning_launcher.multitool.proxy

import android.content.Context
import androidx.annotation.StringDef

/**
 * @author lukas
 * @since 04.07.18
 */
interface Image : Proxy {
    val width: Int

    val height: Int

    @get:Type
    val type: String

    interface Class : Proxy {

        fun createImage(width: Int, height: Int): ImageBitmap

        fun createImage(pkg: String, name: String): Image

        companion object {
            operator fun get(context: Context): Class {
                try {
                    return ProxyFactory.lightningProxy(context.classLoader.loadClass("net.pierrox.lightning_launcher.script.api.Image"), Class::class.java)
                } catch (e: ClassNotFoundException) {
                    throw RuntimeException(e)
                }

            }
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(TYPE_BITMAP)
    annotation class Type

    companion object {
        const val TYPE_BITMAP = "BITMAP"
    }
}
