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
    private Map<BitSet, Set<Integer>> m;
    private BitSet[] arr;
    private BitSet removed;

    @Override
    public void init(Map<BitSet, Integer> umiFreq, int umiLength, int maxEdits){
        this.umiFreq = umiFreq;
        this.umiLength = umiLength;
        this.maxEdits = maxEdits;

        m = new HashMap<BitSet, Set<Integer>>();
        arr = new BitSet[umiFreq.size()];
        removed = new BitSet(umiFreq.size());
        int i = 0;

        for(BitSet umi : umiFreq.keySet()){
            arr[i] = umi;
            insert(umi, i);
            i++;
        }
    }

    // k <= maxEdits must be satisfied
    @Override
    public Set<BitSet> removeNear(BitSet umi, int k, int maxFreq){
        int diff = maxEdits - k;
        int minMatch = 1;

        for(int i = 0; i < diff; i++)
            minMatch *= umiLength - k - i;

        for(int i = 1; i < diff; i++)
            minMatch /= i + 1;

        BitSet b = new BitSet(umiLength * Read.ENCODING_LENGTH);
        Map<Integer, Integer> resCount = new HashMap<>();

        for(int i = 0; i <= maxEdits; i++)
            recursiveRemoveNear(umi, i, maxEdits - i, b, 0, resCount);

        Set<BitSet> res = new HashSet<>();

        for(Map.Entry<Integer, Integer> e : resCount.entrySet()){
            if(e.getValue() >= minMatch){
                int idx = e.getKey();

                if(!removed.get(idx)){
                    int dist = umiDist(umi, arr[idx]);

                    if(dist <= k && (dist == 0 || umiFreq.get(arr[idx]) <= maxFreq)){
                        res.add(arr[idx]);
                        removed.set(idx, true);
                        umiFreq.remove(arr[idx]);
                    }
                }
            }
        }

        return res;
    }

    private void recursiveRemoveNear(BitSet umi, int idx, int currK, BitSet curr, int currIdx, Map<Integer, Integer> resCount){
        if(currIdx >= umiLength - maxEdits){
            if(m.containsKey(curr)){
                for(Integer i : m.get(curr))
                    resCount.put(i, resCount.getOrDefault(i, 0) + 1);
            }

            return;
        }

        charSet(curr, currIdx, charGet(umi, idx));

        for(int i = 0; i <= currK; i++)
            recursiveRemoveNear(umi, idx + 1 + i, currK - i, curr, currIdx + 1, resCount);
    }

    private void insert(BitSet umi, int idx){
        BitSet b = new BitSet(umiLength * Read.ENCODING_LENGTH);

        for(int i = 0; i <= maxEdits; i++)
            recursiveInsert(umi, idx, i, maxEdits - i, b, 0);
    }

    private void recursiveInsert(BitSet umi, int umiIdx, int idx, int k, BitSet curr, int currIdx){
        if(currIdx >= umiLength - maxEdits){
            BitSet key = curr.clone();

            if(!m.containsKey(key))
                m.put(key, new HashSet<Integer>());

            m.get(key).add(umiIdx);

            return;
        }

        charSet(curr, currIdx, charGet(umi, idx));

        for(int i = 0; i <= k; i++)
            recursiveInsert(umi, umiIdx, idx + 1 + i, k - i, curr, currIdx + 1);
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

        for(Map.Entry<BitSet, Set<Integer>> e : m.entrySet()){
            int size = e.getValue().size();
            maxSubseq = Math.max(maxSubseq, size);
            avgSubseq += (float)size / m.size();
        }

        res.put("max subseq bin size", (float)maxSubseq);
        res.put("avg subseq bin size", (float)avgSubseq);

        return res;
    }
}
