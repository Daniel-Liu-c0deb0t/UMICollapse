package umicollapse.algo;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;

import umicollapse.util.BitSet;
import umicollapse.data.DataStructure;
import umicollapse.util.Read;
import umicollapse.util.ReadFreq;
import umicollapse.util.UmiFreq;
import umicollapse.util.ClusterTracker;

public class Directional implements Algorithm{
    @Override
    public List<Read> apply(Map<BitSet, ReadFreq> reads, DataStructure data, ClusterTracker tracker, int umiLength, int k, float percentage){
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

        for(int i = 0; i < freq.length; i++){
            if(data.contains(freq[i].umi)){
                visitAndRemove(freq[i].umi, reads, data, tracker, k, percentage);
                tracker.track(freq[i].umi, freq[i].readFreq.read);
                res.add(freq[i].readFreq.read);
            }
        }

        return res;
    }

    private void visitAndRemove(BitSet u, Map<BitSet, ReadFreq> reads, DataStructure data, ClusterTracker tracker, int k, float percentage){
        Set<BitSet> c = data.removeNear(u, k, (int)(percentage * (reads.get(u).freq + 1)));
        tracker.addAll(c, reads);

        for(BitSet v : c){
            if(u.equals(v))
                continue;

            visitAndRemove(v, reads, data, tracker, k, percentage);
        }
    }
}
