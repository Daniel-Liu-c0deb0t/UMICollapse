package umicollapse.data;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import umicollapse.util.BitSet;
import umicollapse.util.Read;
import static umicollapse.util.Utils.charGet;
import static umicollapse.util.Utils.charSet;
import static umicollapse.util.Utils.charEquals;

public class Trie implements DataStructure{
    private Set<BitSet> s;
    private int umiLength;
    private Node root;

    @Override
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits){
        this.s = umiFreq.keySet();
        this.umiLength = umiLength;

        root = new Node(Integer.MAX_VALUE);

        for(Map.Entry<BitSet, Integer> e : umiFreq.entrySet()){
            BitSet umi = e.getKey();
            int freq = e.getValue();
            insert(umi, freq);
        }
    }

    @Override
    public Set<BitSet> removeNear(BitSet umi, int k, int maxFreq){
        Set<BitSet> res = new HashSet<>();

        if(maxFreq != Integer.MAX_VALUE) // always remove the queried UMI
            recursiveRemoveNear(umi, 0, root, 0, Integer.MAX_VALUE, new BitSet(umiLength * Read.ENCODING_LENGTH), res);

        recursiveRemoveNear(umi, 0, root, k, maxFreq, new BitSet(umiLength * Read.ENCODING_LENGTH), res);
        return res;
    }

    private void recursiveRemoveNear(BitSet umi, int idx, Node currNode, int k, int maxFreq, BitSet currStr, Set<BitSet> res){
        if(k < 0)
            return;

        if(idx >= umiLength){
            res.add(currStr.clone());
            currNode.setExists(false);
            s.remove(currStr);
            return;
        }

        boolean exists = false;
        int freq = Integer.MAX_VALUE;

        for(Map.Entry<Integer, Integer> e : Read.ENCODING_IDX.entrySet()){
            int c = e.getKey();
            int i = e.getValue();

            if(currNode.exists(i)){
                if(currNode.getFreq(i) <= maxFreq){
                    if(charEquals(umi, idx, c))
                        recursiveRemoveNear(umi, idx + 1, currNode.get(i), k, maxFreq, charSet(currStr, idx, c), res);
                    else
                        recursiveRemoveNear(umi, idx + 1, currNode.get(i), k - 1, maxFreq, charSet(currStr, idx, c), res);
                }

                exists |= currNode.exists(i);
                freq = Math.min(freq, currNode.getFreq(i));
            }
        }

        currNode.setExists(exists);
        currNode.setFreq(freq);
    }

    private void insert(BitSet umi, int freq){
        Node curr = root;

        for(int i = 0; i < umiLength; i++){
            curr.setFreq(Math.min(curr.getFreq(), freq));
            int nextIdx = Read.ENCODING_IDX.get(charGet(umi, i));
            curr = curr.ensureCreated(nextIdx, freq);
        }
    }

    @Override
    public boolean contains(BitSet umi){
        return s.contains(umi);
    }

    @Override
    public Map<String, Float> stats(){
        Map<String, Float> res = new HashMap<>();
        res.put("num nodes", (float)numNodes(root));
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
        private int freq;

        Node(int freq){
            this.c = null;
            this.exists = true;
            this.freq = freq;
        }

        Node ensureCreated(int idx, int freq){
            if(c == null)
                c = new Node[Read.ENCODING_MAP.size()];

            if(c[idx] == null)
                c[idx] = new Node(freq);

            return c[idx];
        }

        boolean exists(int idx){
            return c[idx] != null && c[idx].exists;
        }

        void setExists(boolean exists){
            this.exists = exists;
        }

        int getFreq(int idx){
            return c[idx].freq;
        }

        int getFreq(){
            return freq;
        }

        void setFreq(int freq){
            this.freq = freq;
        }

        Node get(int idx){
            return c[idx];
        }

        boolean hasNode(int idx){
            return c != null && c[idx] != null;
        }
    }
}
