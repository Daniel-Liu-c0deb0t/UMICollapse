package util;

import java.util.Arrays;

public class BitSet{
    private static final int CHUNK_SIZE = 64;

    private long[] bits;
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

    public int bitCountXor(BitSet o){
        int length = Math.min(bits.length, o.bits.length);
        int res = 0;

        for(int i = 0; i < length; i++)
            res += Long.bitCount(bits[i] ^ o.bits[i]);

        for(int i = length; i < bits.length; i++)
            res += Long.bitCount(bits[i]);

        for(int i = length; i < o.bits.length; i++)
            res += Long.bitCount(o.bits[i]);

        return res;
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof BitSet))
            return false;

        BitSet o = (BitSet)obj;

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
}
