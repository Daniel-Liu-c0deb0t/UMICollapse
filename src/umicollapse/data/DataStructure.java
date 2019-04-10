package umicollapse.data;

import java.util.Set;
import java.util.Map;

import umicollapse.util.BitSet;

public interface DataStructure extends Data{
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits);
    public Set<BitSet> removeNear(BitSet umi, int k, int maxFreq);
    public boolean contains(BitSet umi);
    public Map<String, Float> stats();
}
