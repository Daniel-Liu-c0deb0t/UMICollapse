package util;

import java.util.Map;
import java.util.HashMap;
import java.util.BitSet;

public abstract class Read{
    public static final int ENCODING_DIST = 2;
    public static final int ENCODING_LENGTH = 3;
    public static final Map<Character, Integer> ENCODING_MAP = new HashMap<>();
    public static final Map<Integer, Integer> ENCODING_IDX = new HashMap<>();
    public static final char[] ALPHABET = {'A', 'T', 'C', 'G'};

    static{
        ENCODING_MAP.put('A', 0b000);
        ENCODING_MAP.put('T', 0b101);
        ENCODING_MAP.put('C', 0b110);
        ENCODING_MAP.put('G', 0b011);

        ENCODING_IDX.put(0b000, 0);
        ENCODING_IDX.put(0b101, 1);
        ENCODING_IDX.put(0b110, 2);
        ENCODING_IDX.put(0b011, 3);
    }

    public abstract int getAvgQual();
    public abstract BitSet getUMI();
}
