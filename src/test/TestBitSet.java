package test;

import umicollapse.util.BitSet;
import umicollapse.util.Utils;

public class TestBitSet{
    public static void main(String[] args){
        test("ATCG", "ATCN");
        test("ATCG", "ATCG");
        test("ATCG", "AGCC");
        test("ANCG", "ANCC");
    }

    private static void test(String a, String b){
        BitSet aa = Utils.toBitSet(a);
        BitSet bb = Utils.toBitSet(b);
        int dist = Utils.umiDist(aa, bb);
        System.out.println("Distance between " + a + " and " + b + " is " + dist);
    }
}
