package umicollapse.data;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import umicollapse.util.BitSet;
import static umicollapse.util.Utils.umiDist;

public class ParallelBKTree implements ParallelDataStructure{
    private int umiLength;
    private Node root;

    @Override
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits){
        this.umiLength = umiLength;

        boolean first = true;

        for(Map.Entry<BitSet, Integer> e : umiFreq.entrySet()){
            BitSet umi = e.getKey();
            int freq = e.getValue();

            if(first){
                root = new Node(umi, freq);
                first = false;
            }else{
                insert(umi, freq);
            }
        }
    }

    @Override
    public Set<BitSet> near(BitSet umi, int k, int maxFreq){
        Set<BitSet> res = new HashSet<>();
        res.add(umi);
        recursiveNear(umi, root, k, maxFreq, res);
        return res;
    }

    private void recursiveNear(BitSet umi, Node curr, int k, int maxFreq, Set<BitSet> res){
        int dist = umiDist(umi, curr.getUMI());

        if(dist <= k && curr.getFreq() <= maxFreq)
            res.add(curr.getUMI());

        if(curr.hasNodes()){
            int lo = Math.max(dist - k, 0);
            int hi = Math.min(dist + k, umiLength);

            for(int i = 0; i < umiLength + 1; i++){
                if(curr.hasNode(i)){
                    if(i >= lo && i <= hi && curr.minFreq(i) <= maxFreq)
                        recursiveNear(umi, curr.get(i), k, maxFreq, res);
                }
            }
        }
    }

    private void insert(BitSet umi, int freq){
        Node curr = root;
        int dist;

        do{
            dist = umiDist(umi, curr.getUMI());
            curr.setMinFreq(Math.min(curr.getMinFreq(), freq));
        }while((curr = curr.initNode(dist, umi, umiLength, freq)) != null);
    }

    private static class Node{
        private BitSet umi;
        private Node[] c;
        private int freq, minFreq;

        Node(BitSet umi, int freq){
            this.c = null;
            this.umi = umi;
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
}
