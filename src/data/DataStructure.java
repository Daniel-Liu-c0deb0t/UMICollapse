package data;

import java.util.List;

import fastq.Read;

public interface DataStructure{
    public List<Read> removeNear(Read r, int k);
}
