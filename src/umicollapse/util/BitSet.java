package umicollapse.util;

import java.util.Arrays;

public class BitSet{
    private static final int CHUNK_SIZE = 64;

    private long[] bits;
    private long[] nBits;
    private boolean recalcHash;
    private int hash;

    public BitSet(int length){
        this.bits = new long[length / CHUNK_SIZE + (length % CHUNK_SIZE == 0 ? 0 : 1)];
        this.recalcHash = true;
    }

    private BitSet(long[] bits){
        this.bits = bits;
        this.recalcHash = true;
    }

    private BitSet(long[] bits, int hash){
        this.bits = bits;
        this.recalcHash = false;
        this.hash = hash;
    }

    public boolean get(int idx){
        return (bits[idx / CHUNK_SIZE] & (1L << (idx % CHUNK_SIZE))) != 0L;
    }

    public void set(int idx, boolean bit){
        recalcHash = true;
        int i = idx / CHUNK_SIZE;
        int j = idx % CHUNK_SIZE;
        bits[i] = bit ? (bits[i] | (1L << j)) : (bits[i] & ~(1L << j));
    }

    public void setNBit(int idx, boolean bit){
        if(nBits == null)
            nBits = new long[bits.length];

        int i = idx / CHUNK_SIZE;
        int j = idx % CHUNK_SIZE;
        nBits[i] = bit ? (nBits[i] | (1L << j)) : (nBits[i] & ~(1L << j));
    }

    public int bitCountXOR(BitSet o){
        int res = 0;

        for(int i = 0; i < bits.length; i++){
            long xor = (nBits == null ? 0L : nBits[i]) ^ (o.nBits == null ? 0L : o.nBits[i]);
            res += Long.bitCount(xor | (bits[i] ^ o.bits[i])) - Long.bitCount(xor) / Read.ENCODING_LENGTH;
        }

        return res;
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof BitSet))
            return false;

        BitSet o = (BitSet)obj;

        if(this == o)
            return true;

        if(bits.length != o.bits.length)
            return false;

        for(int i = 0; i < bits.length; i++){
            if(bits[i] != o.bits[i])
                return false;
        }

        return true;
    }

    public BitSet clone(){
        if(recalcHash)
            return new BitSet(Arrays.copyOf(bits, bits.length));
        else
            return new BitSet(Arrays.copyOf(bits, bits.length), hash);
    }

    @Override
    public int hashCode(){
        if(recalcHash){
            long h = 1234L; // same as Java's built-in BitSet hash function

            for(int i = bits.length; --i >= 0;)
                h ^= bits[i] * (i + 1L);

            hash = (int)((h >> 32) ^ h);
            recalcHash = false;
        }

        return hash;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder();

        for(int i = 0; i < bits.length; i++){
            String s = Long.toBinaryString(bits[i]);
            res.append(reverse(s));
            res.append(make('0', CHUNK_SIZE - s.length()));
        }

        return res.toString();
    }

    private String make(char c, int n){
        char[] res = new char[n];

        for(int i = 0; i < n; i++)
            res[i] = c;

        return new String(res);
    }

    private String reverse(String s){
        char[] res = new char[s.length()];

        for(int i = 0; i < s.length(); i++)
            res[i] = s.charAt(s.length() - 1 - i);

        return new String(res);
    }
}
