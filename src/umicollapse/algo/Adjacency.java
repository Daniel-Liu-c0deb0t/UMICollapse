package umicollapse.algo;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import umicollapse.util.BitSet;
import umicollapse.util.Read;
import umicollapse.util.ReadFreq;
import umicollapse.util.UmiFreq;
import umicollapse.data.DataStructure;

public class Adjacency implements Algorithm{
    @Override
    public List<Read> apply(Map<BitSet, ReadFreq> reads, DataStructure data, int umiLength, int k, float percentage){
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

        for(int i = 0; i < freq.length; i++){
            if(data.contains(freq[i].umi)){
                data.removeNear(freq[i].umi, k, Integer.MAX_VALUE);
                res.add(freq[i].readFreq.read);
            }
        }

        return res;
    }
}
