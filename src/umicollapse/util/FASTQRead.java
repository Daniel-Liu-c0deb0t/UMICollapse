package umicollapse.util;

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
            avg += (float)b / this.qual.length;

        this.avgQual = (int)avg;
    }

    public FASTQRead(String desc, String umiAndSeq, String qual){
        this.desc = desc;
        this.seq = toBitSet(umiAndSeq.toUpperCase());
        this.qual = toPhred33ByteArray(qual);

        float avg = 0.0f;

        for(byte b : this.qual)
            avg += (float)b / this.qual.length;

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

    public FastqRecord toFASTQRecord(int length, int umiLength){
        return new FastqRecord(desc, Utils.toString(seq, length).substring(umiLength), "", Utils.toPhred33String(qual).substring(umiLength));
    }
}
