package algo;

import java.util.List;
import java.util.Map;
import java.util.BitSet;

import util.ReadFreq;
import util.Read;
import data.DataStructure;

public interface Algorithm{
    public List<Read> apply(Map<BitSet, ReadFreq> reads, DataStructure data, int umiLength, int k);
}
