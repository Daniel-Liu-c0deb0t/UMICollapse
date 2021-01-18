package umicollapse.algo;

import java.util.List;
import java.util.Map;

import umicollapse.util.BitSet;
import umicollapse.util.ReadFreq;
import umicollapse.util.Read;
import umicollapse.util.ClusterTracker;
import umicollapse.data.DataStructure;

public interface Algorithm extends Algo{
    public List<Read> apply(Map<BitSet, ReadFreq> reads, DataStructure data, ClusterTracker tracker, int umiLength, int k, float percentage);
}
