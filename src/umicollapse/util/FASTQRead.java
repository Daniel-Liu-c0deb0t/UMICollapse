package umicollapse.util;

import java.util.Arrays;

import htsjdk.samtools.fastq.FastqRecord;

import static umicollapse.util.Utils.toBitSet;
import static umicollapse.util.Utils.toPhred33ByteArray;
import static umicollapse.util.Utils.toPhred33String;

public class FASTQRead extends Read{
    private String desc;
    private BitSet seq;
    private byte[] qual;
    private int avgQual;

    public FASTQRead(String desc, String umi, String seq, String qual){
        this.desc = desc;
        this.seq = toBitSet(umi.toUpperCase() + seq.toUpperCase());
        this.qual = toPhred33ByteArray(qual);

        float avg = 0.0f;

        for(byte b : this.qual)
            avg += b;

        this.avgQual = (int)(avg / this.qual.length);
    }

    public FASTQRead(String desc, String umiAndSeq, String qual){
        this.desc = desc;
        this.seq = toBitSet(umiAndSeq.toUpperCase());
        this.qual = toPhred33ByteArray(qual);

        float avg = 0.0f;

        for(byte b : this.qual)
            avg += b;

        this.avgQual = (int)(avg / this.qual.length);
    }

    @Override
    public BitSet getUMI(int maxLength){
        return seq;
    }

    @Override
    public int getUMILength(){
        return -1; // should never be called!
    }

    @Override
    public int getAvgQual(){
        return avgQual;
    }

    @Override
    public boolean equals(Object o){
        FASTQRead r = (FASTQRead)o;

        if(!seq.equals(r.seq))
            return false;

        if(!desc.equals(r.desc))
            return false;

        if(!Arrays.equals(qual, r.qual))
            return false;

        return true;
    }

    public FastqRecord toFASTQRecord(int length, int umiLength){
        return new FastqRecord(desc, Utils.toString(seq, length).substring(umiLength), "", Utils.toPhred33String(qual).substring(umiLength));
    }
}
