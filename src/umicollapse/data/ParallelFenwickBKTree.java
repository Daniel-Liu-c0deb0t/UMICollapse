package umicollapse.data;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import umicollapse.util.BitSet;
import static umicollapse.util.Utils.umiDist;

public class ParallelFenwickBKTree implements ParallelDataStructure{
    private TreeMap<Integer, Integer> freqs;
    private int umiLength;
    private Node[] fenwick;

    @Override
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits){
        this.umiLength = umiLength;

        freqs = new TreeMap<Integer, Integer>();

        for(Map.Entry<BitSet, Integer> e : umiFreq.entrySet())
            freqs.put(e.getValue(), null);

        int idx = 0;

        for(Integer key : freqs.keySet())
            freqs.put(key, idx++);

        fenwick = new Node[freqs.size() + 1]; // build Fenwick tree on frequencies

        for(Map.Entry<BitSet, Integer> e : umiFreq.entrySet()){
            BitSet umi = e.getKey();
            int freq = e.getValue();
            insert(umi, freq);
        }
    }

    @Override
    public Set<BitSet> near(BitSet umi, int k, int maxFreq){
        Set<BitSet> res = new HashSet<>();
        res.add(umi); // always include queried UMI

        Map.Entry<Integer, Integer> floorEntry = freqs.floorEntry(maxFreq);

        if(floorEntry == null)
            return res;

        int freqIdx = floorEntry.getValue() + 1;

        for(; freqIdx > 0; freqIdx -= freqIdx & (-freqIdx))
            recursiveNear(umi, fenwick[freqIdx], k, res);

        return res;
    }

    private void recursiveNear(BitSet umi, Node curr, int k, Set<BitSet> res){
        int dist = umiDist(umi, curr.getUMI());

        if(dist <= k)
            res.add(curr.getUMI());

        if(curr.hasNodes()){
            int lo = Math.max(dist - k, 0);
            int hi = Math.min(dist + k, umiLength);

            for(int i = lo; i <= hi; i++){
                if(curr.hasNode(i))
                    recursiveNear(umi, curr.get(i), k, res);
            }
        }
    }

    private void insert(BitSet umi, int freq){
        int freqIdx = freqs.get(freq) + 1;

        for(; freqIdx <= freqs.size(); freqIdx += freqIdx & (-freqIdx)){
            if(fenwick[freqIdx] == null){
                fenwick[freqIdx] = new Node(umi);
            }else{
                Node curr = fenwick[freqIdx];
                int dist;

                do{
                    dist = umiDist(umi, curr.getUMI());
                }while((curr = curr.initNode(dist, umi, umiLength)) != null);
            }
        }
    }

    private static class Node{
        private BitSet umi;
        private Node[] c;

        Node(BitSet umi){
            this.c = null;
            this.umi = umi;
        }

        Node initNode(int k, BitSet umi, int umiLength){
            if(c == null)
                c = new Node[umiLength + 1];

            if(c[k] == null){
                c[k] = new Node(umi);
                return null;
            }

            return c[k];
        }

        BitSet getUMI(){
            return umi;
        }

        Node get(int k){
            return c[k];
        }

        boolean hasNode(int k){
            return c[k] != null;
        }

        boolean hasNodes(){
            return c != null;
        }
    }
}
