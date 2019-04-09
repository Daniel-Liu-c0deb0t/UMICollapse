package data;

import java.util.Set;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.BitSet;
import java.util.Map;
import java.util.HashMap;

import util.Read;
import static util.Utils.charGet;
import static util.Utils.charSet;
import static util.Utils.charEquals;

public class FenwickTrie implements DataStructure{
    private Set<BitSet> s;
    private TreeMap<Integer, Integer> freqs;
    private int umiLength;
    private Node[] fenwick;

    @Override
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits){
        this.s = umiFreq.keySet();
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
    public Set<BitSet> removeNear(BitSet umi, int k, int maxFreq){
        Set<BitSet> res = new HashSet<>();

        if(maxFreq != Integer.MAX_VALUE){ // always remove the queried UMI
            int freqIdx = freqs.size();

            for(; freqIdx > 0; freqIdx -= freqIdx & (-freqIdx))
                recursiveRemoveNear(umi, 0, fenwick[freqIdx], 0, new BitSet(), res);
        }

        Map.Entry<Integer, Integer> floorEntry = freqs.floorEntry(maxFreq);

        if(floorEntry == null)
            return res;

        int freqIdx = floorEntry.getValue() + 1;

        for(; freqIdx > 0; freqIdx -= freqIdx & (-freqIdx))
            recursiveRemoveNear(umi, 0, fenwick[freqIdx], k, new BitSet(), res);

        return res;
    }

    private void recursiveRemoveNear(BitSet umi, int idx, Node currNode, int k, BitSet currStr, Set<BitSet> res){
        if(k < 0)
            return;

        if(idx >= umiLength){
            res.add((BitSet)currStr.clone());
            currNode.setExists(false);
            s.remove(currStr);
            return;
        }

        boolean exists = false;

        for(Map.Entry<Integer, Integer> e : Read.ENCODING_IDX.entrySet()){
            int c = e.getKey();
            int i = e.getValue();

            if(currNode.exists(i)){
                if(charEquals(umi, idx, c))
                    recursiveRemoveNear(umi, idx + 1, currNode.get(i), k, charSet(currStr, idx, c), res);
                else
                    recursiveRemoveNear(umi, idx + 1, currNode.get(i), k - 1, charSet(currStr, idx, c), res);

                exists |= currNode.exists(i);
            }
        }

        currNode.setExists(exists);
    }

    private void insert(BitSet umi, int freq){
        int freqIdx = freqs.get(freq) + 1;

        for(; freqIdx <= freqs.size(); freqIdx += freqIdx & (-freqIdx)){
            if(fenwick[freqIdx] == null)
                fenwick[freqIdx] = new Node();

            Node curr = fenwick[freqIdx];

            for(int i = 0; i < umiLength; i++){
                int nextIdx = Read.ENCODING_IDX.get(charGet(umi, i));
                curr = curr.ensureCreated(nextIdx);
            }
        }
    }

    @Override
    public boolean contains(BitSet umi){
        return s.contains(umi);
    }

    @Override
    public Map<String, Float> stats(){
        Map<String, Float> res = new HashMap<>();

        int numNodes = 0;

        for(Node curr : fenwick){
            if(curr != null)
                numNodes += numNodes(curr);
        }

        res.put("num nodes", (float)numNodes);
        return res;
    }

    private int numNodes(Node curr){
        int count = 1;

        for(int i = 0; i < Read.ENCODING_MAP.size(); i++){
            if(curr.hasNode(i))
                count += numNodes(curr.get(i));
        }

        return count;
    }

    private static class Node{
        private Node[] c;
        private boolean exists;

        Node(){
            this.c = null;
            this.exists = true;
        }

        Node ensureCreated(int idx){
            if(c == null)
                c = new Node[Read.ENCODING_MAP.size()];

            if(c[idx] == null)
                c[idx] = new Node();

            return c[idx];
        }

        boolean exists(int idx){
            return c[idx] != null && c[idx].exists;
        }

        void setExists(boolean exists){
            this.exists = exists;
        }

        Node get(int idx){
            return c[idx];
        }

        boolean hasNode(int idx){
            return c != null && c[idx] != null;
        }
    }
}
