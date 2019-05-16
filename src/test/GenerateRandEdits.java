package test;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;

import umicollapse.util.SAMRead;

import java.util.regex.Matcher;

import java.util.Random;

import java.io.File;

public class GenerateRandEdits{
    public static void main(String[] args) throws Exception{
        int k = 1;
        int iter = Integer.parseInt(args[2]);
        Random rand = new Random(1234);

        SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(new File(args[0]));
        SAMFileWriter writer = new SAMFileWriterFactory().makeSAMOrBAMWriter(reader.getFileHeader(), false, new File(args[1]));

        for(SAMRecord record : reader){
            Matcher m = SAMRead.umiPattern("_").matcher(record.getReadName());
            m.find();
            String umi = m.group(2);

            for(int i = 0; i < iter; i++){
                SAMRecord dup = record.deepCopy();
                dup.setReadName(m.replaceFirst("$1_" + TestUtils.randEdits(umi, k, rand) + "$3"));
                writer.addAlignment(dup);
            }

            writer.addAlignment(record);
        }

        reader.close();
        writer.close();
    }
}
