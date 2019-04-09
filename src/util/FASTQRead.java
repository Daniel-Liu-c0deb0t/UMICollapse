package util;

import htsjdk.samtools.fastq.FastqRecord;

import java.util.BitSet;

import static util.Utils.toBitSet;
import static util.Utils.toPhred33ByteArray;
import static util.Utils.toPhred33String;

public class FASTQRead implements Read{
    private String desc;
    private BitSet seq;
    private byte[] qual;
    private int avgQual;

    public Read(String desc, String umi, String seq, String qual){
        this.desc = desc;
        this.seq = toBitSet(umi.toUpperCase() + seq.toUpperCase());
        this.qual = toPhred33ByteArray(qual);

        float avg = 0.0f;

        for(byte b : qual)
            avg += (float)b / qual.length;

        this.avgQual = (int)avg;
    }

    public Read(String desc, String umiAndSeq, String qual){
        this.desc = desc;
        this.seq = toBitSet(umiAndSeq.toUpperCase());
        this.qual = toPhred33ByteArray(qual);

        float avg = 0.0f;

        for(byte b : qual)
            avg += (float)b / qual.length;

        this.avgQual = (int)avg;
    }

    @Override
    public BitSet getUMI(){
        return seq;
    }

    @Override
    public int getAvgQual(){
        return avgQual;
    }

    public FastqRecord toFASTQRecord(int umiLength){
        return new FastqRecord(desc, Utils.toString(seq).substring(umiLength), "", Utils.toPhred33String(qual));
    }
}
