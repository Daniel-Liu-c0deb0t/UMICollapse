package test;

import java.util.Map;
import java.util.HashMap;
import java.util.BitSet;
import java.util.Random;

import util.Utils;
import util.Read;
import data.*;
import algo.*;

public class Test{
    public static void main(String[] args){
        int numRand = 100;
        int numDup = 100;
        int numIter = 10;
        int umiLength = 10;
        int k = 1;
        DataStructure data = new BKTree();
        Random rand = new Random(1234); // fixed seed

        System.out.println("Data structure\t" + data.getClass().getName());
        System.out.println("Number of random iterations\t" + numRand);
        System.out.println("Number of duplicates\t" + numDup);
        System.out.println("Number of testing iterations\t" + numIter);
        System.out.println("UMI length\t" + umiLength);
        System.out.println("Max number of edits\t" + k);

        Map<BitSet, Integer> umiFreq = generateData(numRand, numDup, umiLength, k, rand);

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

    private static long runTest(DataStructure data, Map<BitSet, Integer> umiFreq, int umiLength, int k){
        long start = System.currentTimeMillis();

        data.init(new HashMap<BitSet, Integer>(umiFreq), umiLength, k);

        for(BitSet umi : umiFreq.keySet())
            data.removeNear(umi, k, Integer.MAX_VALUE);

        return System.currentTimeMillis() - start;
    }

    private static Map<BitSet, Integer> generateData(int numRand, int numDup, int umiLength, int k, Random rand){
        Map<BitSet, Integer> res = new HashMap<>();

        for(int i = 0; i < numRand; i++){
            String umi = randUMI(umiLength, rand);

            for(int j = 0; j < numDup; j++)
                res.put(Utils.toBitSet(randEdits(umi, k, rand)), 0);
        }

        return res;
    }

    private static String randUMI(int n, Random rand){
        char[] res = new char[n];

        for(int i = 0; i < n; i++)
            res[i] = Read.ALPHABET[rand.nextInt(Read.ALPHABET.length)];

        return new String(res);
    }

    private static String randEdits(String s, int k, Random rand){
        char[] res = s.toCharArray();
        int edits = rand.nextInt(k + 1);

        for(int i = 0; i < edits; i++){
            int idx = rand.nextInt(s.length());
            res[idx] = pick(Read.ALPHABET, res[idx], rand);
        }

        return new String(res);
    }

    private static char pick(char[] alphabet, char exclude, Random rand){
        int idx = rand.nextInt(alphabet.length - 1) + 1;

        for(int i = 0; i < alphabet.length; i++){
            if(alphabet[i] != exclude)
                idx--;

            if(idx == 0)
                return alphabet[i];
        }

        return '?'; // impossible
    }
}
