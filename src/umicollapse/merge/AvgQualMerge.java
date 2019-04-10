package umicollapse.merge;

import umicollapse.util.Read;

public class AvgQualMerge implements Merge{
    @Override
    public Read merge(Read a, Read b){
        if(a.getAvgQual() >= b.getAvgQual())
            return a;
        else
            return b;
    }
}
