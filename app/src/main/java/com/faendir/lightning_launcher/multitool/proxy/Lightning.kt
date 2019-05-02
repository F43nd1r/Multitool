package com.faendir.lightning_launcher.multitool.proxy

/**
 * @author lukas
 * @since 05.07.18
 */
interface Lightning : Proxy {
    val event: Event

    val variables: VariableSet

    val activeScreen: Screen

    val currentScript: Script

    fun save()

    fun getScriptByPathAndName(path: String, name: String): Script?

    fun createScript(path: String, name: String, text: String, flags: Int): Script

    fun getScriptById(id: String): Script

    fun deleteScript(script: Script)

    fun getAllScriptMatching(flags: Int): Array<Script>
}
