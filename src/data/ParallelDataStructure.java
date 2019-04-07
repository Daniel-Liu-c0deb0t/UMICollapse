package data;

import java.util.List;
import java.util.Map;
import java.util.BitSet;

public interface ParallelDataStructure{
    // init can update global state
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits);
    // near cannot update global state as it will be run in parallel
    public List<BitSet> near(BitSet umi, int k, int maxFreq);
}
