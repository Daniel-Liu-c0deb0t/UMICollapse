package fastq;

public class Read{
    public static final int ENCODING_DIST = 2;

    public String desc;
    public BitSet umi;
    public BitSet read;
    public byte[] qual;

    public Read(String desc, String read, String qual){

    }

    @Override
    public int hashCode(){
        return umi.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Read))
            return false;

        Read r = (Read)o;
        return umi.equals(r.umi);
    }
}
