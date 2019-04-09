package main;

import htsjdk.samtools.fastq.FastqRecord;
import htsjdk.samtools.fastq.FastqReader;
import htsjdk.samtools.fastq.FastqWriter;
import htsjdk.samtools.fastq.FastqWriterFactory;

import java.util.BitSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import algo.*;
import data.*;
import util.Read;
import util.FASTQRead;
import util.ReadFreq;

public class DeduplicateFASTQ{
    public void deduplicateAndMerge(File in, File out, Algo algo, Data data, Merge merge, int umiLength, int k, float percentage){
        FastqReader reader = new FastqReader(in);
        Map<BitSet, ReadFreq> umiRead = new HashMap<>();

        int length = 0;

        for(FastqRecord record : reader){
            length = record.getReadLength();
            Read read = new FASTQRead(record.getReadHeader(), record.getReadString(), record.getBaseQualityString());
            BitSet umi = read.getUMI();

            if(umiRead.containsKey(umi)){
                ReadFreq prev = umiRead.get(umi);
                prev.read = merge.merge(read, prev.read);
                prev.freq++;
            }else{
                umiRead.put(umi, new ReadFreq(read, 1));
            }
        }

        reader.close();

        System.gc();

        List<Read> deduped;

        if(algo instanceof Algorithm)
            deduped = ((Algorithm)algo).apply(umiRead, ((DataStructure)data), length, k, percentage);
        else
            deduped = ((ParallelAlgorithm)algo).apply(umiRead, ((ParallelDataStructure)data), length, k, percentage);

        FastqWriter writer = new FastqWriterFactory().newWriter(out);

        for(Read read : deduped)
            writer.write(((FASTQRead)read).toFASTQRecord(umiLength));

        writer.close();
    }
}
