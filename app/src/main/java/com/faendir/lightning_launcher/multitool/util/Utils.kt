package com.faendir.lightning_launcher.multitool.util

import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import androidx.annotation.RawRes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.acra.util.StreamReader

import java.io.IOException

/**
 * @author F43nd1r
 * @since 09.11.2016
 */

object Utils {
    val GSON: Gson = GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(Intent::class.java, IntentTypeAdapter())
            .registerTypeAdapter(ParcelUuid::class.java, ParcelUuidTypeAdapter())
            .create()

    class IntentTypeAdapter : TypeAdapter<Intent>() {

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: Intent?) {
            writer.beginObject()
            writer.name(URI)
            if (value == null) {
                writer.nullValue()
            } else {
                writer.value(value.toUri(0))
            }
            writer.endObject()
        }

        @Throws(IOException::class)
        override fun read(reader: JsonReader): Intent? {
            var intent: Intent? = null
            reader.beginObject()
            while (reader.hasNext()) {
                if (URI == reader.nextName()) {
                    intent = Intent.parseUri(reader.nextString(), 0)
                }
            }
            reader.endObject()
            return intent
        }

        companion object {
            private const val URI = "uri"
        }
    }

    class ParcelUuidTypeAdapter : TypeAdapter<ParcelUuid>() {
        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: ParcelUuid) {
            writer.value(value.toString())
        }

        @Throws(IOException::class)
        override fun read(reader: JsonReader): ParcelUuid {
            return ParcelUuid.fromString(reader.nextString())
        }
    }

    fun readRawResource(context: Context, @RawRes res: Int): String {
        return StreamReader(context.resources.openRawResource(res)).read()
    }
}
