package umicollapse.merge;

import umicollapse.util.Read;

public class AnyMerge implements Merge{
    @Override
    public Read merge(Read a, Read b){
        return a;
    }
}
