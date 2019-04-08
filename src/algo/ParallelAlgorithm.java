package algo;

import java.util.List;
import java.util.Map;
import java.util.BitSet;

import util.ReadFreq;
import util.Read;
import data.ParallelDataStructure;

public interface ParallelAlgorithm{
    public List<Read> apply(Map<BitSet, ReadFreq> reads, ParallelDataStructure data, int umiLength, int k, float percentage);
}
