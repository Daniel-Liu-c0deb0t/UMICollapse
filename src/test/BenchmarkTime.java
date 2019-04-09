package test;

import java.util.Map;
import java.util.HashMap;
import java.util.BitSet;
import java.util.Random;

import util.Utils;
import util.Read;
import util.ReadFreq;
import algo.*;
import data.*;

public class BenchmarkTime{
    public static void main(String[] args){
        int numRand = 1000;
        int numDup = 20;
        int numIter = 5;
        int umiLength = 100;
        int k = 1;
        float percentage = 0.5f;
        Algorithm algo = new ConnectedComponents();
        DataStructure data = new FenwickBKTree();
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

        algo.apply(umiFreq, data, umiLength, k, percentage);

        if(first){
            Map<String, Float> stats = data.stats();

            for(Map.Entry<String, Float> e : stats.entrySet())
                System.out.println(e.getKey() + "\t" + e.getValue());
        }

        return System.currentTimeMillis() - start;
    }
}
