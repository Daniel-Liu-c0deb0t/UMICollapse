package data;

import java.util.List;
import java.util.Map;
import java.util.BitSet;

public interface DataStructure{
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits);
    public List<BitSet> removeNear(BitSet umi, int k, int maxFreq);
    public boolean contains(BitSet umi);
    public Map<String, Float> stats();
}
