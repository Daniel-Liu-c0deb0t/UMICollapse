package test;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;

import umicollapse.util.SAMRead;

import java.util.regex.Matcher;

import java.io.File;

public class AddUMITag{
    public static void main(String[] args) throws Exception{
        SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(new File(args[0]));
        SAMFileWriter writer = new SAMFileWriterFactory().makeSAMOrBAMWriter(reader.getFileHeader(), false, new File(args[1]));

        for(SAMRecord record : reader){
            Matcher m = SAMRead.umiPattern("_").matcher(record.getReadName());
            m.find();
            String umi = m.group(2);

            record.setAttribute("FZ", new int[]{1, 2, 3}); // dummy tag
            record.setAttribute("RX", umi);
            record.setReadName(m.replaceFirst("$1"));

            writer.addAlignment(record);
        }

        reader.close();
        writer.close();
    }
}
