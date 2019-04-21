package umicollapse.main;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import java.util.stream.Stream;

import java.io.File;

import umicollapse.util.BitSet;
import umicollapse.data.*;
import umicollapse.algo.*;
import umicollapse.merge.*;
import umicollapse.util.Read;
import umicollapse.util.SAMRead;
import umicollapse.util.ReadFreq;

public class DeduplicateSAM{
    private float avgUMICount;
    private int maxUMICount;
    private int dedupedCount;
    private int umiLength;

    public void deduplicateAndMerge(File in, File out, Algo algo, Class<? extends Data> dataClass, Merge merge, int umiLengthParam, int k, float percentage, boolean parallel){
        SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(in);
        Map<Integer, Map<BitSet, ReadFreq>> alignStarts = new HashMap<>();
        Map<Integer, Map<BitSet, ReadFreq>> alignEnds = new HashMap<>();

        umiLength = umiLengthParam;
        int readCount = 0;

        for(SAMRecord record : reader){
            if(record.getReadUnmappedFlag()) // discard unmapped reads
                continue;

            int start = record.getReadNegativeStrandFlag() ? record.getUnclippedEnd() : record.getUnclippedStart();
            Map<Integer, Map<BitSet, ReadFreq>> align = record.getReadNegativeStrandFlag() ? alignEnds : alignStarts;

            if(!align.containsKey(start))
                align.put(start, new HashMap<BitSet, ReadFreq>());

            Map<BitSet, ReadFreq> umiRead = align.get(start);

            Read read = new SAMRead(record);
            BitSet umi = read.getUMI();

            if(umiLength == -1)
                umiLength = read.getUMILength();

            if(umiRead.containsKey(umi)){
                ReadFreq prev = umiRead.get(umi);
                prev.read = merge.merge(read, prev.read);
                prev.freq++;
            }else{
                umiRead.put(umi, new ReadFreq(read, 1));
            }

            readCount++;
        }

        SAMFileWriter writer = new SAMFileWriterFactory().makeSAMOrBAMWriter(reader.getFileHeader(), false, out);

        try{
            reader.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        System.gc(); // attempt to clear up memory before deduplicating

        System.out.println("Done reading input file into memory!");

        int alignPosCount = alignStarts.size() + alignEnds.size();
        avgUMICount = 0.0f;
        maxUMICount = 0;
        dedupedCount = 0;
        Object lock = new Object();

        Stream<Map.Entry<Integer, Map<BitSet, ReadFreq>>> stream = parallel ?
            alignStarts.entrySet().parallelStream() : alignStarts.entrySet().stream();

        stream.forEach(e -> {
            List<Read> deduped;
            Data data = null;

            try{
                data = dataClass.getDeclaredConstructor().newInstance();
            }catch(Exception ex){
                ex.printStackTrace();
            }

            if(algo instanceof Algorithm)
                deduped = ((Algorithm)algo).apply(e.getValue(), (DataStructure)data, umiLength, k, percentage);
            else
                deduped = ((ParallelAlgorithm)algo).apply(e.getValue(), (ParallelDataStructure)data, umiLength, k, percentage);

            synchronized(lock){
                avgUMICount += (float)e.getValue().size() / alignPosCount;
                maxUMICount = Math.max(maxUMICount, e.getValue().size());
                dedupedCount += deduped.size();

                for(Read read : deduped)
                    writer.addAlignment(((SAMRead)read).toSAMRecord());
            }
        });

        stream = parallel ? alignEnds.entrySet().parallelStream() : alignEnds.entrySet().stream();

        stream.forEach(e -> {
            List<Read> deduped;
            Data data = null;

            try{
                data = dataClass.getDeclaredConstructor().newInstance();
            }catch(Exception ex){
                ex.printStackTrace();
            }

            if(algo instanceof Algorithm)
                deduped = ((Algorithm)algo).apply(e.getValue(), (DataStructure)data, umiLength, k, percentage);
            else
                deduped = ((ParallelAlgorithm)algo).apply(e.getValue(), (ParallelDataStructure)data, umiLength, k, percentage);

            synchronized(lock){
                avgUMICount += (float)e.getValue().size() / alignPosCount;
                maxUMICount = Math.max(maxUMICount, e.getValue().size());
                dedupedCount += deduped.size();

                for(Read read : deduped)
                    writer.addAlignment(((SAMRead)read).toSAMRecord());
            }
        });

        writer.close();

        System.out.println("Number of input reads\t" + readCount);
        System.out.println("Number of unique alignment positions\t" + alignPosCount);
        System.out.println("Average number of UMIs per alignment position\t" + avgUMICount);
        System.out.println("Max number of UMIs over all alignment positions\t" + maxUMICount);
        System.out.println("Number of reads after deduplicating\t" + dedupedCount);
    }
}
