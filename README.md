# UMICollapse
Accelerating the deduplication and collapsing process for reads with Unique Molecular Identifiers (UMI). This tool implements many efficient algorithms for faster UMI deduplication. The preprint paper is available **[here](https://www.biorxiv.org/content/10.1101/648683v2)**. If you use this code, please cite

```
@article{liu2019algorithms,
  title={Algorithms for efficiently collapsing reads with Unique Molecular Identifiers},
  author={Liu, Daniel},
  journal={bioRxiv},
  year={2019},
  publisher={Cold Spring Harbor Laboratory}
}
```

## Installation
First, clone this repository:
```
git clone https://github.com/Daniel-Liu-c0deb0t/UMICollapse.git
cd UMICollapse
```
Then, install the dependencies, which are used for FASTQ/SAM/BAM input/output operations. Make sure you have Java 11.
```
mkdir lib
cd lib
curl -O http://central.maven.org/maven2/com/github/samtools/htsjdk/2.19.0/htsjdk-2.19.0.jar
curl -O http://central.maven.org/maven2/org/xerial/snappy/snappy-java/1.1.7.3/snappy-java-1.1.7.3.jar
cd ..
```
Now you have UMICollapse installed!

## Example Run
First, get some sample data from the UMI-tools repository. These aligned reads have their UMIs extracted and concatenated to their read headers. Make sure you have `samtools` installed to index the aligned BAM.
```
mkdir test
cd test
curl -O https://github.com/CGATOxford/UMI-tools/releases/download/1.0.0/example.bam
samtools index example.bam
cd ..
```
Finally, `test/example.bam` can be deduplicated.
```
./umicollapse bam -i test/example.bam -o test/dedup_example.bam
```
The UMI length will be autodetected, and the output `test/dedup_example.bam` should only contain reads that have a unique UMI.

## Command-Line Arguments
### Mode (appears before commands)
* `sam` or `bam`: the input is an aligned SAM/BAM file with the UMIs in the read headers. This separately deduplicates each alignment coordinate.
* `fastq`: the input is a FASTQ file. This deduplicates the entire FASTQ file based on each entire read sequence.

### Commands
* `-i`: input file. Required.
* `-o`: output file. Required.
* `-k`: number of substitution edits to allow. Default: 1.
* `-u`: the UMI length. Default: autodetect.
* `-p`: threshold percentage for identifying adjacent UMIs in the directional algorithm. Default: 0.5.
* `-t`: parallelize the deduplication of each separate alignment position. Default: false.
* `-T`: parallelize the deduplication of one single alignment position. The data structure can only be `naive`, `bktree`, and `fenwickbktree`. Default: false.
* `--umi-sep`: separator string between the UMI and the rest of the read header. Default: `_`.
* `--algo`: deduplication algorithm. Either `cc` for connected components, `adj` for adjacency, or `dir` for directional. Default: `dir`.
* `--merge`: method for identifying which UMI to keep out of every two UMIs. Either `any`, `avgqual`, or `mapqual`. Default: `mapqual`.
* `--data`: data structure used in deduplication. Either `naive`, `combo`, `ngram`, `delete`, `trie`, `bktree`, `sortbktree`, `ngrambktree`, `sortngrambktree`, or `fenwickbktree`. Default: `ngrambktree`.

## Java Virtual Machine Memory
If you need more memory to process larger datasets, then modify the `umicollapse` file. `-Xms` represents the initial heap size, `-Xmx` represents the max heap size, and `-Xss` represents the stack size.
