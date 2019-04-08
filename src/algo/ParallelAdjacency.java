package algo;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Arrays;

import util.Read;
import util.ReadFreq;
import util.UmiFreq;
import data.ParallelDataStructure;

public class ParallelAdjacency implements ParallelAlgorithm{
    @Override
    public List<Read> apply(Map<BitSet, ReadFreq> reads, ParallelDataStructure data, int umiLength, int k, float percentage, int threadCount){
        Map<BitSet, Integer> m = new HashMap<>();
        UmiFreq[] freq = new UmiFreq[reads.size()];
        List<Read> res = new ArrayList<>();
        int idx = 0;

        for(Map.Entry<BitSet, ReadFreq> e : reads.entrySet()){
            freq[idx] = new UmiFreq(e.getKey(), e.getValue());
            m.put(e.getKey(), e.getValue().freq);
            idx++;
        }

        Arrays.sort(freq, (a, b) -> b.readFreq.freq - a.readFreq.freq);
        data.init(m, umiLength, k);

        List<List<BitSet>> adj = new ArrayList<>();

        for(int i = 0; i < freq.length; i++)
            adj.add(null);

        ForkJoinPool pool = new ForkJoinPool(threadCount); // custom pool for custom thread count

        pool.submit(() -> IntStream.range(0, freq.length).parallel()
                .forEach(i -> adj.set(i, data.near(freq[i].umi, k, Integer.MAX_VALUE)))).get();

        Set<BitSet> visited = new HashSet<>();

        for(int i = 0; i < freq.length; i++){
            if(!visited.contains(freq[i].umi)){
                for(BitSet umi : adj.get(i))
                    visited.add(umi);

                res.add(freq[i].readFreq.read);
            }
        }

        return res;
    }
}
