package data;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.BitSet;

import static util.Utils.umiDist;

public class BKTree implements DataStructure{
    private int umiLength;
    private Node root;

    public BKTree(Set<BitSet> s, int umiLength){
        this.umiLength = umiLength;

        boolean first = true;

        for(BitSet umi : s){
            if(first){
                root = new Node(umi);
                first = false;
            }else{
                insert(umi);
            }
        }
    }

    @Override
    public List<BitSet> removeNear(BitSet umi, int k){
        List<BitSet> res = new ArrayList<>();
        recursiveRemoveNear(umi, root, k, res);
        return res;
    }

    private void recursiveRemoveNear(BitSet umi, Node curr, int k, List<BitSet> res){
        int dist = umiDist(umi, curr.getUMI());

        if(dist <= k && curr.exists()){
            res.add(curr.getUMI());
            curr.setExists(false);
        }

        int lo = Math.max(dist - k, 0);
        int hi = Math.min(dist + k, umiLength);

        for(int i = lo; i <= hi; i++){
            if(!curr.subtreeExists(i))
                continue;

            recursiveRemoveNear(umi, curr.get(i), k, res);
        }

        if(!curr.childSubtreeExists())
            curr.setSubtreeExists(false);
    }

    private void insert(BitSet umi){
        Node curr = root;
        int dist;

        do{
            dist = umiDist(umi, curr.getUMI());
        }while((curr = curr.initNode(dist, umi, umiLength)) != null);
    }

    private static class Node{
        private BitSet umi;
        private boolean exists, subtreeExists;
        private Node[] c;

        Node(BitSet umi){
            this.c = null;
            this.umi = umi;
            this.exists = true;
            this.subtreeExists = true;
        }

        Node initNode(int k, BitSet umi, int umiLength){
            if(c == null)
                c = new Node[umiLength + 1];

            if(c[k] == null){
                c[k] = new Node(umi);
                return null;
            }

            return c[k];
        }

        BitSet getUMI(){
            return umi;
        }

        boolean exists(){
            return exists;
        }

        void setExists(boolean exists){
            this.exists = exists;
        }

        boolean childSubtreeExists(){
            if(c == null)
                return false;

            for(int i = 0; i < c.length; i++){
                if(c[i] != null && c[i].subtreeExists)
                    return true;
            }

            return false;
        }

        void setSubtreeExists(boolean subtreeExists){
            this.subtreeExists = subtreeExists;
        }

        boolean subtreeExists(int k){
            return c != null && c[k] != null && c[k].subtreeExists;
        }

        Node get(int k){
            return c[k];
        }
    }
}
