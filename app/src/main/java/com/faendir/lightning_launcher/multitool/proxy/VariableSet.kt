package com.faendir.lightning_launcher.multitool.proxy

/**
 * @author lukas
 * @since 05.07.18
 */
interface VariableSet : Proxy {
    fun edit(): VariableEditor

    fun getString(name: String): String?
}
