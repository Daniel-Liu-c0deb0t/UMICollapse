package test;

import java.util.Set;
import java.util.HashSet;
import java.util.BitSet;

import fastq.Read;
import data.Naive;
import data.Combo;
import data.Trie;
import data.BKTree;

public class Test{
    public static void main(String[] args){
        Read r = new Read("@", "ATCGATCG", "AAAAAAAA", 4);

        Set<BitSet> s = new HashSet<>();
        s.add(r.umi);

        Naive n = new Naive(s, 4);

        Combo c = new Combo(s, 4);

        Trie t = new Trie(s, 4);

        BKTree b = new BKTree(s, 4);
    }
}
