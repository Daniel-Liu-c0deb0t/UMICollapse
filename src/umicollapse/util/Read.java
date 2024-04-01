package umicollapse.util;

import java.util.Map;
import java.util.HashMap;

public abstract class Read{
    public static final int ENCODING_DIST = 2;
    public static final int ENCODING_LENGTH = 3;
    public static final Map<Character, Integer> ENCODING_MAP = new HashMap<>();
    public static final Map<Integer, Integer> ENCODING_IDX = new HashMap<>();
    public static final char[] ALPHABET = {'A', 'T', 'C', 'G', 'N'};
    public static final int UNDETERMINED = 0b100;
    public static final char UNDETERMINED_CHAR = 'N';
    public static final int ANY = 0b111;

    static{
        ENCODING_MAP.put('A', 0b000);
        ENCODING_MAP.put('T', 0b101);
        ENCODING_MAP.put('C', 0b110);
        ENCODING_MAP.put('G', 0b011);
        ENCODING_MAP.put(UNDETERMINED_CHAR, UNDETERMINED);

        ENCODING_IDX.put(0b000, 0);
        ENCODING_IDX.put(0b101, 1);
        ENCODING_IDX.put(0b110, 2);
        ENCODING_IDX.put(0b011, 3);
        ENCODING_IDX.put(UNDETERMINED, 4);
    }

    public abstract int getAvgQual();
    public abstract BitSet getUMI(int maxLength);
    public abstract int getUMILength();
}
