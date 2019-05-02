package com.faendir.lightning_launcher.multitool.fastadapter

import android.content.Context

/**
 * @author F43nd1r
 * @since 13.10.2017
 */

interface DeletableModel : Model {
    fun getUndoText(context: Context): String
    override var name : String
}
