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

public class ParallelBenchmarkTime{
    public static void main(String[] args){
        int numRand = 1000;
        int numDup = 20;
        int numIter = 5;
        int umiLength = 100;
        int k = 1;
        int threadCount = 2;
        float percentage = 0.5f;
        ParallelAlgorithm algo = new ParallelConnectedComponents();
        ParallelDataStructure data = new ParallelFenwickBKTree();
        Random rand = new Random(1234); // fixed seed

        System.out.println("Parallel algorithm\t" + algo.getClass().getName());
        System.out.println("Parallel data structure\t" + data.getClass().getName());
        System.out.println("Number of random iterations\t" + numRand);
        System.out.println("Number of duplicates\t" + numDup);
        System.out.println("Number of testing iterations\t" + numIter);
        System.out.println("UMI length\t" + umiLength);
        System.out.println("Max number of edits\t" + k);
        System.out.println("Thread count\t" + threadCount);

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", (threadCount - 1) + "");

        Map<BitSet, ReadFreq> umiFreq = TestUtils.generateData(numRand, numDup, umiLength, k, percentage, rand);

        System.out.println("Actual number of UMIs\t" + umiFreq.size());

        long avgTime = 0L;

        for(int i = 0; i < numIter + 1; i++){
            System.gc();

            long time = runTest(algo, data, umiFreq, umiLength, k, percentage);

            if(i > 0) // first time is warm-up
                avgTime += time;
        }

        avgTime /= numIter;

        System.out.println("Average time (ms)\t" + avgTime);
    }

    private static long runTest(ParallelAlgorithm algo, ParallelDataStructure data, Map<BitSet, ReadFreq> umiFreq, int umiLength, int k, float percentage){
        long start = System.currentTimeMillis();

        algo.apply(umiFreq, data, new ClusterTracker(false), umiLength, k, percentage);

        return System.currentTimeMillis() - start;
    }
}
