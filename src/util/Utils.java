package util;

import java.util.BitSet;

public class Utils{
    public static final int HASH_CONST = 31;

    // fast Hamming distance by using pairwise equidistant encodings for each nucleotide
    public static int umiDist(BitSet a, BitSet b){
        BitSet c = (BitSet)a.clone();
        c.xor(b);
        // divide by the pairwise Hamming distance in the encoding
        return c.cardinality() / Read.ENCODING_DIST;
    }

    public static boolean charEquals(BitSet a, int idx, int b){
        for(int i = 0; i < Read.ENCODING_LENGTH; i++){
            if(a.get(idx * Read.ENCODING_LENGTH + i) != ((b & (1 << i)) != 0))
                return false;
        }

        return true;
    }

    public static BitSet charSet(BitSet a, int idx, int b){
        for(int i = 0; i < Read.ENCODING_LENGTH; i++)
            a.set(idx * Read.ENCODING_LENGTH + i, ((b & (1 << i)) != 0));

        return a;
    }

    public static int charGet(BitSet a, int idx){
        int res = 0;

        for(int i = 0; i < Read.ENCODING_LENGTH; i++){
            if(a.get(idx * Read.ENCODING_LENGTH + i))
                res |= 1 << i;
        }

        return res;
    }

    public static BitSet toBitSet(String s){
        BitSet res = new BitSet();

        for(int i = 0; i < s.length(); i++)
            charSet(res, i, Read.ENCODING_MAP.get(s.charAt(i)));

        return res;
    }

    public static String toString(BitSet a, int length){
        char[] res = new char[length];

        for(int i = 0; i < length; i++)
            res[i] = Read.ALPHABET[Read.ENCODING_IDX.get(charGet(a, i))];

        return new String(res);
    }

    // converts quality string to byte array, using the Phred+33 format
    public static byte[] toPhred33ByteArray(String q){
        byte[] res = new byte[q.length()];

        for(int i = 0; i < q.length(); i++)
            res[i] = (byte)(q.charAt(i) - '!');

        return res;
    }
}
