package algo;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Arrays;

import data.ParallelDataStructure;
import util.Read;
import util.ReadFreq;
import util.UmiFreq;

public class ParallelDirectional implements ParallelAlgorithm{
    @Override
    public List<Read> apply(Map<BitSet, ReadFreq> reads, ParallelDataStructure data, int umiLength, int k, float percentage, int threadCount){
        UmiFreq[] freq = new UmiFreq[reads.size()];
        List<Read> res = new ArrayList<>();
        Map<BitSet, Integer> m = new HashMap<>();
        int idx = 0;

        for(Map.Entry<BitSet, ReadFreq> e : reads.entrySet()){
            freq[idx] = new UmiFreq(e.getKey(), e.getValue());
            m.put(e.getKey(), e.getValue().freq);
            idx++;
        }

        Arrays.sort(freq, (a, b) -> b.readFreq.freq - a.readFreq.freq);
        data.init(m, umiLength, k);

        List<List<BitSet>> adjIdx = new ArrayList<>();

        for(int i = 0; i < freq.length; i++)
            adjIdx.add(null);

        ForkJoinPool pool = new ForkJoinPool(threadCount); // custom pool for custom thread count

        pool.submit(() -> IntStream.range(0, freq.length).parallel()
                .forEach(i -> adjIdx.set(i, data.near(freq[i].umi, k, (int)(percentage * freq[i].readFreq.freq))))).get();

        Map<BitSet, List<BitSet>> adj = new HashMap<>();

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

    private void visitAndRemove(BitSet u, Map<BitSet, ReadFreq> reads, Map<BitSet, List<BitSet>> adj, Set<BitSet> visited){
        if(visited.contains(u))
            return;

        List<BitSet> c = adj.get(u);
        visited.add(u);

        for(BitSet v : c){
            if(u.equals(v))
                continue;

            visitAndRemove(v, reads, adj, visited);
        }
    }
}
