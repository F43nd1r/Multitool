package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;

/**
 * Created on 31.07.2016.
 *
 * @author F43nd1r
 */

public class DoubleBackedList implements List<ScriptGroup> {
    private final List<ScriptGroup> list;
    private final FlexibleAdapter<ScriptItem> adapter;

    public DoubleBackedList(final List<ScriptGroup> list, final FlexibleAdapter<ScriptItem> adapter) {
        this.list = list;
        //noinspection unchecked
        this.adapter = adapter;
        adapter.initializeListeners(new FlexibleAdapter.OnItemMoveListener() {
            private ScriptItem t;

            @Override
            public void onActionStateChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            }

            @Override
            public boolean shouldMoveItem(int from, int to) {
                t = adapter.getItem(from);
                return !adapter.getExpandableOf(from).equals(adapter.getExpandableOf(from > to ? to - 1 : to));
            }

            @Override
            public void onItemMove(int fromPosition, int toPosition) {
                Log.d("move", fromPosition + " " + toPosition);
                if (t instanceof Script) {
                    Script move = (Script) t;
                    ScriptGroup fromGroup = move.getHeader();
                    int index = list.indexOf(fromGroup) + (fromPosition < toPosition ? 1 : -1);
                    if (index < 0 || index >= list.size()) return;
                    ScriptGroup toGroup = list.get(index);
                    fromGroup.remove(move);
                    toGroup.add(move);
                }
            }
        });
    }

    private void ensureHeaders(ScriptGroup... groups) {
        for (ScriptGroup g : groups) {
            for (Script s : g) {
                s.setHeader(g);
            }
            g.setExpanded(false);
        }
    }

    private void ensureHeaders(Iterable<? extends ScriptGroup> groups) {
        for (ScriptGroup g : groups) {
            for (Script s : g) {
                s.setHeader(g);
            }
            g.setExpanded(false);
        }
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @NonNull
    @Override
    public java.util.Iterator<ScriptGroup> iterator() {
        return new Iterator(list.listIterator());
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(@NonNull T1[] t1s) {
        return list.toArray(t1s);
    }

    @Override
    public boolean add(ScriptGroup t) {
        ensureHeaders(t);
        adapter.addItem(adapter.getItemCount(), t);
        return list.add(t);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof IFlexible) {
            adapter.removeItem(adapter.getGlobalPositionOf((IFlexible) o));
        }
        return list.remove(o);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        return list.containsAll(collection);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends ScriptGroup> collection) {
        ensureHeaders(collection);
        adapter.addItems(adapter.getItemCount(), new ArrayList<ScriptItem>(collection));
        return list.addAll(collection);
    }

    @Override
    public boolean addAll(int i, @NonNull Collection<? extends ScriptGroup> collection) {
        ensureHeaders(collection);
        adapter.addItems(i, new ArrayList<ScriptItem>(collection));
        return list.addAll(i, collection);
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        List<Integer> positions = new ArrayList<>();
        for (Object x : collection) {
            if (x instanceof IFlexible) {
                positions.add(adapter.getGlobalPositionOf((IFlexible) x));
            }
        }
        adapter.removeItems(positions);
        return list.removeAll(collection);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        adapter.removeRange(0, adapter.getItemCount());
        list.clear();
    }

    @Override
    public ScriptGroup get(int i) {
        return list.get(i);
    }

    @Override
    public ScriptGroup set(int i, ScriptGroup t) {
        ensureHeaders(t);
        adapter.removeItem(i);
        adapter.addItem(i, t);
        return list.set(i, t);
    }

    @Override
    public void add(int i, ScriptGroup t) {
        ensureHeaders(t);
        adapter.addItem(i, t);
        list.add(i, t);
    }

    @Override
    public ScriptGroup remove(int i) {
        adapter.removeItem(i);
        return list.remove(i);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<ScriptGroup> listIterator() {
        return new Iterator(list.listIterator());
    }

    @NonNull
    @Override
    public ListIterator<ScriptGroup> listIterator(int i) {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public List<ScriptGroup> subList(int i, int i1) {
        return list.subList(i, i1);
    }

    public static class Iterator implements ListIterator<ScriptGroup> {
        private final ListIterator<ScriptGroup> listIterator;

        public Iterator(ListIterator<ScriptGroup> listIterator) {
            this.listIterator = listIterator;
        }

        @Override
        public void add(ScriptGroup t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(ScriptGroup t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int previousIndex() {
            return listIterator.previousIndex();
        }

        @Override
        public int nextIndex() {
            return listIterator.nextIndex();
        }

        @Override
        public ScriptGroup previous() {
            return listIterator.previous();
        }

        @Override
        public boolean hasPrevious() {
            return listIterator.hasPrevious();
        }

        @Override
        public ScriptGroup next() {
            return listIterator.next();
        }

        @Override
        public boolean hasNext() {
            return listIterator.hasNext();
        }
    }
}
