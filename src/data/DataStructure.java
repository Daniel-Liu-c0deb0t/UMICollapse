package data;

import java.util.List;
import java.util.Set;
import java.util.BitSet;

public interface DataStructure{
    public void init(Set<BitSet> s, int umiLength, int maxEdits);
    public List<BitSet> removeNear(BitSet umi, int k);
    public boolean contains(BitSet umi);
}
