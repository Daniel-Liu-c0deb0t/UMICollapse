package util;

import htsjdk.samtools.SAMRecord;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SAMRead extends Read{
    private static final Pattern umiPattern = Pattern.compile("^.*_([ATCG]+).*$", Pattern.CASE_INSENSITIVE);

    private SAMRecord record;
    private int avgQual;

    public SAMRead(SAMRecord record){
        this.record = record;

        float avg = 0.0f;

        for(byte b : record.getBaseQualities())
            avg += (float)b / record.getReadLength();

        this.avgQual = (int)avg;
    }

    @Override
    public BitSet getUMI(){
        Matcher m = umiPattern.matcher(record.getReadName());
        m.find();
        String umi = m.group(1);
        return Utils.toBitSet(umi.toUpperCase());
    }

    @Override
    public int getAvgQual(){
        return avgQual;
    }

    public int getMapQual(){
        return record.getMappingQuality();
    }

    public SAMRecord toSAMRecord(){
        return record;
    }
}
