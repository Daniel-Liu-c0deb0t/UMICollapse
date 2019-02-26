package util;

import java.util.BitSet;

import fastq.Read;

public class Utils{
    // fast Hamming distance by using pairwise equidistant encodings for each nucleotide
    public static int umiDist(BitSet a, BitSet b){
        BitSet c = a.xor(b);
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
        for(int i = 0; i < Read.ENCODING_LENGTH; i++){
            a.set(idx * Read.ENCODING_LENGTH + i, ((b & (1 << i)) != 0));
        }

        return a;
    }
}
