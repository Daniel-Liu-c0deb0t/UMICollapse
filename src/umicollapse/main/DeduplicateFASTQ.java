package umicollapse.main;

import htsjdk.samtools.fastq.FastqRecord;
import htsjdk.samtools.fastq.FastqReader;
import htsjdk.samtools.fastq.FastqWriter;
import htsjdk.samtools.fastq.FastqWriterFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import java.io.File;

import umicollapse.util.BitSet;
import umicollapse.algo.*;
import umicollapse.data.*;
import umicollapse.merge.*;
import umicollapse.util.Read;
import umicollapse.util.FASTQRead;
import umicollapse.util.ReadFreq;

public class DeduplicateFASTQ{
    public void deduplicateAndMerge(File in, File out, Algo algo, Data data, Merge merge, int umiLength, int k, float percentage){
        if(umiLength == -1)
            umiLength = 0;

        FastqReader reader = new FastqReader(in);
        Map<Integer, Map<BitSet, ReadFreq>> readLength = new HashMap<>();

        int readCount = 0;

        for(FastqRecord record : reader){
            int length = record.getReadLength();

            if(!readLength.containsKey(length))
                readLength.put(length, new HashMap<BitSet, ReadFreq>());

            Map<BitSet, ReadFreq> umiRead = readLength.get(length);

            Read read = new FASTQRead(record.getReadName(), record.getReadString(), record.getBaseQualityString());
            BitSet umi = read.getUMI();

            if(umiRead.containsKey(umi)){
                ReadFreq prev = umiRead.get(umi);
                prev.read = merge.merge(read, prev.read);
                prev.freq++;
            }else{
                umiRead.put(umi, new ReadFreq(read, 1));
            }

            readCount++;
        }

        reader.close();

        System.gc(); // attempt to clear up memory before deduplicating

        int uniqueCount = 0;
        int dedupedCount = 0;
        FastqWriter writer = new FastqWriterFactory().newWriter(out);

        for(Map.Entry<Integer, Map<BitSet, ReadFreq>> e : readLength.entrySet()){
            uniqueCount += e.getValue().size();
            List<Read> deduped;

            if(algo instanceof Algorithm)
                deduped = ((Algorithm)algo).apply(e.getValue(), ((DataStructure)data), e.getKey(), k, percentage);
            else
                deduped = ((ParallelAlgorithm)algo).apply(e.getValue(), ((ParallelDataStructure)data), e.getKey(), k, percentage);

            dedupedCount += deduped.size();

            for(Read read : deduped)
                writer.write(((FASTQRead)read).toFASTQRecord(e.getKey(), umiLength));
        }

        writer.close();

        System.out.println("Number of input reads\t" + readCount);
        System.out.println("Number of unique reads\t" + uniqueCount);
        System.out.println("Number of reads after deduplicating\t" + dedupedCount);
    }
}
