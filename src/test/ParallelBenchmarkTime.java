package test;

import java.util.Map;
import java.util.HashMap;
import java.util.BitSet;
import java.util.Random;

import util.Utils;
import util.Read;
import data.*;

public class ParallelBenchmarkTime{
    public static void main(String[] args){
        int numRand = 1000;
        int numDup = 20;
        int numIter = 5;
        int umiLength = 100;
        int k = 1;
        ParallelDataStructure data = new ParallelFenwickBKTree();
        Random rand = new Random(1234); // fixed seed

        System.out.println("Parallel data structure\t" + data.getClass().getName());
        System.out.println("Number of random iterations\t" + numRand);
        System.out.println("Number of duplicates\t" + numDup);
        System.out.println("Number of testing iterations\t" + numIter);
        System.out.println("UMI length\t" + umiLength);
        System.out.println("Max number of edits\t" + k);

        Map<BitSet, Integer> umiFreq = TestUtils.generateData(numRand, numDup, umiLength, k, rand);

        System.out.println("Actual number of UMIs\t" + umiFreq.size());

        long avgTime = 0L;

        for(int i = 0; i < numIter + 1; i++){
            System.gc();

            long time = runTest(data, umiFreq, umiLength, k);

            if(i > 0) // first time is warm-up
                avgTime += time;
        }

        avgTime /= numIter;

        System.out.println("Average time (ms)\t" + avgTime);
    }

    private static long runTest(ParallelDataStructure data, Map<BitSet, Integer> umiFreq, int umiLength, int k){
        long start = System.currentTimeMillis();

        data.init(new HashMap<BitSet, Integer>(umiFreq), umiLength, k);

        for(BitSet umi : umiFreq.keySet())
            data.near(umi, k, Integer.MAX_VALUE);

        return System.currentTimeMillis() - start;
    }
}
