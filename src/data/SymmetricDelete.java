package data;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.BitSet;

import static util.Utils.charGet;
import static util.Utils.charSet;
import static util.Utils.umiDist;

public class SymmetricDelete implements DataStructure{
    private int umiLength, maxEdits;
    private Map<BitSet, Set<Integer>> m;
    private BitSet[] arr;
    private BitSet removed;

    public SymmetricDelete(Set<BitSet> s, int umiLength, int maxEdits){
        this.umiLength = umiLength;
        this.maxEdits = maxEdits;

        m = new HashMap<BitSet, Set<Integer>>();
        arr = new BitSet[s.size()];
        removed = new BitSet();
        int i = 0;

        for(BitSet umi : s){
            arr[i] = umi;
            insert(umi, i);
            i++;
        }
    }

    // k <= maxEdits must be satisfied
    @Override
    public List<BitSet> removeNear(BitSet umi, int k){
        int diff = maxEdits - k;
        int minMatch = 1;

        for(int i = 0; i < diff; i++)
            minMatch *= umiLength - k - i;

        for(int i = 1; i < diff; i++)
            minMatch /= i + 1;

        BitSet b = new BitSet();
        Map<Integer, Integer> resCount = new HashMap<>();

        for(int i = 0; i <= maxEdits; i++)
            recursiveRemoveNear(umi, i, maxEdits - i, b, 0, resCount);

        List<BitSet> res = new ArrayList<>();

        for(Map.Entry<Integer, Integer> e : resCount.entrySet()){
            if(e.getValue() >= minMatch){
                int idx = e.getKey();

                if(!removed.get(idx) && umiDist(umi, arr[idx]) <= k){
                    res.add(arr[idx]);
                    removed.set(idx);
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
        BitSet b = new BitSet();

        for(int i = 0; i <= maxEdits; i++)
            recursiveInsert(umi, idx, i, maxEdits - i, b, 0);
    }

    private void recursiveInsert(BitSet umi, int umiIdx, int idx, int k, BitSet curr, int currIdx){
        if(currIdx >= umiLength - maxEdits){
            BitSet key = (BitSet)curr.clone();

            if(!m.containsKey(key))
                m.put(key, new HashSet<Integer>());

            m.get(key).add(umiIdx);

            return;
        }

        charSet(curr, currIdx, charGet(umi, idx));

        for(int i = 0; i <= k; i++)
            recursiveInsert(umi, umiIdx, idx + 1 + i, k - i, curr, currIdx + 1);
    }
}
