package umicollapse.util;

public class ClusterTracker{
    private boolean track;

    private List<BitSet> temp;
    private int tempFreq;

    private Map<BitSet, Integer> toUniqueIdx;
    private List<ClusterStats> clusters;
    private int idx;

    public ClusterTracker(boolean track){
        this.track = track;

        this.temp = new ArrayList<BitSet>();
        this.tempFreq = 0;

        this.toUniqueIdx = new HashMap<BitSet, Integer>();
        this.clusters = new ArrayList<ClusterStats>();
        this.idx = 0;
    }

    public boolean shouldTrack(){
        return this.track;
    }

    public void addAll(Set<BitSet> s, Map<BitSet, ReadFreq> reads){
        if(this.track){
            this.temp.addAll(s);

            for(BitSet umi : s)
                this.tempFreq += reads.get(umi).readFreq.freq;
        }
    }

    public void track(BitSet unique){
        if(this.track){
            for(BitSet s : this.temp)
                this.toUniqueIdx.put(s, idx);

            clusters.add(new ClusterStats(unique, tempFreq));

            this.temp.clear();
            this.tempFreq = 0;
            this.idx++;
        }
    }

    private static class ClusterStats{
        BitSet umi;
        int freq;

        public ClusterStats(BitSet umi, int freq){
            this.umi = umi;
            this.freq = freq;
        }
    }
}
