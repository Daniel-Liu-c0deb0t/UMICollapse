package umicollapse.data;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

import umicollapse.util.BitSet;
import umicollapse.util.Read;
import static umicollapse.util.Utils.charSet;
import static umicollapse.util.Utils.charEquals;

public class Combo implements DataStructure{
    private Map<BitSet, Integer> umiFreq;
    private int umiLength;

    @Override
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits){
        this.umiFreq = umiFreq;
        this.umiLength = umiLength;
    }

    @Override
    public Set<BitSet> removeNear(BitSet umi, int k, int maxFreq){
        Set<BitSet> res = new HashSet<>();
        recursiveRemoveNear(umi, 0, k, maxFreq, new BitSet(umiLength * Read.ENCODING_LENGTH), res, k);
        return res;
    }

    private void recursiveRemoveNear(BitSet umi, int idx, int k, int maxFreq, BitSet curr, Set<BitSet> res, int K){
        if(k < 0)
            return;

        if(idx >= umiLength){
            if(umiFreq.containsKey(curr) && (k == K || umiFreq.get(curr) <= maxFreq)){
                res.add(curr.clone());
                umiFreq.remove(curr);
            }

            return;
        }

        for(int c : Read.ENCODING_IDX.keySet()){
            if(charEquals(umi, idx, c))
                recursiveRemoveNear(umi, idx + 1, k, maxFreq, charSet(curr, idx, c), res, K);
            else
                recursiveRemoveNear(umi, idx + 1, k - 1, maxFreq, charSet(curr, idx, c), res, K);
        }
    }

    @Override
    public boolean contains(BitSet umi){
        return umiFreq.containsKey(umi);
    }

    @Override
    public Map<String, Float> stats(){
        Map<String, Float> res = new HashMap<>();
        return res;
    }
}
