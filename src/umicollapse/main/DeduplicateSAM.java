package umicollapse.main;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import java.io.File;

import umicollapse.util.BitSet;
import umicollapse.data.*;
import umicollapse.algo.*;
import umicollapse.merge.*;
import umicollapse.util.Read;
import umicollapse.util.SAMRead;
import umicollapse.util.ReadFreq;

public class DeduplicateSAM{
    public void deduplicateAndMerge(File in, File out, Algo algo, Data data, Merge merge, int umiLength, int k, float percentage){
        SamReader reader = SamReaderFactory.makeDefault().open(in);
        Map<Integer, Map<BitSet, ReadFreq>> alignStarts = new HashMap<>();

        int readCount = 0;

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

            readCount++;
        }

        SAMFileWriter writer = new SAMFileWriterFactory().makeSAMOrBAMWriter(reader.getFileHeader(), false, out);

        try{
            reader.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        System.gc(); // attempt to clear up memory before deduplicating

        int alignPosCount = alignStarts.size();
        float avgUMICount = 0.0f;
        int maxUMICount = 0;
        int dedupedCount = 0;

        for(Map.Entry<Integer, Map<BitSet, ReadFreq>> e : alignStarts.entrySet()){
            avgUMICount += (float)e.getValue().size() / alignPosCount;
            maxUMICount = Math.max(maxUMICount, e.getValue().size());
            List<Read> deduped;

            if(algo instanceof Algorithm)
                deduped = ((Algorithm)algo).apply(e.getValue(), (DataStructure)data, umiLength, k, percentage);
            else
                deduped = ((ParallelAlgorithm)algo).apply(e.getValue(), (ParallelDataStructure)data, umiLength, k, percentage);

            dedupedCount += deduped.size();

            for(Read read : deduped)
                writer.addAlignment(((SAMRead)read).toSAMRecord());
        }

        writer.close();

        System.out.println("Number of input reads\t" + readCount);
        System.out.println("Number of unique alignment positions\t" + alignPosCount);
        System.out.println("Average number of UMIs per alignment position\t" + avgUMICount);
        System.out.println("Max number of UMIs over all alignment positions\t" + maxUMICount);
        System.out.println("Number of reads after deduplicating\t" + dedupedCount);
    }
}
