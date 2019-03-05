package data;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;

import static util.Utils.charGet;
import static util.Utils.HASH_CONST;
import static util.Utils.umiDist;

public class Ngram implements DataStructure{
    private Set<BitSet> s;
    private int umiLength, ngramSize, hashPow;
    private Map<Interval, Set<Integer>> m;
    private BitSet[] arr;
    private BitSet removed;

    @Override
    public void init(Set<BitSet> s, int umiLength, int maxEdits){
        this.s = new HashSet<BitSet>(s);
        this.umiLength = umiLength;
        ngramSize = (int)Math.ceil((umiLength - maxEdits) / (maxEdits + 1.0f) + 1.0f) - 1;

        hashPow = 1;

        for(int i = 0; i < ngramSize - 1; i++)
            hashPow *= HASH_CONST;

        m = new HashMap<Interval, Set<Integer>>();
        arr = new BitSet[s.size()];
        removed = new BitSet();
        int i = 0;

        for(BitSet umi : s){
            arr[i] = umi;
            insert(umi, i);
            i++;
        }
    }

    // k <= maxEdits must be satisfied
    @Override
    public List<BitSet> removeNear(BitSet umi, int k){
        int minMatch = umiLength - ngramSize * (k + 1) + 1;
        Map<Integer, Integer> count = new HashMap<>();
        List<BitSet> res = new ArrayList<>();
        int hash = 0;

        for(int i = 0; i <= umiLength - ngramSize; i++){
            Interval in = new Interval(umi, i, i + ngramSize - 1);

            if(i == 0){
                for(int j = 0; j < ngramSize; j++)
                    hash = hash * HASH_CONST + charGet(umi, j);
            }else{
                hash = (hash - charGet(umi, i) * hashPow) * HASH_CONST + charGet(umi, i + ngramSize - 1);
            }

            in.setHash(hash);

            if(m.containsKey(in)){
                for(Integer j : m.get(in))
                    count.put(j, count.getOrDefault(j, 0) + 1);
            }
        }

        for(Map.Entry<Integer, Integer> e : count.entrySet()){
            if(e.getValue() >= minMatch){
                int idx = e.getKey();

                if(!removed.get(idx) && umiDist(umi, arr[idx]) <= k){
                    res.add(arr[idx]);
                    removed.set(idx);
                    s.remove(arr[idx]);
                }
            }
        }

        return res;
    }

    private void insert(BitSet umi, int idx){
        int hash = 0;

        for(int i = 0; i <= umiLength - ngramSize; i++){
            Interval in = new Interval(umi, i, i + ngramSize - 1);

            if(i == 0){
                for(int j = 0; j < ngramSize; j++)
                    hash = hash * HASH_CONST + charGet(umi, j);
            }else{
                hash = (hash - charGet(umi, i) * hashPow) * HASH_CONST + charGet(umi, i + ngramSize - 1);
            }

            in.setHash(hash);

            if(!m.containsKey(in))
                m.put(in, new HashSet<Integer>());

            m.get(in).add(idx);
        }
    }

    @Override
    public boolean contains(BitSet umi){
        return s.contains(umi);
    }

    private static class Interval{
        private BitSet s;
        private int lo, hi, hash;

        Interval(BitSet s, int lo, int hi){
            this.s = s;
            this.lo = lo;
            this.hi = hi;
        }

        int get(int i){
            return charGet(s, lo + i);
        }

        void setHash(int hash){
            this.hash = hash;
        }

        @Override
        public int hashCode(){
            return hash;
        }

        @Override
        public boolean equals(Object o){
            if(!(o instanceof Interval))
                return false;

            Interval other = (Interval)o;

            for(int i = 0; i < hi - lo + 1; i++){
                if(get(i) != other.get(i))
                    return false;
            }

            return true;
        }
    }
}
