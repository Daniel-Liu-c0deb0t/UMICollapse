package umicollapse.main;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
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
import umicollapse.util.ClusterTracker;
import umicollapse.util.Utils;
import static umicollapse.util.Utils.HASH_CONST;

public class DeduplicateSAM{
    private int avgUMICount;
    private int maxUMICount;
    private int dedupedCount;
    private int umiLength;

    public void deduplicateAndMerge(File in, File out, Algo algo, Class<? extends Data> dataClass, Merge merge, int umiLengthParam, int k, float percentage, boolean parallel, String umiSeparator, boolean paired, boolean removeUnpaired, boolean removeChimeric, boolean keepUnmapped, boolean trackClusters){
        SAMRead.setDefaultUMIPattern(umiSeparator);

        SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(in);
        Writer writer = new Writer(in, out, reader, paired);
        Map<Alignment, Map<BitSet, ReadFreq>> align = new HashMap<>(1 << 16);

        umiLength = umiLengthParam;
        int totalReadCount = 0;
        int unmapped = 0;
        int unpaired = 0;
        int chimeric = 0;
        int readCount = 0;

        for(SAMRecord record : reader){
            // always skip the reversed read
            if(paired && record.getReadPairedFlag() && record.getSecondOfPairFlag())
                continue;

            totalReadCount++;

            if(record.getReadUnmappedFlag()){ // discard unmapped reads
                unmapped++;
                if(keepUnmapped)
                    writer.write(record);
                continue;
            }

            if(paired){
                if(!record.getReadPairedFlag()){
                    unpaired++;

                    if(removeUnpaired)
                        continue;
                }

                if(record.getReadPairedFlag() && record.getMateUnmappedFlag()){
                    unmapped++;
                    continue;
                }

                if(record.getReadPairedFlag() && !record.getReferenceName().equals(record.getMateReferenceName())){
                    chimeric++;

                    if(removeChimeric)
                        continue;
                }
            }

            Alignment alignment = null;

            if(paired){
                alignment = new PairedAlignment(
                        record.getReadNegativeStrandFlag(),
                        record.getReadNegativeStrandFlag() ? record.getUnclippedEnd() : record.getUnclippedStart(),
                        record.getReferenceName(),
                        record.getInferredInsertSize()
                );
            }else{
                alignment = new Alignment(
                        record.getReadNegativeStrandFlag(),
                        record.getReadNegativeStrandFlag() ? record.getUnclippedEnd() : record.getUnclippedStart(),
                        record.getReferenceName()
                );
            }

            if(!align.containsKey(alignment))
                align.put(alignment, new HashMap<BitSet, ReadFreq>(4));

            Map<BitSet, ReadFreq> umiRead = align.get(alignment);

            Read read = new SAMRead(record);
            BitSet umi = read.getUMI(umiLength);

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

        try{
            reader.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        reader = null;

        System.gc(); // attempt to clear up memory before deduplicating

        System.out.println("Done reading input file into memory!");

        int alignPosCount = align.size();
        avgUMICount = 0;
        maxUMICount = 0;
        dedupedCount = 0;
        Object lock = new Object();

        final Map<Alignment, ClusterTracker> clusterTrackers = trackClusters ? new HashMap<Alignment, ClusterTracker>() : null;

        Stream<Map.Entry<Alignment, Map<BitSet, ReadFreq>>> stream =
            parallel ? align.entrySet().parallelStream() : ((paired && !trackClusters) ? align.entrySet().stream().sorted((a, b) -> a.getKey().getRef().compareTo(b.getKey().getRef())) : align.entrySet().stream());

        stream.forEach(e -> {
            List<Read> deduped;
            Data data = null;

            try{
                data = dataClass.getDeclaredConstructor().newInstance();
            }catch(Exception ex){
                ex.printStackTrace();
            }

            ClusterTracker currTracker = new ClusterTracker(trackClusters);

            if(algo instanceof Algorithm)
                deduped = ((Algorithm)algo).apply(e.getValue(), (DataStructure)data, currTracker, umiLength, k, percentage);
            else
                deduped = ((ParallelAlgorithm)algo).apply(e.getValue(), (ParallelDataStructure)data, currTracker, umiLength, k, percentage);

            synchronized(lock){
                currTracker.setOffset(dedupedCount);

                avgUMICount += e.getValue().size();
                maxUMICount = Math.max(maxUMICount, e.getValue().size());
                dedupedCount += deduped.size();

                if(trackClusters){
                    clusterTrackers.put(e.getKey(), currTracker);
                }else{
                    for(Read read : deduped)
                        writer.write(((SAMRead)read).toSAMRecord());
                }
            }
        });

        // second pass to tag reads with their cluster and other stats
        if(trackClusters){
            System.gc(); // attempt to clear up memory before second pass

            System.out.println("Done with the first pass for tracking clusters!");

            SamReader reader2 = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(in);

            for(SAMRecord record : reader2){
                if(record.getReadUnmappedFlag()) // discard unmapped reads
                    continue;

                if(paired && ((removeUnpaired && !record.getReadPairedFlag()) // discard unpaired
                            || (record.getReadPairedFlag() && record.getSecondOfPairFlag()) // ignore reversed reads
                            || (record.getReadPairedFlag() && record.getMateUnmappedFlag()) // discard unmapped reads
                            || (removeChimeric && record.getReadPairedFlag()
                                && !record.getReferenceName().equals(record.getMateReferenceName())))){ // discard chimeric reads
                    continue;
                }

                Alignment alignment = null;

                if(paired){
                    alignment = new PairedAlignment(
                            record.getReadNegativeStrandFlag(),
                            record.getReadNegativeStrandFlag() ? record.getUnclippedEnd() : record.getUnclippedStart(),
                            record.getReferenceName(),
                            record.getInferredInsertSize()
                    );
                }else{
                    alignment = new Alignment(
                            record.getReadNegativeStrandFlag(),
                            record.getReadNegativeStrandFlag() ? record.getUnclippedEnd() : record.getUnclippedStart(),
                            record.getReferenceName()
                    );
                }

                ClusterTracker currTracker = clusterTrackers.get(alignment);
                Map<BitSet, ReadFreq> map = align.get(alignment);

                Read read = new SAMRead(record);
                BitSet umi = read.getUMI(umiLength);

                int id = currTracker.getId(umi);
                ClusterTracker.ClusterStats stats = currTracker.getStats(id);
                int absId = id + currTracker.getOffset();
                SAMRecord record2 = record.deepCopy();
                ReadFreq readFreq = map.get(umi);

                record2.setAttribute("MI", absId + "");
                record2.setAttribute("RX", Utils.toString(stats.getUMI(), umiLength));

                if(stats.getUMI().equals(umi) && stats.getRead().equals(read)){
                    record2.setAttribute("cs", stats.getFreq());
                    record2.setAttribute("su", readFreq.freq);
                }else{
                    record2.setDuplicateReadFlag(true);

                    if(readFreq.read.equals(read))
                        record2.setAttribute("su", readFreq.freq);
                }

                writer.write(record2);
            }

            try{
                reader2.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        writer.close();

        System.out.println("Number of input reads\t" + totalReadCount);
        System.out.println("Number of removed unmapped reads\t" + unmapped);

        if(paired){
            System.out.println("Number of unpaired reads\t" + unpaired);
            System.out.println("Number of chimeric reads\t" + chimeric);
        }

        System.out.println("Number of unremoved reads\t" + readCount);
        System.out.println("Number of unique alignment positions\t" + alignPosCount);
        System.out.println("Average number of UMIs per alignment position\t" + ((double)avgUMICount / alignPosCount));
        System.out.println("Max number of UMIs over all alignment positions\t" + maxUMICount);

        if(trackClusters)
            System.out.println("Number of groups of reads\t" + dedupedCount);
        else
            System.out.println("Number of reads after deduplicating\t" + dedupedCount);
    }

    // trade off speed for lower memory usage
    // input should be sorted based on alignment for best results
    public void deduplicateAndMergeTwoPass(File in, File out, Algo algo, Class<? extends Data> dataClass, Merge merge, int umiLengthParam, int k, float percentage, String umiSeparator, boolean paired, boolean removeUnpaired, boolean removeChimeric, boolean keepUnmapped, boolean trackClusters){
        SamReader firstPass = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(in);
        Writer writer = new Writer(in, out, firstPass, paired);
        Map<Alignment, AlignReads> align = new HashMap<>(1 << 16);
        int totalReadCount = 0;
        int unmapped = 0;
        int unpaired = 0;
        int chimeric = 0;
        int readCount = 0;

        // first pass to figure out where each alignment position ends
        for(SAMRecord record : firstPass){
            // always skip the reversed read
            if(paired && record.getReadPairedFlag() && record.getSecondOfPairFlag())
                continue;

            totalReadCount++;

            if(record.getReadUnmappedFlag()){ // discard unmapped reads
                unmapped++;
                if(keepUnmapped)
                    writer.write(record);
                continue;
            }

            if(paired){
                if(!record.getReadPairedFlag()){
                    unpaired++;

                    if(removeUnpaired)
                        continue;
                }

                if(record.getReadPairedFlag() && record.getMateUnmappedFlag()){
                    unmapped++;
                    continue;
                }

                if(record.getReadPairedFlag() && !record.getReferenceName().equals(record.getMateReferenceName())){
                    chimeric++;

                    if(removeChimeric)
                        continue;
                }
            }

            Alignment alignment = null;

            if(paired){
                alignment = new PairedAlignment(
                        record.getReadNegativeStrandFlag(),
                        record.getReadNegativeStrandFlag() ? record.getUnclippedEnd() : record.getUnclippedStart(),
                        record.getReferenceName(),
                        record.getInferredInsertSize()
                );
            }else{
                alignment = new Alignment(
                        record.getReadNegativeStrandFlag(),
                        record.getReadNegativeStrandFlag() ? record.getUnclippedEnd() : record.getUnclippedStart(),
                        record.getReferenceName()
                );
            }

            if(!align.containsKey(alignment))
                align.put(alignment, new AlignReads());

            align.get(alignment).latest = readCount;
            readCount++;
        }

        try{
            firstPass.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        firstPass = null;

        System.gc(); // attempt to clear up memory before second pass

        System.out.println("Done with the first pass!");

        SAMRead.setDefaultUMIPattern(umiSeparator);

        SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(in);

        umiLength = umiLengthParam;
        int idx = 0;
        int alignPosCount = align.size();
        avgUMICount = 0;
        maxUMICount = 0;
        dedupedCount = 0;

        for(SAMRecord record : reader){
            if(record.getReadUnmappedFlag()) // discard unmapped reads
                continue;

            if(paired && ((removeUnpaired && !record.getReadPairedFlag()) // discard unpaired
                        || (record.getReadPairedFlag() && record.getSecondOfPairFlag()) // ignore reversed reads
                        || (record.getReadPairedFlag() && record.getMateUnmappedFlag()) // discard unmapped reads
                        || (removeChimeric && record.getReadPairedFlag()
                            && !record.getReferenceName().equals(record.getMateReferenceName())))){ // discard chimeric reads
                continue;
            }

            Alignment alignment = null;

            if(paired){
                alignment = new PairedAlignment(
                        record.getReadNegativeStrandFlag(),
                        record.getReadNegativeStrandFlag() ? record.getUnclippedEnd() : record.getUnclippedStart(),
                        record.getReferenceName(),
                        record.getInferredInsertSize()
                );
            }else{
                alignment = new Alignment(
                        record.getReadNegativeStrandFlag(),
                        record.getReadNegativeStrandFlag() ? record.getUnclippedEnd() : record.getUnclippedStart(),
                        record.getReferenceName()
                );
            }

            AlignReads alignReads = align.get(alignment);

            if(alignReads.umiRead == null)
                alignReads.umiRead = new HashMap<BitSet, ReadFreq>(4);

            Read read = new SAMRead(record);
            BitSet umi = read.getUMI(umiLength);

            if(umiLength == -1)
                umiLength = read.getUMILength();

            if(alignReads.umiRead.containsKey(umi)){
                ReadFreq prev = alignReads.umiRead.get(umi);
                prev.read = merge.merge(read, prev.read);
                prev.freq++;
            }else{
                alignReads.umiRead.put(umi, new ReadFreq(read, 1));
            }

            if(idx >= alignReads.latest){
                List<Read> deduped;
                Data data = null;

                try{
                    data = dataClass.getDeclaredConstructor().newInstance();
                }catch(Exception ex){
                    ex.printStackTrace();
                }

                if(algo instanceof Algorithm)
                    deduped = ((Algorithm)algo).apply(alignReads.umiRead, (DataStructure)data, new ClusterTracker(trackClusters), umiLength, k, percentage);
                else
                    deduped = ((ParallelAlgorithm)algo).apply(alignReads.umiRead, (ParallelDataStructure)data, new ClusterTracker(trackClusters), umiLength, k, percentage);

                avgUMICount += alignReads.umiRead.size();
                maxUMICount = Math.max(maxUMICount, alignReads.umiRead.size());
                dedupedCount += deduped.size();

                for(Read r : deduped)
                    writer.write(((SAMRead)r).toSAMRecord());

                // done with the current alignment position, so free up memory
                align.remove(alignment);
            }

            idx++;
        }

        try{
            reader.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        writer.close();

        System.out.println("Number of input reads\t" + totalReadCount);
        System.out.println("Number of removed unmapped reads\t" + unmapped);

        if(paired){
            System.out.println("Number of unpaired reads\t" + unpaired);
            System.out.println("Number of chimeric reads\t" + chimeric);
        }

        System.out.println("Number of unremoved reads\t" + readCount);
        System.out.println("Number of unique alignment positions\t" + alignPosCount);
        System.out.println("Average number of UMIs per alignment position\t" + ((double)avgUMICount / alignPosCount));
        System.out.println("Max number of UMIs over all alignment positions\t" + maxUMICount);
        System.out.println("Number of reads after deduplicating\t" + dedupedCount);
    }

    private static class ReversedRead implements Comparable{
        private String name, ref;
        private int coord;

        public ReversedRead(String name, String ref, int coord){
            this.name = name;
            this.ref = ref.intern();
            this.coord = coord;
        }

        @Override
        public boolean equals(Object o){
            if(!(o instanceof ReversedRead))
                return false;

            ReversedRead a = (ReversedRead)o;

            if(this == a)
                return true;

            if(ref != a.ref)
                return false;

            if(!name.equals(a.name))
                return false;

            return true;
        }

        @Override
        public int hashCode(){
            int hash = name.hashCode();
            hash = hash * HASH_CONST + ref.hashCode();
            hash = hash * HASH_CONST + coord;
            return hash;
        }

        @Override
        public int compareTo(Object o){
            ReversedRead other = (ReversedRead)o;

            if(coord != other.coord)
                return coord - other.coord;

            if(ref != other.ref)
                return ref.compareTo(other.ref);

            return name.compareTo(other.name);
        }
    }

    // heavily inspired by TwoPassPairWriter from UMI-tools
    private static class Writer{
        private boolean paired;
        private SAMFileWriter writer;
        private File in;
        private String ref = null;
        private HashSet<ReversedRead> set;

        public Writer(File in, File out, SamReader r, boolean paired){
            if(paired){
                this.in = in;
                this.set = new HashSet<ReversedRead>();
            }

            this.writer = new SAMFileWriterFactory().makeSAMOrBAMWriter(r.getFileHeader(), false, out);
            this.paired = paired;
        }

        public void write(SAMRecord record){
            if(paired){ // must be forwards read
                String currRef = record.getReferenceName();

                if(ref == null)
                    ref = currRef;

                if(!ref.equals(currRef)){
                    writeReversed(false);
                    ref = currRef;
                }

                if(record.getReadPairedFlag()){
                    set.add(new ReversedRead(
                            record.getReadName(),
                            record.getMateReferenceName(),
                            record.getMateAlignmentStart()
                    ));
                }
            }

            writer.addAlignment(record);
        }

        public void close(){
            if(paired)
                writeReversed(true);

            writer.close();
        }

        private void writeReversed(boolean fullPass){
            if(ref == null)
                return;

            SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(in);
            SAMRecordIterator iter = null;

            if(fullPass)
                iter = reader.iterator();
            else
                iter = reader.query(ref, 0, 0, true);

            while(iter.hasNext()){
                SAMRecord record = iter.next();

                if(!record.getReadUnmappedFlag()
                        && record.getReadPairedFlag()
                        && record.getSecondOfPairFlag()
                        && !record.getMateUnmappedFlag()){
                    ReversedRead read = new ReversedRead(
                            record.getReadName(),
                            record.getReferenceName(),
                            record.getAlignmentStart()
                    );

                    if(set.contains(read)){
                        writer.addAlignment(record);
                        set.remove(read);
                    }
                }
            }

            try{
                reader.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private static class AlignReads{
        public int latest;
        public Map<BitSet, ReadFreq> umiRead;

        public AlignReads(){
            this.latest = 0;
            this.umiRead = null;
        }
    }

    private static class PairedAlignment extends Alignment{
        private int tlen;

        public PairedAlignment(boolean strand, int coord, String ref, int tlen){
            super(strand, coord, ref);
            this.tlen = tlen;
        }

        @Override
        public boolean equals(Object o){
            if(!(o instanceof Alignment))
                return false;

            PairedAlignment a = (PairedAlignment)o;

            if(this == a)
                return true;

            if(tlen != a.tlen)
                return false;

            return super.equals(a);
        }

        @Override
        public int hashCode(){
            int hash = super.hashCode();
            hash = hash * HASH_CONST + tlen;
            return hash;
        }

        @Override
        public int compareTo(Object o){
            PairedAlignment other = (PairedAlignment)o;

            if(tlen != other.tlen)
                return Integer.compare(tlen, other.tlen);

            return super.compareTo(other);
        }
    }

    private static class Alignment implements Comparable{
        private boolean strand;
        private int coord;
        private String ref;

        public Alignment(boolean strand, int coord, String ref){
            this.strand = strand;
            this.coord = coord;
            this.ref = ref.intern();
        }

        public String getRef(){
            return ref;
        }

        @Override
        public boolean equals(Object o){
            if(!(o instanceof Alignment))
                return false;

            Alignment a = (Alignment)o;

            if(this == a)
                return true;

            if(strand != a.strand)
                return false;

            if(coord != a.coord)
                return false;

            if(ref != a.ref) // can directly compare interned strings
                return false;

            return true;
        }

        @Override
        public int hashCode(){
            int hash = strand ? 1231 : 1237;
            hash = hash * HASH_CONST + coord;
            hash = hash * HASH_CONST + ref.hashCode();
            return hash;
        }

        @Override
        public int compareTo(Object o){
            Alignment other = (Alignment)o;

            if(strand != other.strand)
                return Boolean.compare(strand, other.strand);

            if(coord != other.coord)
                return coord - other.coord;

            return ref.compareTo(other.ref);
        }
    }
}
