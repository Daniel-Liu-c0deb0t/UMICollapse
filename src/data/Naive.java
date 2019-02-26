package data;

import static util.Utils.umiDist;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

public class Naive implements DataStructure{
    private Set<BitSet> s;

    public Naive(Set<BitSet> s, int umiLength){
        this.s = new HashSet<BitSet>(s);
    }

    @Override
    public List<BitSet> removeNear(BitSet umi, int k){
        List<BitSet> res = new ArrayList<>();

        for(Iterator<BitSet> it = s.iterator(); it.hasNext();){
            BitSet o = it.next();

            if(umiDist(umi, o) <= k){
                res.add(o);
                it.remove();
            }
        }

        return res;
    }
}
