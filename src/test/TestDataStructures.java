package test;

import java.util.Map;
import java.util.HashMap;
import java.util.BitSet;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import util.Utils;
import data.*;

public class TestDataStructures{
    public static void main(String[] args){
        DataStructure baseline = new Naive();
        DataStructure[] data = {new Combo(), new Ngram(), new SymmetricDelete(), new Trie(), new BKTree()};

        String[] s1 = {"AAAA", "AAAT", "CCCC", "CCCG", "TTTT"};
        test(s1, 0, baseline, data);

        String[] s2 = {"AAAA", "AAAT", "CCCC", "CCCG", "TTTT"};
        test(s2, 1, baseline, data);
    }

    private static void test(String[] umiList, int k, DataStructure baseline, DataStructure[] data){
        Map<BitSet, Integer> m = new HashMap<>();
        int umiLength = umiList[0].length();

        for(String umi : umiList)
            m.put(Utils.toBitSet(umi), 0);

        baseline.init(new HashMap<BitSet, Integer>(m), umiLength, k);

        for(DataStructure d : data)
            d.init(new HashMap<BitSet, Integer>(m), umiLength, k);

        for(BitSet umi : m.keySet()){
            List<BitSet> baselineList = baseline.removeNear(umi, k, Integer.MAX_VALUE);

            for(DataStructure d : data){
                List<BitSet> list = d.removeNear(umi, k, Integer.MAX_VALUE);

                if(!listMatches(list, baselineList)){
                    System.out.println("\nError");

                    for(String s : umiList)
                        System.out.println(s);

                    System.out.println("Data structure\t" + d.getClass().getName());
                    System.out.println("Max number of edits\t" + k);
                    System.out.println("Query\t" + Utils.toString(umi, umiLength));
                }
            }
        }
    }

    private static boolean listMatches(List<BitSet> a, List<BitSet> b){
        if(a.size() != b.size())
            return false;

        Set<BitSet> s = new HashSet<>(a);
        s.removeAll(b);
        return s.isEmpty();
    }
}
