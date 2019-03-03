package merge;

import fastq.Read;

public class BestMerge implements Merge{
    @Override
    public Read merge(Read a, Read b){
        int qa = 0;

        for(int i = 0; i < a.qual.length; i++)
            qa += a.qual[i];

        int qb = 0;

        for(int i = 0; i < b.qual.length; i++)
            qb += b.qual[i];

        return qa < qb ? b : a;
    }
}
