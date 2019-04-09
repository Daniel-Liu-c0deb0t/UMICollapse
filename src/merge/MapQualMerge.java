package merge;

import util.Read;
import util.SAMRead;

public class MapQualMerge implements Merge{
    @Override
    public Read merge(Read a, Read b){
        SAMRead samA = (SAMRead)a;
        SAMRead samB = (SAMRead)b;

        if(samA.getMapQual() >= samB.getMapQual())
            return a;
        else
            return b;
    }
}
