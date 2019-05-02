package com.faendir.lightning_launcher.multitool.proxy

import android.content.Intent
import android.view.View
import androidx.annotation.Keep

/**
 * @author lukas
 * @since 09.07.18
 */
interface JavaScript {
    @Keep
    interface CreateMenu : JavaScript {
        fun showMenu(jsMenu: Any, jsItem: Any) {
            showMenu(ProxyFactory.lightningProxy(jsMenu, Menu::class.java), ProxyFactory.lightningProxy(jsItem, Item::class.java))
        }

        fun showMenu(menu: Menu, item: Item)
    }

    @Keep
    interface ActivityResult : JavaScript {
        fun onActivityResult(resultCode: Int, data: Intent, token: String)
    }

    @Keep
    interface CreateCustomView : JavaScript {
        fun onCreate(jsItem: Any): View {
            return onCreate(ProxyFactory.lightningProxy(jsItem, CustomView::class.java))
        }

        fun onCreate(item: CustomView): View
    }

    @Keep
    interface Setup : JavaScript {
        fun setup()
    }

    @Keep
    interface Normal : JavaScript {
        fun run()
    }

    @Keep
    interface Direct : JavaScript {

        fun execute(data: String): String

        companion object {
            const val PARAM_CLASS = "multitool\$classname"
            const val PARAM_DATA = "multitool\$data"
        }
    }

    @Keep
    interface Listener : JavaScript {
        fun register()

        fun unregister()

        fun handleCommand(command: String) {}
    }
}
