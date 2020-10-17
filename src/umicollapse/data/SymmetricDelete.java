package umicollapse.data;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import umicollapse.util.BitSet;
import umicollapse.util.Read;
import static umicollapse.util.Utils.charGet;
import static umicollapse.util.Utils.charSet;
import static umicollapse.util.Utils.umiDist;

public class SymmetricDelete implements DataStructure{
    private Map<BitSet, Integer> umiFreq;
    private int umiLength, maxEdits;
    private Map<BitSet, Set<BitSet>> m;

    @Override
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits){
        this.umiFreq = umiFreq;
        this.umiLength = umiLength;
        this.maxEdits = maxEdits;

        m = new HashMap<BitSet, Set<BitSet>>();

        for(BitSet umi : umiFreq.keySet())
            insert(umi);
    }

    // k <= maxEdits must be satisfied
    @Override
    public Set<BitSet> removeNear(BitSet umi, int k, int maxFreq){
        BitSet b = new BitSet(umiLength * Read.ENCODING_LENGTH);
        Set<BitSet> res = new HashSet<>();

        for(int i = 0; i <= maxEdits; i++){
            recursiveRemoveNear(umi, i, maxEdits - i, k, maxFreq, b, res);

            if(i < umiLength)
                charSet(b, i, Read.ANY);
        }

        return res;
    }

    private void recursiveRemoveNear(BitSet umi, int idx, int k, int maxK, int maxFreq, BitSet curr, Set<BitSet> res){
        if(idx > umiLength)
            return;

        if(idx == umiLength){
            if(k > 0)
                return;

            if(m.containsKey(curr)){
                for(BitSet val : m.get(curr)){
                    if(umiFreq.containsKey(val)){
                        if(maxK == maxEdits){
                            if(umiFreq.get(val) <= maxFreq || umi.equals(val)){
                                res.add(val);
                                umiFreq.remove(val);
                            }
                        }else{
                            int dist = umiDist(umi, val);

                            if(dist <= maxK && (dist == 0 || umiFreq.get(val) <= maxFreq)){
                                res.add(val);
                                umiFreq.remove(val);
                            }
                        }
                    }
                }
            }

            return;
        }

        charSet(curr, idx, charGet(umi, idx));

        for(int i = 0; i <= k; i++){
            recursiveRemoveNear(umi, idx + 1 + i, k - i, maxK, maxFreq, curr, res);

            if(idx + 1 + i < umiLength)
                charSet(curr, idx + 1 + i, Read.ANY);
        }
    }

    private void insert(BitSet umi){
        BitSet b = new BitSet(umiLength * Read.ENCODING_LENGTH);

        for(int i = 0; i <= maxEdits; i++){
            recursiveInsert(umi, i, maxEdits - i, b);

            if(i < umiLength)
                charSet(b, i, Read.ANY);
        }
    }

    private void recursiveInsert(BitSet umi, int idx, int k, BitSet curr){
        if(idx > umiLength)
            return;

        if(idx == umiLength){
            if(k > 0)
                return;

            BitSet key = curr.clone();

            if(!m.containsKey(key))
                m.put(key, new HashSet<BitSet>());

            m.get(key).add(umi);

            return;
        }

        charSet(curr, idx, charGet(umi, idx));

        for(int i = 0; i <= k; i++){
            recursiveInsert(umi, idx + 1 + i, k - i, curr);

            if(idx + 1 + i < umiLength)
                charSet(curr, idx + 1 + i, Read.ANY);
        }
    }

    @Override
    public boolean contains(BitSet umi){
        return umiFreq.containsKey(umi);
    }

    @Override
    public Map<String, Float> stats(){
        Map<String, Float> res = new HashMap<>();
        res.put("num subseq", (float)m.size());

        int maxSubseq = 0;
        float avgSubseq = 0.0f;

        for(Map.Entry<BitSet, Set<BitSet>> e : m.entrySet()){
            int size = e.getValue().size();
            maxSubseq = Math.max(maxSubseq, size);
            avgSubseq += size;
        }

        res.put("max subseq bin size", (float)maxSubseq);
        res.put("avg subseq bin size", avgSubseq / m.size());

        return res;
    }
}
