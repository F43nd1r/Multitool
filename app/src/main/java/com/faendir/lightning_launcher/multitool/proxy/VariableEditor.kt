package com.faendir.lightning_launcher.multitool.proxy

/**
 * @author lukas
 * @since 05.07.18
 */
interface VariableEditor : Proxy {
    fun setString(name: String, value: String): VariableEditor

    fun commit()
}
