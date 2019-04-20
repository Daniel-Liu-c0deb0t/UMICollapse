package umicollapse.data;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import umicollapse.util.BitSet;
import static umicollapse.util.Utils.umiDist;

public class SortBKTree implements DataStructure{
    private Set<BitSet> s;
    private int umiLength;
    private Node root;

    @Override
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits){
        this.s = umiFreq.keySet();
        this.umiLength = umiLength;

        Freq[] freqs = new Freq[umiFreq.size()];
        int idx = 0;

        for(Map.Entry<BitSet, Integer> e : umiFreq.entrySet())
            freqs[idx++] = new Freq(e.getKey(), e.getValue());

        Arrays.sort(freqs, (a, b) -> a.freq - b.freq);

        boolean first = true;

        for(int i = 0; i < freqs.length; i++){
            BitSet umi = freqs[i].umi;
            int freq = freqs[i].freq;

            if(first){
                root = new Node(umi, freq);
                first = false;
            }else{
                insert(umi, freq);
            }
        }
    }

    @Override
    public Set<BitSet> removeNear(BitSet umi, int k, int maxFreq){
        Set<BitSet> res = new HashSet<>();

        if(maxFreq != Integer.MAX_VALUE) // always remove the queried UMI
            recursiveRemoveNear(umi, root, 0, Integer.MAX_VALUE, res);

        recursiveRemoveNear(umi, root, k, maxFreq, res);
        return res;
    }

    private void recursiveRemoveNear(BitSet umi, Node curr, int k, int maxFreq, Set<BitSet> res){
        int dist = umiDist(umi, curr.getUMI());

        if(dist <= k && curr.exists() && curr.getFreq() <= maxFreq){
            res.add(curr.getUMI());
            curr.setExists(false);
            s.remove(curr.getUMI());
        }

        boolean subtreeExists = curr.exists();
        int minFreq = curr.exists() ? curr.getFreq() : Integer.MAX_VALUE;

        if(curr.hasNodes()){
            int lo = Math.max(dist - k, 0);
            int hi = Math.min(dist + k, umiLength);

            for(int i = 0; i < umiLength + 1; i++){
                if(curr.subtreeExists(i)){
                    if(i >= lo && i <= hi && curr.minFreq(i) <= maxFreq)
                        recursiveRemoveNear(umi, curr.get(i), k, maxFreq, res);

                    minFreq = Math.min(minFreq, curr.minFreq(i));
                    subtreeExists |= curr.subtreeExists(i);
                }
            }
        }

        curr.setSubtreeExists(subtreeExists);
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

    @Override
    public Map<String, Float> stats(){
        Map<String, Float> res = new HashMap<>();
        double[] d = depth(root);
        res.put("max depth", (float)d[1]);
        res.put("avg depth", (float)(d[2] / d[0]));
        return res;
    }

    private double[] depth(Node curr){
        double[] a = {0.0f, 0.0f, 0.0f}; // num leaf nodes, max depth, depth sum

        boolean isLeaf = true;

        for(int i = 0; i < umiLength + 1; i++){
            if(curr.hasNode(i)){
                double[] b = depth(curr.get(i));
                a[0] += b[0];
                a[1] = Math.max(a[1], b[1] + 1);
                a[2] += b[2] + b[0];
                isLeaf = false;
            }
        }

        if(isLeaf){
            a[0] += 1;
            a[1] += 1;
            a[2] += 1;
        }

        return a;
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

    private static class Freq{
        BitSet umi;
        int freq;

        Freq(BitSet umi, int freq){
            this.umi = umi;
            this.freq = freq;
        }
    }
}
