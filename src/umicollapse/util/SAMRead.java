package umicollapse.util;

import htsjdk.samtools.SAMRecord;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SAMRead extends Read{
    private static Pattern defaultUMIPattern;
    private SAMRecord record;
    private int avgQual;

    public SAMRead(SAMRecord record){
        this.record = record;

        float avg = 0.0f;

        for(byte b : record.getBaseQualities())
            avg += b;

        this.avgQual = (int)(avg / record.getReadLength());
    }

    public static void setDefaultUMIPattern(String sep){
        defaultUMIPattern = umiPattern(sep);
    }

    public static Pattern umiPattern(String sep){
        return Pattern.compile("^(.*)" + sep + "([ATCGN]+)(.*?)$", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public BitSet getUMI(){
        Matcher m = defaultUMIPattern.matcher(record.getReadName());
        m.find();
        String umi = m.group(2);
        return Utils.toBitSet(umi.toUpperCase());
    }

    @Override
    public int getUMILength(){
        Matcher m = defaultUMIPattern.matcher(record.getReadName());
        m.find();
        return m.group(2).length();
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
