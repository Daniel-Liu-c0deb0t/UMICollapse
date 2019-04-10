package umicollapse.data;

import java.util.Set;
import java.util.Map;

import umicollapse.util.BitSet;

public interface ParallelDataStructure extends Data{
    // init can update global state
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits);
    // near cannot update global state as it will be run in parallel
    public Set<BitSet> near(BitSet umi, int k, int maxFreq);
}
