package umicollapse.algo;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import umicollapse.util.BitSet;
import umicollapse.data.ParallelDataStructure;
import umicollapse.util.Read;
import umicollapse.util.ReadFreq;
import umicollapse.util.UmiFreq;
import umicollapse.util.ClusterTracker;

public class ParallelDirectional implements ParallelAlgorithm{
    @Override
    public List<Read> apply(Map<BitSet, ReadFreq> reads, ParallelDataStructure data, ClusterTracker tracker, int umiLength, int k, float percentage){
        if(tracker.shouldTrack()){
            throw new UnsupportedOperationException();
        }

        UmiFreq[] freq = new UmiFreq[reads.size()];
        List<Read> res = new ArrayList<>();
        Map<BitSet, Integer> m = new HashMap<>();
        int idx = 0;

        for(Map.Entry<BitSet, ReadFreq> e : reads.entrySet()){
            freq[idx] = new UmiFreq(e.getKey(), e.getValue());
            m.put(e.getKey(), e.getValue().freq);
            idx++;
        }

        Arrays.parallelSort(freq, (a, b) -> b.readFreq.freq - a.readFreq.freq);
        data.init(m, umiLength, k);

        List<Set<BitSet>> adjIdx = new ArrayList<>();

        for(int i = 0; i < freq.length; i++)
            adjIdx.add(null);

        IntStream.range(0, freq.length).parallel()
            .forEach(i -> adjIdx.set(i, data.near(freq[i].umi, k, (int)(percentage * (freq[i].readFreq.freq + 1)))));

        Map<BitSet, Set<BitSet>> adj = new HashMap<>();

        for(int i = 0; i < freq.length; i++)
            adj.put(freq[i].umi, adjIdx.get(i));

        Set<BitSet> visited = new HashSet<>();

        for(int i = 0; i < freq.length; i++){
            if(!visited.contains(freq[i].umi)){
                visitAndRemove(freq[i].umi, reads, adj, visited);
                res.add(freq[i].readFreq.read);
            }
        }

        return res;
    }

    private void visitAndRemove(BitSet u, Map<BitSet, ReadFreq> reads, Map<BitSet, Set<BitSet>> adj, Set<BitSet> visited){
        if(visited.contains(u))
            return;

        Set<BitSet> c = adj.get(u);
        visited.add(u);

        for(BitSet v : c){
            if(u.equals(v))
                continue;

            visitAndRemove(v, reads, adj, visited);
        }
    }
}
