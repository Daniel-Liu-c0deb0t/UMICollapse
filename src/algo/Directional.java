package algo;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Arrays;

import data.DataStructure;
import util.Read;
import util.ReadFreq;
import util.UmiFreq;

public class Directional implements Algorithm{
    @Override
    public List<Read> apply(Map<BitSet, ReadFreq> reads, DataStructure data, int umiLength, int k, float percentage){
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
                visitAndRemove(freq[i].umi, reads, data, k, percentage);
                res.add(freq[i].readFreq.read);
            }
        }

        return res;
    }

    private void visitAndRemove(BitSet u, Map<BitSet, ReadFreq> reads, DataStructure data, int k, float percentage){
        Set<BitSet> c = data.removeNear(u, k, (int)(percentage * reads.get(u).freq));

        for(BitSet v : c){
            if(u.equals(v))
                continue;

            visitAndRemove(v, reads, data, k, percentage);
        }
    }
}
