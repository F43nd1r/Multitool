package com.faendir.lightning_launcher.multitool.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.fastadapter.ExpandableItem
import com.faendir.lightning_launcher.multitool.fastadapter.ItemFactory
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IInterceptor
import com.mikepenz.fastadapter.adapters.ModelAdapter
import com.mikepenz.fastadapter.utils.ComparableItemListImpl
import org.acra.ACRA
import java.util.*

/**
 * @author F43nd1r
 * @since 07.11.2017
 */

class IntentChooserFragment : Fragment(), SearchView.OnQueryTextListener {
    private lateinit var adapter: ModelAdapter<IntentInfo, ExpandableItem<IntentInfo>>
    private lateinit var fastAdapter: FastAdapter<ExpandableItem<IntentInfo>>
    private var search: String? = null

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.IntentChooserFragment, 0, 0)
        val action: String?
        val category: String?
        val indirect: Boolean
        try {
            action = a.getString(R.styleable.IntentChooserFragment_intent_action)
            category = a.getString(R.styleable.IntentChooserFragment_intent_category)
            indirect = a.getBoolean(R.styleable.IntentChooserFragment_intent_indirect, false)
        } finally {
            a.recycle()
        }
        if (action != null) {
            val intent = Intent(action)
            if (category != null) {
                intent.addCategory(category)
            }
            val args = Bundle()
            args.putParcelable(KEY_INTENT, intent)
            args.putBoolean(KEY_INDIRECT, indirect)
            arguments = args
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.intent_chooser_page, container, false)
        val args = arguments
        if (args != null) {
            adapter = ModelAdapter(ComparableItemListImpl(compareBy<ExpandableItem<IntentInfo>> { it.model }),
                    IInterceptor<IntentInfo, ExpandableItem<IntentInfo>> { ItemFactory.forLauncherIconSize<IntentInfo>(activity!!).wrap(it) })
            fastAdapter = FastAdapter.with(adapter)
            fastAdapter.withOnClickListener { _, _, item, _ ->
                val info = item.model
                when {
                    info.isIndirect -> startActivityForResult(info.intent, 0)
                    else -> setResult(info.intent, info.name)
                }
                true
            }

            adapter.itemFilter.withFilterPredicate { item, constraint -> !item.model.name.toLowerCase().contains(constraint.toString().toLowerCase()) }
            val intent = args.getParcelable<Intent>(KEY_INTENT)!!
            val indirect = args.getBoolean(KEY_INDIRECT)
            IntentHandlerListTask(activity!!, intent, indirect) { infos ->
                if (root.parent != null) {
                    val recyclerView = root.findViewById<RecyclerView>(R.id.list)
                    adapter.set(infos)
                    val list = adapter.itemList as ComparableItemListImpl<ExpandableItem<IntentInfo>>
                    list.withComparator(list.comparator)
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.layoutManager = LinearLayoutManager(activity)
                    recyclerView.adapter = fastAdapter
                    root.findViewById<View>(R.id.progressBar).visibility = View.GONE
                }
            }.execute()
        }
        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    fun setComparator(comparator: Comparator<IntentInfo>) {
        (adapter.itemList as ComparableItemListImpl<ExpandableItem<IntentInfo>>).withComparator { e1, e2 -> comparator.compare(e1.model, e2.model) }
    }

    private fun setResult(intent: Intent, label: String) {
        val result = Intent()
        result.putExtra(Intent.EXTRA_INTENT, intent)
        result.putExtra(Intent.EXTRA_TITLE, label)
        activity?.setResult(Activity.RESULT_OK, result)
        activity?.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val intent = data.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)
            if (intent != null) {
                setResult(intent, data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME))
            } else {
                Toast.makeText(activity, R.string.toast_cantLoadAction, Toast.LENGTH_SHORT).show()
                ACRA.getErrorReporter().handleSilentException(NullPointerException("Shortcut intent was null"))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val search = menu.findItem(R.id.action_search)
        val view = search.actionView as SearchView
        view.setOnQueryTextListener(this)
        if (this.search != null && "" != this.search) {
            view.setQuery(this.search, true)
            view.isIconified = false
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        adapter.filter(query)
        search = query
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        adapter.filter(newText)
        search = newText
        return true
    }

    companion object {
        private const val KEY_INTENT = "intent"
        private const val KEY_INDIRECT = "indirect"

        fun newInstance(intent: Intent, isIndirect: Boolean): IntentChooserFragment {
            val fragment = IntentChooserFragment()
            val args = Bundle()
            args.putParcelable(KEY_INTENT, intent)
            args.putBoolean(KEY_INDIRECT, isIndirect)
            fragment.arguments = args
            return fragment
        }
    }
}
