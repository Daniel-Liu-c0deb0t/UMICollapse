package data;

import static util.Utils.umiDist;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

public class Naive implements DataStructure{
    private Map<BitSet, Integer> umiFreq;

    @Override
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits){
        this.umiFreq = umiFreq;
    }

    @Override
    public List<BitSet> removeNear(BitSet umi, int k, int maxFreq){
        List<BitSet> res = new ArrayList<>();

        for(Iterator<Map.Entry<BitSet, Integer>> it = umiFreq.entrySet().iterator(); it.hasNext();){
            Map.Entry<BitSet, Integer> e = it.next();
            BitSet o = e.getKey();
            BitSet f = e.getValue();

            if(umiDist(umi, o) <= k && f <= maxFreq){
                res.add(o);
                it.remove();
            }
        }

        return res;
    }

    @Override
    public boolean contains(BitSet umi){
        return umiFreq.containsKey(umi);
    }
}
