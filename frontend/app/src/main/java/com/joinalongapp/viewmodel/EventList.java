package com.joinalongapp.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

//TODO: could just make the eventlist public
public class EventList extends ViewModel implements List<Event> {
    List<Event> eventList = new ArrayList<>();

    @Override
    public void clear() {

    }

    @Override
    public int size() {
        return eventList.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(@Nullable Object o) {
        return false;
    }

    @NonNull
    @Override
    public Iterator<Event> iterator() {
        return null;
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] a) {
        return null;
    }

    @Override
    public boolean add(Event event) {
        eventList.add(event);
        return false;
    }

    @Override
    public boolean remove(@Nullable Object o) {
        return false;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends Event> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends Event> c) {
        return false;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        return false;
    }

    @Override
    public Event get(int index) {
        return eventList.get(index);
    }

    @Override
    public Event set(int index, Event element) {
        return eventList.set(index, element);
    }

    @Override
    public void add(int index, Event element) {
        eventList.add(index, element);
    }

    @Override
    public Event remove(int index) {
        return null;
    }

    @Override
    public int indexOf(@Nullable Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(@Nullable Object o) {
        return 0;
    }

    @NonNull
    @Override
    public ListIterator<Event> listIterator() {
        return null;
    }

    @NonNull
    @Override
    public ListIterator<Event> listIterator(int index) {
        return null;
    }

    @NonNull
    @Override
    public List<Event> subList(int fromIndex, int toIndex) {
        return null;
    }
}
