package data;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.BitSet;

import static util.Utils.umiDist;

public class BKTree implements DataStructure{
    private Set<Bitset> s;
    private int umiLength;
    private Node root;

    @Override
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits){
        this.s = new HashSet<BitSet>(umiFreq.keySet());
        this.umiLength = umiLength;

        boolean first = true;

        for(Map.Entry<BitSet, Integer> e : umiFreq){
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
    public List<BitSet> removeNear(BitSet umi, int k, int maxFreq){
        List<BitSet> res = new ArrayList<>();
        recursiveRemoveNear(umi, root, k, maxFreq, res);
        return res;
    }

    private void recursiveRemoveNear(BitSet umi, Node curr, int k, int maxFreq, List<BitSet> res){
        int dist = umiDist(umi, curr.getUMI());

        if(dist <= k && curr.exists() && curr.getFreq() <= maxFreq){
            res.add(curr.getUMI());
            curr.setExists(false);
            s.remove(curr.getUMI());
        }

        int lo = Math.max(dist - k, 0);
        int hi = Math.min(dist + k, umiLength);
        boolean subtreeExists = curr.exists();
        int minFreq = Integer.MAX_VALUE;

        for(int i = lo; i <= hi; i++){
            if(curr.subtreeExists(i)){
                if(curr.minFreq(i) <= maxFreq)
                    recursiveRemoveNear(umi, curr.get(i), k, maxFreq, res);

                minFreq = Math.min(minFreq, curr.minFreq(i));
                subtreeExists |= curr.subtreeExists(i);
            }
        }

        curr.setSubtreeExists(subtreeExists);

        if(curr.exists())
            minFreq = Math.min(minFreq, curr.getFreq());

        curr.setMinFreq(minFreq);
    }

    private void insert(BitSet umi, int freq){
        Node curr = root;
        int dist;

        do{
            dist = umiDist(umi, curr.getUMI());
            curr.setMinFreq(Math.min(curr.getMinFreq(), freq));
        }while((curr = curr.initNode(dist, umi, umiLength, freq)) != null);
    }

    @Override
    public boolean contains(BitSet umi){
        return s.contains(umi);
    }

    private static class Node{
        private BitSet umi;
        private boolean exists, subtreeExists;
        private Node[] c;
        private int freq, minFreq;

        Node(BitSet umi, int freq){
            this.c = null;
            this.umi = umi;
            this.exists = true;
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

        boolean exists(){
            return exists;
        }

        void setExists(boolean exists){
            this.exists = exists;
        }

        void setSubtreeExists(boolean subtreeExists){
            this.subtreeExists = subtreeExists;
        }

        boolean subtreeExists(int k){
            return c != null && c[k] != null && c[k].subtreeExists;
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
    }
}
