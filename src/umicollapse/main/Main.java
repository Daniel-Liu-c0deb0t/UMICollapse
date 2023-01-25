package umicollapse.main;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.File;

import umicollapse.data.*;
import umicollapse.merge.*;
import umicollapse.algo.*;

public class Main{
    public static void main(String[] args){
        System.out.println("Arguments\t" + Arrays.toString(args));

        long startTime = System.currentTimeMillis();

        if(args.length == 0)
            throw new IllegalArgumentException("No arguments specified!");

        Map<String, List<String>> m = new HashMap<>();
        String curr = null;

        for(int i = 1; i < args.length; i++){
            if(args[i].startsWith("-")){
                curr = args[i];
                m.put(args[i], new ArrayList<String>());
            }else{
                m.get(curr).add(args[i]);
            }
        }

        Map<Boolean, Map<String, Class<? extends Algo>>> algo = new HashMap<>();
        Map<String, Class<? extends Algo>> a1 = new HashMap<>();
        a1.put("adj", Adjacency.class);
        a1.put("dir", Directional.class);
        a1.put("cc", ConnectedComponents.class);
        algo.put(false, a1);
        Map<String, Class<? extends Algo>> a2 = new HashMap<>();
        a2.put("adj", ParallelAdjacency.class);
        a2.put("dir", ParallelDirectional.class);
        a2.put("cc", ParallelConnectedComponents.class);
        algo.put(true, a2);

        Map<Boolean, Map<String, Class<? extends Data>>> data = new HashMap<>();
        Map<String, Class<? extends Data>> d1 = new HashMap<>();
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
        data.put(false, d1);
        Map<String, Class<? extends Data>> d2 = new HashMap<>();
        d2.put("naive", ParallelNaive.class);
        d2.put("bktree", ParallelBKTree.class);
        d2.put("fenwickbktree", ParallelFenwickBKTree.class);
        data.put(true, d2);

        Map<String, Class<? extends Merge>> merge = new HashMap<>();
        merge.put("any", AnyMerge.class);
        merge.put("avgqual", AvgQualMerge.class);
        merge.put("mapqual", MapQualMerge.class);

        String mode = args[0];
        File in = null;
        File out = null;
        String algoStr = "dir";
        String dataStr = "ngrambktree";
        String mergeStr = mode.equals("fastq") ? "avgqual" : "mapqual";
        int k = 1;
        int umiLength = -1;
        float percentage = 0.5f;
        String umiSeparator = "_";

        boolean parallelData = false;
        boolean parallelAlign = false;

        boolean twoPass = false;

        boolean paired = false;
        boolean removeUnpaired = false;
        boolean removeChimeric = false;
        boolean keepUnmapped = false;

        boolean trackClusters = false;

        String s = "-k";

        if(m.containsKey(s))
            k = Integer.parseInt(m.get(s).get(0));

        s = "-u";

        if(m.containsKey(s))
            umiLength = Integer.parseInt(m.get(s).get(0));

        s = "-p";

        if(m.containsKey(s))
            percentage = Float.parseFloat(m.get(s).get(0));

        s = "-i";

        if(m.containsKey(s))
            in = new File(m.get(s).get(0));
        else
            throw new IllegalArgumentException("Missing input file!");

        s = "-o";

        if(m.containsKey(s))
            out = new File(m.get(s).get(0));
        else
            throw new IllegalArgumentException("Missing output file!");

        s = "-t";

        if(m.containsKey(s)){
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", (Integer.parseInt(m.get(s).get(0)) - 1) + "");
            parallelAlign = true;
            parallelData = false;
        }

        s = "-T";

        if(m.containsKey(s)){
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", (Integer.parseInt(m.get(s).get(0)) - 1) + "");
            parallelData = true;
            parallelAlign = false;
        }

        s = "--algo";

        if(m.containsKey(s))
            algoStr = m.get(s).get(0);

        s = "--data";

        if(m.containsKey(s))
            dataStr = m.get(s).get(0);

        s = "--merge";

        if(m.containsKey(s))
            mergeStr = m.get(s).get(0);

        s = "--umi-sep";

        if(m.containsKey(s))
            umiSeparator = m.get(s).get(0);

        s = "--two-pass";

        if(m.containsKey(s))
            twoPass = true;

        s = "--paired";

        if(m.containsKey(s))
            paired = true;

        s = "--remove-unpaired";

        if(m.containsKey(s))
            removeUnpaired = true;

        s = "--remove-chimeric";

        if(m.containsKey(s))
            removeChimeric = true;

        s = "--keep-unmapped";

        if(m.containsKey(s))
            keepUnmapped = true;

        s = "--tag";

        if(m.containsKey(s))
            trackClusters = true;

        if(trackClusters && twoPass)
            throw new UnsupportedOperationException("Cannot track clusters with the two pass algorithm!");

        if(paired && parallelAlign)
            throw new UnsupportedOperationException("Cannot process paired-end reads in parallel!");

        if(paired && keepUnmapped)
            throw new UnsupportedOperationException("Cannot keep unmapped reads with paired-end reads!");

        Algo a = null;
        Class<? extends Data> d = null;
        Merge mAlgo = null;

        d = data.get(parallelData).get(dataStr);

        try{
            a = algo.get(parallelData).get(algoStr).getDeclaredConstructor().newInstance();
            mAlgo = merge.get(mergeStr).getDeclaredConstructor().newInstance();
        }catch(Exception e){
            e.printStackTrace();
        }

        if(mode.equals("fastq")){
            DeduplicateFASTQ dedup = new DeduplicateFASTQ();
            dedup.deduplicateAndMerge(in, out, a, d, mAlgo, umiLength, k, percentage, parallelAlign, trackClusters);
        }else if(mode.equals("bam") || mode.equals("sam")){
            DeduplicateSAM dedup = new DeduplicateSAM();

            if(twoPass){
                dedup.deduplicateAndMergeTwoPass(in, out, a, d, mAlgo, umiLength, k, percentage, umiSeparator, paired, removeUnpaired, removeChimeric, keepUnmapped, trackClusters);
            }else{
                dedup.deduplicateAndMerge(in, out, a, d, mAlgo, umiLength, k, percentage, parallelAlign, umiSeparator, paired, removeUnpaired, removeChimeric, keepUnmapped, trackClusters);
            }
        }

        System.out.println("UMI collapsing finished in " + ((System.currentTimeMillis() - startTime) / 1000.0) + " seconds!");
    }
}
