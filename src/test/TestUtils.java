package test;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.BitSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import util.Utils;
import util.Read;

public class TestUtils{
    public static boolean listMatches(List<BitSet> a, List<BitSet> b){
        if(a.size() != b.size())
            return false;

        Set<BitSet> s = new HashSet<>(a);
        s.removeAll(b);
        return s.isEmpty();
    }

    public static Map<BitSet, Integer> generateData(int numRand, int numDup, int umiLength, int k, Random rand){
        Map<BitSet, Integer> res = new HashMap<>();

        for(int i = 0; i < numRand; i++){
            String umi = randUMI(umiLength, rand);

            for(int j = 0; j < numDup; j++)
                res.put(Utils.toBitSet(randEdits(umi, k, rand)), 0);
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
        int edits = rand.nextInt(k + 1);

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
