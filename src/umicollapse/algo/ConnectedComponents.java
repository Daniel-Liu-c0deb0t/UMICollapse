package umicollapse.algo;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import umicollapse.util.BitSet;
import umicollapse.data.DataStructure;
import umicollapse.util.ReadFreq;
import umicollapse.util.Read;

public class ConnectedComponents implements Algorithm{
    @Override
    public List<Read> apply(Map<BitSet, ReadFreq> reads, DataStructure data, int umiLength, int k, float percentage){
        Map<BitSet, Integer> m = new HashMap<>();

        for(Map.Entry<BitSet, ReadFreq> e : reads.entrySet())
            m.put(e.getKey(), e.getValue().freq);

        data.init(m, umiLength, k);
        List<Read> res = new ArrayList<>();

        for(BitSet umi : reads.keySet()){
            if(data.contains(umi))
                res.add(visitAndRemove(umi, reads, data, k).read);
        }

        return res;
    }

    private ReadFreq visitAndRemove(BitSet u, Map<BitSet, ReadFreq> reads, DataStructure data, int k){
        ReadFreq max = reads.get(u);
        Set<BitSet> c = data.removeNear(u, k, Integer.MAX_VALUE);

        for(BitSet v : c){
            if(u.equals(v))
                continue;

            ReadFreq r = visitAndRemove(v, reads, data, k);

            if(r.freq > max.freq)
                max = r;
        }

        return max;
    }
}
