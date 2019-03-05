package algo;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.BitSet;

import data.DataStructure;
import util.ReadFreq;
import util.Read;

public class ConnectedComponents implements Algorithm{
    @Override
    public List<Read> apply(Map<BitSet, ReadFreq> reads, DataStructure data, int umiLength, int k){
        Set<BitSet> s = reads.keySet();
        data.init(s, umiLength, k);
        List<Read> res = new ArrayList<>();

        for(BitSet umi : s){
            if(data.contains(umi))
                res.add(visitAndRemove(umi, reads, data, k).read);
        }

        return res;
    }

    private ReadFreq visitAndRemove(BitSet u, Map<BitSet, ReadFreq> reads, DataStructure data, int k){
        ReadFreq max = reads.get(u);
        List<BitSet> c = data.removeNear(u, k);

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
