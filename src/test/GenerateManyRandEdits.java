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

public class GenerateManyRandEdits{
    public static void main(String[] args) throws Exception{
        int k = 1;
        int iter = Integer.parseInt(args[2]);
        int dupIter = 20;
        Random rand = new Random(1234);

        SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(new File(args[0]));
        SAMFileWriter writer = new SAMFileWriterFactory().makeSAMOrBAMWriter(reader.getFileHeader(), false, new File(args[1]));

        for(SAMRecord record : reader){
            Matcher m = SAMRead.umiPattern("_").matcher(record.getReadName());
            m.find();
            int umiLength = m.group(2).length();

            for(int i = 0; i < iter; i++){
                String umi = TestUtils.randUMI(umiLength, rand);

                for(int j = 0; j < dupIter; j++){
                    SAMRecord dup = record.deepCopy();
                    dup.setReadName(m.replaceFirst("$1_" + TestUtils.randEdits(umi, k, rand) + "$3"));
                    writer.addAlignment(dup);
                }

                SAMRecord dup = record.deepCopy();
                dup.setReadName(m.replaceFirst("$1_" + umi + "$3"));
                writer.addAlignment(dup);
            }

            break;
        }

        reader.close();
        writer.close();
    }
}
