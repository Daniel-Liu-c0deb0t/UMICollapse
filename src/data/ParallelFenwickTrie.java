package data;

import java.util.Set;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;
import java.util.HashMap;

import util.Read;
import static util.Utils.charGet;
import static util.Utils.charSet;
import static util.Utils.charEquals;

public class ParallelFenwickTrie implements ParallelDataStructure{
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
    public List<BitSet> near(BitSet umi, int k, int maxFreq){
        List<BitSet> res = new ArrayList<>();
        res.add(umi); // always include the queried UMI

        Map.Entry<Integer, Integer> floorEntry = freqs.floorEntry(maxFreq);

        if(floorEntry == null)
            return res;

        int freqIdx = floorEntry.getValue() + 1;

        for(; freqIdx > 0; freqIdx -= freqIdx & (-freqIdx))
            recursiveNear(umi, 0, fenwick[freqIdx], k, new BitSet(), res);

        return res;
    }

    private void recursiveNear(BitSet umi, int idx, Node currNode, int k, BitSet currStr, List<BitSet> res){
        if(k < 0)
            return;

        if(idx >= umiLength){
            res.add((BitSet)currStr.clone());
            return;
        }

        for(Map.Entry<Integer, Integer> e : Read.ENCODING_IDX.entrySet()){
            int c = e.getKey();
            int i = e.getValue();

            if(currNode.exists(i)){
                if(charEquals(umi, idx, c))
                    recursiveNear(umi, idx + 1, currNode.get(i), k, charSet(currStr, idx, c), res);
                else
                    recursiveNear(umi, idx + 1, currNode.get(i), k - 1, charSet(currStr, idx, c), res);
            }
        }
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

    private static class Node{
        private Node[] c;

        Node(){
            this.c = null;
        }

        Node ensureCreated(int idx){
            if(c == null)
                c = new Node[Read.ENCODING_MAP.size()];

            if(c[idx] == null)
                c[idx] = new Node();

            return c[idx];
        }

        boolean exists(int idx){
            return c[idx] != null;
        }

        Node get(int idx){
            return c[idx];
        }
    }
}
