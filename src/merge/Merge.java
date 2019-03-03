package merge;

import fastq.Read;

public interface Merge{
    public Read merge(Read a, Read b);
}
