package fastq;

import java.util.Map;
import java.util.HashMap;

import static util.Utils.toBitSet;
import static util.Utils.toPhred33ByteArray;

public class Read{
    public static final int ENCODING_DIST = 2;
    public static final int ENCODING_LENGTH = 3;
    public static final Map<Character, BitSet> ENCODING_MAP = new HashMap<>();

    static{
        ENCODING_MAP.put('A', 0b000);
        ENCODING_MAP.put('T', 0b100);
        ENCODING_MAP.put('C', 0b010);
        ENCODING_MAP.put('G', 0b001);
    }

    public String desc;
    public BitSet umi;
    public BitSet seq;
    public byte[] qual;

    public Read(String desc, String seq, String qual, int umiLength){
        this.desc = desc.substring(1);
        seq = seq.toUpperCase();
        this.umi = toBitSet(seq.substring(0, umiLength));
        this.seq = toBitSet(seq.substring(umiLength + 1));
        this.qual = toPhred33ByteArray(qual);
    }
}
