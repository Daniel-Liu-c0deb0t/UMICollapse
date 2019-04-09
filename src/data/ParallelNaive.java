package data;

import static util.Utils.umiDist;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.BitSet;
import java.util.Iterator;

public class ParallelNaive implements ParallelDataStructure{
    private Map<BitSet, Integer> umiFreq;

    @Override
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits){
        this.umiFreq = umiFreq;
    }

    @Override
    public Set<BitSet> near(BitSet umi, int k, int maxFreq){
        Set<BitSet> res = new HashSet<>();

        for(Iterator<Map.Entry<BitSet, Integer>> it = umiFreq.entrySet().iterator(); it.hasNext();){
            Map.Entry<BitSet, Integer> e = it.next();
            BitSet o = e.getKey();
            int f = e.getValue();
            int dist = umiDist(umi, o);

            if(dist <= k && (dist == 0 || f <= maxFreq))
                res.add(o);
        }

        return res;
    }
}
