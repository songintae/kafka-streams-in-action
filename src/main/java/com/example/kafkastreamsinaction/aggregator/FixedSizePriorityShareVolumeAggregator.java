package com.example.kafkastreamsinaction.aggregator;

import com.example.kafkastreamsinaction.model.ShareVolume;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public class FixedSizePriorityShareVolumeAggregator {

    private final TreeSet<ShareVolume> inner;
    private final int maxSize;

    public FixedSizePriorityShareVolumeAggregator(Comparator<ShareVolume> comparator, int maxSize) {
        this.inner = new TreeSet<>(comparator);
        this.maxSize = maxSize;
    }

    public FixedSizePriorityShareVolumeAggregator add(ShareVolume element) {
        inner.add(element);
        if (inner.size() > maxSize) {
            inner.pollLast();
        }

        return this;
    }

    public FixedSizePriorityShareVolumeAggregator remove(ShareVolume element) {
        inner.remove(element);
        return this;
    }

    public Iterator<ShareVolume> iterator() {
        return inner.iterator();
    }

    @Override
    public String toString() {
        return "FixedSizePriorityQueue{" +
                "inner=" + inner +
                ", maxSize=" + maxSize +
                '}';
    }
}
