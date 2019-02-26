package fastq;

import java.util.Map;
import java.util.HashMap;

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
    public BitSet read;
    public byte[] qual;

    public Read(String desc, String read, String qual, int umiLength){

    }
}
