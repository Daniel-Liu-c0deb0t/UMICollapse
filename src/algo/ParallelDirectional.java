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
    // k is an integer from 0-100 that represent a percentage
    @Override
    public List<Read> apply(Map<BitSet, ReadFreq> reads, ParallelDataStructure data, int umiLength, int k, int threadCount){
        float percentage = k / 100.0f;
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
        data.init(m, umiLength, 1);

        for(int i = 0; i < freq.length; i++){
            if(data.contains(freq[i].umi)){
                visitAndRemove(freq[i].umi, reads, data, percentage);
                res.add(freq[i].readFreq.read);
            }
        }

        return res;
    }

    private void visitAndRemove(BitSet u, Map<BitSet, ReadFreq> reads, DataStructure data, float percentage){
        List<BitSet> c = data.removeNear(u, 1, (int)(percentage * reads.get(u).freq));

        for(BitSet v : c){
            if(u.equals(v))
                continue;

            visitAndRemove(v, reads, data, percentage);
        }
    }
}
