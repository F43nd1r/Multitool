package com.faendir.lightning_launcher.multitool.proxy

/**
 * @author lukas
 * @since 04.07.18
 */
interface PropertyEditor : Proxy {
    fun setBoolean(@PropertySet.BooleanProperty name: String, value: Boolean): PropertyEditor

    fun commit()

    fun getBox(@PropertySet.BoxProperty name: String): Box

    fun setEventHandler(@PropertySet.EventProperty name: String, action: Int, data: String?): PropertyEditor

    fun setString(@PropertySet.StringProperty name: String, value: String): PropertyEditor

    fun setInteger(@PropertySet.IntProperty name: String, value: Long): PropertyEditor

    fun setEventHandler(@PropertySet.EventProperty name: String, eventHandler: EventHandler): PropertyEditor
}
