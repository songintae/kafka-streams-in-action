package com.example.kafkastreamsinaction.aggregator;

import com.example.kafkastreamsinaction.model.ShareVolume;
import lombok.Getter;

import java.util.*;

@Getter
public class FixedSizePriorityShareVolumeAggregator {

    private List<ShareVolume> inner;
    private int maxSize;

    private FixedSizePriorityShareVolumeAggregator() {
    }

    public FixedSizePriorityShareVolumeAggregator(int maxSize) {
        this.inner = new ArrayList<>();
        this.maxSize = maxSize;
    }

    public FixedSizePriorityShareVolumeAggregator add(ShareVolume element) {
        inner.add(element);
        if (inner.size() > maxSize) {
            final Optional<ShareVolume> minShare = inner.stream()
                    .min(Comparator.comparing(ShareVolume::getShares));
            minShare.ifPresent(v -> inner.remove(minShare.get()));
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
