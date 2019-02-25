package data;

import fastq.Read;
import static util.Utils.umiDist;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class Naive implements DataStructure{
    private Set<Read> s;

    public Naive(Set<Read> s){
        this.s = s;
    }

    @Override
    public List<Read> removeNear(Read r, int k){
        List<Read> res = new ArrayList<>();

        for(Read o : s){
            if(umiDist(r, o) <= k){
                res.add(o);
            }
        }

        s.removeAll(res);

        return res;
    }
}
