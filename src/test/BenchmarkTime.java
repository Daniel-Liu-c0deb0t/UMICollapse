package test;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import umicollapse.util.BitSet;
import umicollapse.util.Utils;
import umicollapse.util.Read;
import umicollapse.util.ReadFreq;
import umicollapse.util.ClusterTracker;
import umicollapse.algo.*;
import umicollapse.data.*;

public class BenchmarkTime{
    public static void main(String[] args) throws Exception{
        int numRand = Integer.parseInt(args[0]);
        int numDup = 20;
        int numIter = 3;
        int umiLength = Integer.parseInt(args[1]);
        int k = Integer.parseInt(args[2]);
        float percentage = 0.5f;
        Algorithm algo = new Directional();

        Map<String, Class<? extends DataStructure>> d1 = new HashMap<>();
        d1.put("naive", Naive.class);
        d1.put("combo", Combo.class);
        d1.put("ngram", Ngram.class);
        d1.put("delete", SymmetricDelete.class);
        d1.put("trie", Trie.class);
        d1.put("bktree", BKTree.class);
        d1.put("sortbktree", SortBKTree.class);
        d1.put("ngrambktree", NgramBKTree.class);
        d1.put("sortngrambktree", SortNgramBKTree.class);
        d1.put("fenwickbktree", FenwickBKTree.class);

        DataStructure data = d1.get(args[3]).getDeclaredConstructor().newInstance();
        Random rand = new Random(1234); // fixed seed

        System.out.println("Algorithm\t" + algo.getClass().getName());
        System.out.println("Data structure\t" + data.getClass().getName());
        System.out.println("Number of random iterations\t" + numRand);
        System.out.println("Number of duplicates\t" + numDup);
        System.out.println("Number of testing iterations\t" + numIter);
        System.out.println("UMI length\t" + umiLength);
        System.out.println("Max number of edits\t" + k);

        Map<BitSet, ReadFreq> umiFreq = TestUtils.generateData(numRand, numDup, umiLength, k, percentage, rand);

        System.out.println("Actual number of UMIs\t" + umiFreq.size());

        long avgTime = 0L;

        for(int i = 0; i < numIter + 1; i++){
            System.gc();

            long time = runTest(algo, data, umiFreq, umiLength, k, percentage, i == 0);

            if(i > 0) // first time is warm-up
                avgTime += time;
        }

        avgTime /= numIter;

        System.out.println("Average time (ms)\t" + avgTime);
    }

    private static long runTest(Algorithm algo, DataStructure data, Map<BitSet, ReadFreq> umiFreq, int umiLength, int k, float percentage, boolean first){
        long start = System.currentTimeMillis();

        algo.apply(umiFreq, data, new ClusterTracker(false), umiLength, k, percentage);

        if(first){
            Map<String, Float> stats = data.stats();

            for(Map.Entry<String, Float> e : stats.entrySet())
                System.out.println(e.getKey() + "\t" + e.getValue());
        }

        return System.currentTimeMillis() - start;
    }
}
