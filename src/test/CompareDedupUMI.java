package test;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.SamReaderFactory;

import umicollapse.util.SAMRead;

import java.util.regex.Matcher;

import java.util.Set;
import java.util.HashSet;

import java.io.File;

public class CompareDedupUMI{
    public static void main(String[] args) throws Exception{
        SamReader r1 = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(new File(args[0]));

        Set<String> s = new HashSet<>();

        for(SAMRecord record : r1){
            Matcher m = SAMRead.umiPattern("_").matcher(record.getReadName());
            m.find();
            String umi = m.group(2);
            int start = record.getReadNegativeStrandFlag() ? record.getUnclippedEnd() : record.getUnclippedStart();
            s.add(umi + "_" + record.getReadNegativeStrandFlag() + "_" + start + "_" + record.getReferenceName());
        }

        r1.close();

        SamReader r2 = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(new File(args[1]));
        int wrong = 0;

        for(SAMRecord record : r2){
            Matcher m = SAMRead.umiPattern("_").matcher(record.getReadName());
            m.find();
            int start = record.getReadNegativeStrandFlag() ? record.getUnclippedEnd() : record.getUnclippedStart();
            String umi = m.group(2);
            umi += "_" + record.getReadNegativeStrandFlag() + "_" + start + "_" + record.getReferenceName();

            if(s.contains(umi)){
                s.remove(umi);
            }else{
                System.out.println("> " + umi);
                wrong++;
            }
        }

        wrong += s.size();

        r2.close();

        for(String key : s)
            System.out.println("< " + key);

        System.out.println("Wrong\t" + wrong);
    }
}
