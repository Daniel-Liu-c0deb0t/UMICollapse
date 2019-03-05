package util;

import java.util.BitSet;

public class UmiFreq{
    public BitSet umi;
    public ReadFreq readFreq;

    public UmiFreq(BitSet umi, ReadFreq readFreq){
        this.umi = umi;
        this.readFreq = readFreq;
    }
}
