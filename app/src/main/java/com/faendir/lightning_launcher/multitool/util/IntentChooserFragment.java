package com.faendir.lightning_launcher.multitool.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.fastadapter.ExpandableItem;
import com.mikepenz.fastadapter.commons.adapters.GenericFastItemAdapter;

import org.acra.ACRA;

import java.util.Comparator;

/**
 * @author F43nd1r
 * @since 07.11.2017
 */

public class IntentChooserFragment extends Fragment implements SearchView.OnQueryTextListener {
    private static final String KEY_INTENT = "intent";
    private static final String KEY_INDIRECT = "indirect";
    private GenericFastItemAdapter<IntentInfo, ExpandableItem<IntentInfo>> adapter;
    private String search;

    public static IntentChooserFragment newInstance(Intent intent, boolean isIndirect){
        IntentChooserFragment fragment = new IntentChooserFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_INTENT, intent);
        args.putBoolean(KEY_INDIRECT, isIndirect);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IntentChooserFragment, 0, 0);
        String action;
        String category;
        boolean indirect;
        try {
            action = a.getString(R.styleable.IntentChooserFragment_intent_action);
            category = a.getString(R.styleable.IntentChooserFragment_intent_category);
            indirect = a.getBoolean(R.styleable.IntentChooserFragment_intent_indirect, false);
        } finally {
            a.recycle();
        }
        if ((action != null)) {
            Intent intent = new Intent(action);
            if (category != null) {
                intent.addCategory(category);
            }
            Bundle args = new Bundle();
            args.putParcelable(KEY_INTENT, intent);
            args.putBoolean(KEY_INDIRECT, indirect);
            setArguments(args);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.intent_chooser_page, container, false);
        Bundle args = getArguments();
        adapter = new GenericFastItemAdapter<>(ExpandableItem::new);
        adapter.getGenericItemAdapter().withComparator((o1, o2) -> o1.getModel().compareTo(o2.getModel()));
        adapter.withOnClickListener((v, adapter1, item, position) -> handleSelection(item.getModel()));
        adapter.getGenericItemAdapter().getItemFilter().withFilterPredicate(((item, constraint) -> !item.getModel().getName().toLowerCase().contains(constraint.toString().toLowerCase())));
        Intent intent = args.getParcelable(KEY_INTENT);
        boolean indirect = args.getBoolean(KEY_INDIRECT);
        new IntentHandlerListTask(getActivity(), intent, indirect, infos -> {
            if(root.getParent() != null) {
                RecyclerView recyclerView = root.findViewById(R.id.list);
                adapter.setModel(infos);
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setAdapter(adapter);
                root.findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        }).execute();
        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void setComparator(Comparator<IntentInfo> comparator){
        adapter.getGenericItemAdapter().withComparator((o1, o2) -> comparator.compare(o1.getModel(), o2.getModel()));
        adapter.notifyAdapterDataSetChanged();
    }

    private boolean handleSelection(IntentInfo info) {
        if (info.isIndirect()) {
            startActivityForResult(info.getIntent(), 0);
        } else if (info.getIntent() != null) {
            setResult(info.getIntent(), info.getName());
        } else {
            nullIntent();
            ACRA.getErrorReporter().handleSilentException(new NullPointerException(info.getName() + " intent was null"));
        }
        return true;
    }

    private void setResult(Intent intent, String label) {
        Intent result = new Intent();
        result.putExtra(Intent.EXTRA_INTENT, intent);
        result.putExtra(Intent.EXTRA_TITLE, label);
        getActivity().setResult(Activity.RESULT_OK, result);
        getActivity().finish();
    }

    private void nullIntent() {
        Toast.makeText(getActivity(), R.string.toast_cantLoadAction, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
            if (intent != null) {
                setResult(intent, data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME));
            } else {
                nullIntent();
                ACRA.getErrorReporter().handleSilentException(new NullPointerException("Shortcut intent was null"));
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem search = menu.findItem(R.id.action_search);
        SearchView view = (SearchView) search.getActionView();
        view.setOnQueryTextListener(this);
        if(this.search != null && !"".equals(this.search)){
            view.setQuery(this.search, true);
            view.setIconified(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.getGenericItemAdapter().filter(query);
        search = query;
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getGenericItemAdapter().filter(newText);
        search = newText;
        return true;
    }
}
