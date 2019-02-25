package util;

import fastq.Read;

import java.util.BitSet;

public class Utils{
    // fast Hamming distance by using pairwise equidistant encodings for each nucleotide
    public static int umiDist(Read a, Read b){
        BitSet c = a.umi.xor(b.umi);
        // divide by the pairwise Hamming distance in the encoding
        return c.cardinality() / Read.ENCODING_DIST;
    }
}
