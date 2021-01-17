package umicollapse.algo;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.IntStream;

import umicollapse.util.BitSet;
import umicollapse.data.ParallelDataStructure;
import umicollapse.util.ReadFreq;
import umicollapse.util.Read;
import umicollapse.util.ClusterTracker;

public class ParallelConnectedComponents implements ParallelAlgorithm{
    @Override
    public List<Read> apply(Map<BitSet, ReadFreq> reads, ParallelDataStructure data, ClusterTracker tracker, int umiLength, int k, float percentage){
        if(tracker.shouldTrack()){
            throw new UnsupportedOperationException();
        }

        Map<BitSet, Integer> m = new HashMap<>();
        BitSet[] idxToUMI = new BitSet[reads.size()];

        int idx = 0;

        for(Map.Entry<BitSet, ReadFreq> e : reads.entrySet()){
            m.put(e.getKey(), e.getValue().freq);
            idxToUMI[idx++] = e.getKey();
        }

        data.init(m, umiLength, k);

        List<Set<BitSet>> adjIdx = new ArrayList<>();

        for(int i = 0; i < reads.size(); i++)
            adjIdx.add(null);

        IntStream.range(0, reads.size()).parallel()
            .forEach(i -> adjIdx.set(i, data.near(idxToUMI[i], k, Integer.MAX_VALUE)));

        Map<BitSet, Set<BitSet>> adj = new HashMap<>();

        for(int i = 0; i < adjIdx.size(); i++)
            adj.put(idxToUMI[i], adjIdx.get(i));

        List<Read> res = new ArrayList<>();
        Set<BitSet> visited = new HashSet<>();

        for(BitSet umi : reads.keySet()){
            if(!visited.contains(umi))
                res.add(visitAndRemove(umi, reads, adj, visited).read);
        }

        return res;
    }

    private ReadFreq visitAndRemove(BitSet u, Map<BitSet, ReadFreq> reads, Map<BitSet, Set<BitSet>> adj, Set<BitSet> visited){
        if(visited.contains(u))
            return null;

        ReadFreq max = reads.get(u);
        Set<BitSet> c = adj.get(u);
        visited.add(u);

        for(BitSet v : c){
            if(u.equals(v))
                continue;

            ReadFreq r = visitAndRemove(v, reads, adj, visited);

            if(r != null && r.freq > max.freq)
                max = r;
        }

        return max;
    }
}
