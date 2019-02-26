package data;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;

import fastq.Read;
import static util.Utils.charSet;
import static util.Utils.charEquals;

public class Combo implements DataStructure{
    private Set<BitSet> s;
    private int umiLength;

    public Combo(Set<BitSet> s, int umiLength){
        this.s = new HashSet<BitSet>(s);
        this.umiLength = umiLength;
    }

    @Override
    public List<BitSet> removeNear(BitSet umi, int k){
        List<BitSet> res = new ArrayList<>();
        BitSet curr = new BitSet();
        recursiveRemoveNear(umi, 0, k, curr, res);
        return res;
    }

    private void recursiveRemoveNear(BitSet umi, int idx, int k, BitSet curr, List<BitSet> res){
        if(k < 0){
            return;
        }

        if(idx >= umiLength){
            if(s.contains(curr)){
                res.add((BitSet)curr.clone());
                s.remove(curr);
            }

            return;
        }

        for(int v : Read.ENCODING_MAP.values()){
            if(charEquals(umi, idx, v)){
                recursiveRemoveNear(umi, idx + 1, k, charSet(curr, idx, v), res);
            }else{
                recursiveRemoveNear(umi, idx + 1, k - 1, charSet(curr, idx, v), res);
            }
        }
    }
}
