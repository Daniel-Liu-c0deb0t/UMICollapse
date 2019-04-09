package merge;

import util.Read;
import util.BAMRead;

public class MapQualMerge implements Merge{
    @Override
    public Read merge(Read a, Read b){
        BAMRead bamA = (BAMRead)a;
        BAMRead bamB = (BAMRead)b;

        if(bamA.getMapQual() >= bamB.getMapQual())
            return a;
        else
            return b;
    }
}
