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
import umicollapse.util.Read;
import umicollapse.util.ReadFreq;
import umicollapse.util.UmiFreq;
import umicollapse.util.ClusterTracker;
import umicollapse.data.ParallelDataStructure;

public class ParallelAdjacency implements ParallelAlgorithm{
    @Override
    public List<Read> apply(Map<BitSet, ReadFreq> reads, ParallelDataStructure data, ClusterTracker tracker, int umiLength, int k, float percentage){
        if(tracker.shouldTrack()){
            throw new UnsupportedOperationException();
        }

        Map<BitSet, Integer> m = new HashMap<>();
        UmiFreq[] freq = new UmiFreq[reads.size()];
        List<Read> res = new ArrayList<>();
        int idx = 0;

        for(Map.Entry<BitSet, ReadFreq> e : reads.entrySet()){
            freq[idx] = new UmiFreq(e.getKey(), e.getValue());
            m.put(e.getKey(), e.getValue().freq);
            idx++;
        }

        Arrays.parallelSort(freq, (a, b) -> b.readFreq.freq - a.readFreq.freq);
        data.init(m, umiLength, k);

        List<Set<BitSet>> adj = new ArrayList<>();

        for(int i = 0; i < freq.length; i++)
            adj.add(null);

        IntStream.range(0, freq.length).parallel()
            .forEach(i -> adj.set(i, data.near(freq[i].umi, k, Integer.MAX_VALUE)));

        Set<BitSet> visited = new HashSet<>();

        for(int i = 0; i < freq.length; i++){
            if(!visited.contains(freq[i].umi)){
                visited.addAll(adj.get(i));
                res.add(freq[i].readFreq.read);
            }
        }

        return res;
    }
}
