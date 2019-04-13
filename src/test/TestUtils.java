package test;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import umicollapse.util.BitSet;
import umicollapse.util.Utils;
import umicollapse.util.Read;
import umicollapse.util.ReadFreq;

public class TestUtils{
    public static boolean setMatches(Set<BitSet> a, Set<BitSet> b){
        if(a.size() != b.size())
            return false;

        return a.equals(b);
    }

    public static Map<BitSet, ReadFreq> generateData(int numRand, int numDup, int umiLength, int k, float percentage, Random rand){
        float maxFreq = 1e7f;
        Map<BitSet, ReadFreq> res = new HashMap<>();

        for(int i = 0; i < numRand; i++){
            String umi = randUMI(umiLength, rand);
            // freq within top 30% of maxFreq
            ReadFreq r = new ReadFreq(null, (int)(maxFreq * (1.0f - 0.3f * rand.nextFloat())));
            res.put(Utils.toBitSet(umi), r);

            for(int j = 0; j < numDup; j++){
                // freq within bottom (70% * percentage) of maxFreq
                r = new ReadFreq(null, (int)(maxFreq * (0.7f * rand.nextFloat() * percentage)));
                res.put(Utils.toBitSet(randEdits(umi, k, rand)), r);
            }
        }

        return res;
    }

    public static String randUMI(int n, Random rand){
        char[] res = new char[n];

        for(int i = 0; i < n; i++)
            res[i] = Read.ALPHABET[rand.nextInt(Read.ALPHABET.length)];

        return new String(res);
    }

    public static String randEdits(String s, int k, Random rand){
        char[] res = s.toCharArray();
        int edits = rand.nextInt(k) + 1; // at least 1 edit

        for(int i = 0; i < edits; i++){
            int idx = rand.nextInt(s.length());
            res[idx] = pick(Read.ALPHABET, res[idx], rand);
        }

        return new String(res);
    }

    public static char pick(char[] alphabet, char exclude, Random rand){
        int idx = rand.nextInt(alphabet.length - 1) + 1;

        for(int i = 0; i < alphabet.length; i++){
            if(alphabet[i] != exclude)
                idx--;

            if(idx == 0)
                return alphabet[i];
        }

        return '?'; // impossible
    }
}
