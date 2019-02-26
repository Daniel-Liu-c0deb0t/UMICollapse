package data;

import static util.Utils.umiDist;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;

public class Naive implements DataStructure{
    private Set<BitSet> s;

    public Naive(Set<BitSet> s){
        this.s = new HashSet<BitSet>(s);
    }

    @Override
    public List<BitSet> removeNear(BitSet umi, int k){
        List<BitSet> res = new ArrayList<>();

        for(BitSet o : s){
            if(umiDist(umi, o) <= k){
                res.add(o);
            }
        }

        s.removeAll(res);

        return res;
    }
}
