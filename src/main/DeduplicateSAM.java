package main;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import java.io.File;

import util.BitSet;
import data.*;
import algo.*;
import merge.*;
import util.Read;
import util.SAMRead;
import util.ReadFreq;

public class DeduplicateSAM{
    public void deduplicateAndMerge(File in, File out, Algo algo, Data data, Merge merge, int umiLength, int k, float percentage){
        SamReader reader = SamReaderFactory.makeDefault().open(in);
        Map<Integer, Map<BitSet, ReadFreq>> alignStarts = new HashMap<>();

        for(SAMRecord record : reader){
            int start = record.getAlignmentStart();

            if(!alignStarts.containsKey(start))
                alignStarts.put(start, new HashMap<BitSet, ReadFreq>());

            Map<BitSet, ReadFreq> umiRead = alignStarts.get(start);

            Read read = new SAMRead(record);
            BitSet umi = read.getUMI();

            if(umiRead.containsKey(umi)){
                ReadFreq prev = umiRead.get(umi);
                prev.read = merge.merge(read, prev.read);
                prev.freq++;
            }else{
                umiRead.put(umi, new ReadFreq(read, 1));
            }
        }

        SAMFileWriter writer = new SAMFileWriterFactory().makeSAMOrBAMWriter(reader.getFileHeader(), false, out);

        try{
            reader.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        System.gc(); // attempt to clear up memory before deduplicating

        for(Map.Entry<Integer, Map<BitSet, ReadFreq>> e : alignStarts.entrySet()){
            List<Read> deduped;

            if(algo instanceof Algorithm)
                deduped = ((Algorithm)algo).apply(e.getValue(), (DataStructure)data, umiLength, k, percentage);
            else
                deduped = ((ParallelAlgorithm)algo).apply(e.getValue(), (ParallelDataStructure)data, umiLength, k, percentage);

            for(Read read : deduped)
                writer.addAlignment(((SAMRead)read).toSAMRecord());
        }

        writer.close();
    }
}
