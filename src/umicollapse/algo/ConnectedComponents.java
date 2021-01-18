package umicollapse.algo;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import umicollapse.util.BitSet;
import umicollapse.data.DataStructure;
import umicollapse.util.ReadFreq;
import umicollapse.util.UmiFreq;
import umicollapse.util.Read;
import umicollapse.util.ClusterTracker;

public class ConnectedComponents implements Algorithm{
    @Override
    public List<Read> apply(Map<BitSet, ReadFreq> reads, DataStructure data, ClusterTracker tracker, int umiLength, int k, float percentage){
        Map<BitSet, Integer> m = new HashMap<>();

        for(Map.Entry<BitSet, ReadFreq> e : reads.entrySet())
            m.put(e.getKey(), e.getValue().freq);

        data.init(m, umiLength, k);
        List<Read> res = new ArrayList<>();

        for(BitSet umi : reads.keySet()){
            if(data.contains(umi)){
                UmiFreq umiFreq = visitAndRemove(umi, reads, data, tracker, k);
                tracker.track(umiFreq.umi, umiFreq.readFreq.read);
                res.add(umiFreq.readFreq.read);
            }
        }

        return res;
    }

    private UmiFreq visitAndRemove(BitSet u, Map<BitSet, ReadFreq> reads, DataStructure data, ClusterTracker tracker, int k){
        UmiFreq max = new UmiFreq(u, reads.get(u));
        Set<BitSet> c = data.removeNear(u, k, Integer.MAX_VALUE);
        tracker.addAll(c, reads);

        for(BitSet v : c){
            if(u.equals(v))
                continue;

            UmiFreq r = visitAndRemove(v, reads, data, tracker, k);

            if(r.readFreq.freq > max.readFreq.freq)
                max = r;
        }

        return max;
    }
}
