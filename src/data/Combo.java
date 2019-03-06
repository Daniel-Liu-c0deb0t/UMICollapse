package data;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;

import util.Read;
import static util.Utils.charSet;
import static util.Utils.charEquals;

public class Combo implements DataStructure{
    private Map<BitSet, Integer> umiFreq;
    private int umiLength;

    @Override
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits){
        this.umiFreq = umiFreq;
        this.umiLength = umiLength;
    }

    @Override
    public List<BitSet> removeNear(BitSet umi, int k, int maxFreq){
        List<BitSet> res = new ArrayList<>();
        recursiveRemoveNear(umi, 0, k, maxFreq, new BitSet(), res);
        return res;
    }

    private void recursiveRemoveNear(BitSet umi, int idx, int k, int maxFreq, BitSet curr, List<BitSet> res){
        if(k < 0)
            return;

        if(idx >= umiLength){
            if(umiFreq.containsKey(curr) && umiFreq.get(curr) <= maxFreq){
                res.add((BitSet)curr.clone());
                umiFreq.remove(curr);
            }

            return;
        }

        for(int c : Read.ENCODING_IDX.keySet()){
            if(charEquals(umi, idx, c))
                recursiveRemoveNear(umi, idx + 1, k, maxFreq, charSet(curr, idx, c), res);
            else
                recursiveRemoveNear(umi, idx + 1, k - 1, maxFreq, charSet(curr, idx, c), res);
        }
    }

    @Override
    public boolean contains(BitSet umi){
        return umiFreq.contains(umi);
    }
}
