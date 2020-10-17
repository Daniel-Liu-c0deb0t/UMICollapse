package umicollapse.data;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import umicollapse.util.BitSet;
import static umicollapse.util.Utils.charGet;
import static umicollapse.util.Utils.HASH_CONST;
import static umicollapse.util.Utils.umiDist;

public class Ngram implements DataStructure{
    private Map<BitSet, Integer> umiFreq;
    private int umiLength, ngramSize, maxEdits;
    private Map<Interval, Set<BitSet>> m;

    @Override
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits){
        this.umiFreq = umiFreq;
        this.umiLength = umiLength;
        this.maxEdits = maxEdits;
        ngramSize = umiLength / (maxEdits + 1);

        m = new HashMap<Interval, Set<BitSet>>();

        for(BitSet umi : umiFreq.keySet())
            insert(umi);
    }

    // k <= maxEdits must be satisfied
    @Override
    public Set<BitSet> removeNear(BitSet umi, int k, int maxFreq){
        Set<BitSet> res = new HashSet<>();

        for(int i = 0; i < maxEdits + 1; i++){
            Interval in = new Interval(umi, i * ngramSize, i == maxEdits ? (umiLength - 1) : ((i + 1) * ngramSize - 1));

            if(m.containsKey(in)){
                for(BitSet s : m.get(in)){
                    if(umiFreq.containsKey(s)){
                        int dist = umiDist(umi, s);

                        if(dist <= k && (dist == 0 || umiFreq.get(s) <= maxFreq)){
                            res.add(s);
                            umiFreq.remove(s);
                        }
                    }
                }
            }
        }

        return res;
    }

    private void insert(BitSet umi){
        for(int i = 0; i < maxEdits + 1; i++){
            Interval in = new Interval(umi, i * ngramSize, i == maxEdits ? (umiLength - 1) : ((i + 1) * ngramSize - 1));

            if(!m.containsKey(in))
                m.put(in, new HashSet<BitSet>());

            m.get(in).add(umi);
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
        res.put("n-grams size", (float)ngramSize);

        int maxNgrams = 0;
        float avgNgrams = 0.0f;

        for(Map.Entry<Interval, Set<BitSet>> e : m.entrySet()){
            int size = e.getValue().size();
            maxNgrams = Math.max(maxNgrams, size);
            avgNgrams += size;
        }

        res.put("max n-gram bin size", (float)maxNgrams);
        res.put("avg n-gram bin size", avgNgrams / m.size());

        return res;
    }

    private static class Interval implements Comparable{
        private BitSet s;
        private int lo, hi, hash;

        Interval(BitSet s, int lo, int hi){
            this.s = s;
            this.lo = lo;
            this.hi = hi;

            for(int i = 0; i < hi - lo + 1; i++)
                hash = hash * HASH_CONST + get(i);

            hash = hash * HASH_CONST + lo;
            hash = hash * HASH_CONST + hi;
        }

        int get(int i){
            return charGet(s, lo + i);
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

            if(lo != other.lo || hi != other.hi)
                return false;

            for(int i = 0; i < hi - lo + 1; i++){
                if(get(i) != other.get(i))
                    return false;
            }

            return true;
        }

        @Override
        public int compareTo(Object o){
            Interval other = (Interval)o;

            if(lo != other.lo)
                return lo - other.lo;

            if(hi != other.hi)
                return hi - other.hi;

            for(int i = 0; i < hi - lo + 1; i++){
                int a = get(i);
                int b = other.get(i);

                if(a != b)
                    return a - b;
            }

            return 0;
        }
    }
}
