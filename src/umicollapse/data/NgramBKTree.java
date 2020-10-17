package umicollapse.data;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import umicollapse.util.BitSet;
import static umicollapse.util.Utils.charGet;
import static umicollapse.util.Utils.HASH_CONST;
import static umicollapse.util.Utils.umiDist;

public class NgramBKTree implements DataStructure{
    private Map<BitSet, Integer> umiFreq;
    private int umiLength, ngramSize, maxEdits;
    private Map<Interval, Node> m;

    @Override
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits){
        this.umiFreq = umiFreq;
        this.umiLength = umiLength;
        this.maxEdits = maxEdits;
        ngramSize = umiLength / (maxEdits + 1);

        m = new HashMap<Interval, Node>();

        for(Map.Entry<BitSet, Integer> e : umiFreq.entrySet())
            insert(e.getKey(), e.getValue());
    }

    // k <= maxEdits must be satisfied
    @Override
    public Set<BitSet> removeNear(BitSet umi, int k, int maxFreq){
        Set<BitSet> res = new HashSet<>();

        for(int i = 0; i < maxEdits + 1; i++){
            Interval in = new Interval(umi, i * ngramSize, i == maxEdits ? (umiLength - 1) : ((i + 1) * ngramSize - 1));

            if(m.containsKey(in)){
                Node curr = m.get(in);

                if(maxFreq != Integer.MAX_VALUE) // always remove the queried UMI
                    recursiveRemoveNearBKTree(umi, curr, 0, Integer.MAX_VALUE, res);

                recursiveRemoveNearBKTree(umi, curr, k, maxFreq, res);
            }
        }

        return res;
    }

    private void insert(BitSet umi, int freq){
        for(int i = 0; i < maxEdits + 1; i++){
            Interval in = new Interval(umi, i * ngramSize, i == maxEdits ? (umiLength - 1) : ((i + 1) * ngramSize - 1));

            if(m.containsKey(in)){
                int length = umiLength - ((i == maxEdits ? (umiLength - 1) : ((i + 1) * ngramSize - 1)) - i * ngramSize + 1);
                insertBKTree(m.get(in), umi, length, freq);
            }else{
                m.put(in, new Node(umi, freq));
            }
        }
    }

    private void recursiveRemoveNearBKTree(BitSet umi, Node curr, int k, int maxFreq, Set<BitSet> res){
        int dist = umiDist(umi, curr.getUMI());
        boolean exists = umiFreq.containsKey(curr.getUMI());

        if(dist <= k && exists && curr.getFreq() <= maxFreq){
            res.add(curr.getUMI());
            umiFreq.remove(curr.getUMI());
        }

        boolean subtreeExists = exists;
        int minFreq = exists ? curr.getFreq() : Integer.MAX_VALUE;

        if(curr.hasNodes()){
            int lo = Math.max(dist - k, 0);
            int length = curr.getNodeCount();
            int hi = Math.min(dist + k, length - 1);

            for(int i = 0; i < length; i++){
                if(curr.subtreeExists(i)){
                    if(i >= lo && i <= hi && curr.minFreq(i) <= maxFreq)
                        recursiveRemoveNearBKTree(umi, curr.get(i), k, maxFreq, res);

                    minFreq = Math.min(minFreq, curr.minFreq(i));
                    subtreeExists |= curr.subtreeExists(i);
                }
            }
        }

        curr.setSubtreeExists(subtreeExists);
        curr.setMinFreq(minFreq);
    }

    private void insertBKTree(Node curr, BitSet umi, int length, int freq){
        int dist;

        do{
            dist = umiDist(umi, curr.getUMI());
            curr.setMinFreq(Math.min(curr.getMinFreq(), freq));
        }while((curr = curr.initNode(dist, umi, length, freq)) != null);
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
        return res;
    }

    private static class Node{
        private BitSet umi;
        private boolean subtreeExists;
        private Node[] c;
        private int freq, minFreq;

        Node(BitSet umi, int freq){
            this.c = null;
            this.umi = umi;
            this.subtreeExists = true;
            this.freq = freq;
            this.minFreq = freq;
        }

        Node initNode(int k, BitSet umi, int umiLength, int freq){
            if(c == null)
                c = new Node[umiLength + 1];

            if(c[k] == null){
                c[k] = new Node(umi, freq);
                return null;
            }

            return c[k];
        }

        BitSet getUMI(){
            return umi;
        }

        int getNodeCount(){
            return c.length;
        }

        void setSubtreeExists(boolean subtreeExists){
            this.subtreeExists = subtreeExists;
        }

        boolean subtreeExists(int k){
            return c[k] != null && c[k].subtreeExists;
        }

        void setMinFreq(int minFreq){
            this.minFreq = minFreq;
        }

        int getMinFreq(){
            return minFreq;
        }

        int getFreq(){
            return freq;
        }

        int minFreq(int k){
            return c[k] == null ? Integer.MAX_VALUE : c[k].minFreq;
        }

        Node get(int k){
            return c[k];
        }

        boolean hasNode(int k){
            return c != null && c[k] != null;
        }

        boolean hasNodes(){
            return c != null;
        }
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
