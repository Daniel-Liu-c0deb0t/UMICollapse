package test;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.BitSet;

public class TestUtils{
    public static boolean listMatches(List<BitSet> a, List<BitSet> b){
        if(a.size() != b.size())
            return false;

        Set<BitSet> s = new HashSet<>(a);
        s.removeAll(b);
        return s.isEmpty();
    }
}
