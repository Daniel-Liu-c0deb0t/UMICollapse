package data;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import util.BitSet;
import static util.Utils.charGet;
import static util.Utils.HASH_CONST;
import static util.Utils.umiDist;

public class Ngram implements DataStructure{
    private Map<BitSet, Integer> umiFreq;
    private int umiLength, ngramSize, hashPow;
    private Map<Interval, Set<Integer>> m;
    private BitSet[] arr;
    private BitSet removed;

    @Override
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits){
        this.umiFreq = umiFreq;
        this.umiLength = umiLength;
        ngramSize = (int)Math.ceil((umiLength - maxEdits) / (maxEdits + 1.0f) + 1.0f) - 1;

        hashPow = 1;

        for(int i = 0; i < ngramSize - 1; i++)
            hashPow *= HASH_CONST;

        m = new HashMap<Interval, Set<Integer>>();
        arr = new BitSet[umiFreq.size()];
        removed = new BitSet(umiFreq.size());
        int i = 0;

        for(BitSet umi : umiFreq.keySet()){
            arr[i] = umi;
            insert(umi, i);
            i++;
        }
    }

    // k <= maxEdits must be satisfied
    @Override
    public Set<BitSet> removeNear(BitSet umi, int k, int maxFreq){
        int minMatch = umiLength - ngramSize * (k + 1) + 1;
        Map<Integer, Integer> count = new HashMap<>();
        Set<BitSet> res = new HashSet<>();
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

                if(!removed.get(idx)){
                    int dist = umiDist(umi, arr[idx]);

                    if(dist <= k && (dist == 0 || umiFreq.get(arr[idx]) <= maxFreq)){
                        res.add(arr[idx]);
                        removed.set(idx, true);
                        umiFreq.remove(arr[idx]);
                    }
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
        return umiFreq.containsKey(umi);
    }

    @Override
    public Map<String, Float> stats(){
        Map<String, Float> res = new HashMap<>();
        res.put("num n-grams", (float)m.size());

        int maxNgrams = 0;
        float avgNgrams = 0.0f;

        for(Map.Entry<Interval, Set<Integer>> e : m.entrySet()){
            int size = e.getValue().size();
            maxNgrams = Math.max(maxNgrams, size);
            avgNgrams += (float)size / m.size();
        }

        res.put("max n-gram bin size", (float)maxNgrams);
        res.put("avg n-gram bin size", avgNgrams);

        return res;
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
