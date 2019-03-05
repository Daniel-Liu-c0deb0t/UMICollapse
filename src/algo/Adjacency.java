package algo;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;

import util.Read;
import util.ReadFreq;
import util.UmiFreq;
import data.DataStructure;

public class Adjacency implements Algorithm{
    @Override
    public List<Read> apply(Map<BitSet, ReadFreq> reads, DataStructure data, int umiLength, int k){
        Set<BitSet> s = reads.keySet();
        data.init(s, umiLength, k);
        UmiFreq[] freq = new UmiFreq[reads.size()];
        List<Read> res = new ArrayList<>();
        int idx = 0;

        for(Map.Entry<BitSet, ReadFreq> e : reads.entrySet()){
            freq[idx] = new UmiFreq(e.getKey(), e.getValue());
            idx++;
        }

        Arrays.sort(freq, (a, b) -> b.readFreq.freq - a.readFreq.freq);

        for(int i = 0; i < freq.length; i++){
            if(data.contains(freq[i].umi)){
                data.removeNear(freq[i].umi, k);
                res.add(freq[i].readFreq.read);
            }
        }

        return res;
    }
}
