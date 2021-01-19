# UMICollapse
Accelerating the deduplication and collapsing process for reads with Unique Molecular Identifiers (UMI).

UMIs are a popular way to identify duplicate DNA/RNA reads caused by PCR amplification. This requires software for collapsing duplicate reads with the same UMI, while accounting for sequencing/PCR errors. This tool implements many efficient algorithms for orders-of-magnitude faster UMI deduplication than previous tools (UMI-tools, etc.), while maintaining similar functionality. This is achieved by using faster data structures with n-grams and BK-trees, along other techniques that are carefully implemented to scale well to larger datasets and longer UMIs.

The preprint paper is available **[here](https://www.biorxiv.org/content/10.1101/648683v2)** and it has been published in PeerJ. If you use this code, please cite

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
curl -O -L https://repo1.maven.org/maven2/com/github/samtools/htsjdk/2.19.0/htsjdk-2.19.0.jar
curl -O -L https://repo1.maven.org/maven2/org/xerial/snappy/snappy-java/1.1.7.3/snappy-java-1.1.7.3.jar
cd ..
```
Now you have UMICollapse installed!

## Example Run
First, get some sample data from the UMI-tools repository. These aligned reads have their UMIs extracted and concatenated to their read headers (You can do this with the `extract` tool in UMI-tools). Make sure you have `samtools` installed to index the BAM file.
```
mkdir test
cd test
curl -O -L https://github.com/CGATOxford/UMI-tools/releases/download/1.0.0/example.bam
samtools index example.bam
cd ..
```
Finally, `test/example.bam` can be deduplicated.
```
./umicollapse bam -i test/example.bam -o test/dedup_example.bam
```
The UMI length will be autodetected, and the output `test/dedup_example.bam` should only contain reads that have a unique UMI. Unmapped reads are removed.

Here is a hypothetical example with paired-end reads:
```
./umicollapse bam -i paired_example.bam -o dedup_paired_example.bam --umi-sep : --paired --two-pass
```
This should be equivalent to the following with [UMI-tools](https://github.com/CGATOxford/UMI-tools):
```
umi_tools dedup -I paired_example.bam -S dedup_paired_example.bam --umi-separator=: --paired
```

By default, clusters/groups of reads with the same UMI are collapsed into one consensus read. It is possible to only mark duplicate reads with the `--tag` option. Note that only the forwards reads are tagged in paired-end mode. This also currently does not work with `--two-pass`.

The examples above are based on the workflow where reads are aligned to produce SAM/BAM files before collapsing them based on their UMIs at each unique alignment coordinate. It is also possible to collapse reads based on their sequences directly, without aligning. This may be preferable or faster in some workflows. This can be done by specifying the `fastq` option instead of `bam` and providing an input FASTQ file:
```
./umicollapse fastq -i input.fastq -o output.fastq
```

It is important to note that UMIs are first collapsed by identity (exact same UMIs), and then grouped/clustered while allowing some errors/mismatches.

## Building
Run
```
./build.sh
```
to build the executable `.jar` file.

## Testing
Running basic tests after the `.jar` file is built:
```
./test.sh
```
There are also some small scripts for testing and debugging. For example, comparing two files to check if the UMIs are the same can be done with:
```
./run.sh test.CompareDedupUMI test/dedup_example_1.bam test/dedup_example_2.bam
```
or running benchmarks:
```
./run.sh test.BenchmarkTime 10000 10 1 ngrambktree
```

## Command-Line Arguments
### Mode (appears before commands)
* `sam` or `bam`: the input is an aligned SAM/BAM file with the UMIs in the read headers. This separately deduplicates each alignment coordinate.
* `fastq`: the input is a FASTQ file. This deduplicates the entire FASTQ file based on each entire read sequence. Note that the "UMI" would be the entire read sequence.

### Commands
* `-i`: input file. Required.
* `-o`: output file. Required.
* `-k`: number of substitution edits to allow. Default: 1.
* `-u`: the UMI length. If set to a length in `fastq` mode, then trims the prefix of each read (the UMI is still the entire read sequence though). Default: autodetect.
* `-p`: threshold percentage for identifying adjacent UMIs in the directional algorithm. Default: 0.5.
* `-t`: parallelize the deduplication of each separate alignment position. Using this is discouraged as it is lacking many features. Default: false.
* `-T`: parallelize the deduplication of one single alignment position. The data structure can only be `naive`, `bktree`, and `fenwickbktree`. Using this is discouraged as it is lacking many features. Default: false.
* `--umi-sep`: separator string between the UMI and the rest of the read header. Default: `_`.
* `--algo`: deduplication algorithm. Either `cc` for connected components, `adj` for adjacency, or `dir` for directional. Default: `dir`.
* `--merge`: method for identifying which UMI to keep out of every two UMIs. Either `any`, `avgqual`, or `mapqual`. Default: `mapqual` for SAM/BAM mode, `avgqual` for FASTQ mode.
* `--data`: data structure used in deduplication. Either `naive`, `combo`, `ngram`, `delete`, `trie`, `bktree`, `sortbktree`, `ngrambktree`, `sortngrambktree`, or `fenwickbktree`. Default: `ngrambktree`.
* `--two-pass`: use a separate two-pass algorithm for SAM/BAM deduplication. This may be slightly slower, but it should use much less memory if the reads are approximately sorted by alignment coordinate. Default: false.
* `--paired`: use paired-end mode, which deduplicates pairs of reads from a SAM/BAM file. This is very memory intensive, and the input SAM/BAM files should be sorted. Default: false (single-end).
* `--remove-unpaired`: remove unpaired reads during paired-end mode. Default: false.
* `--remove-chimeric`: remove chimeric reads (pairs map to different references) during paired-end mode. Default: false.
* `--tag`: tag reads that belong to the same group without removing them. In `fastq` mode, this will append `cluster_id=[unique ID for all reads of the same cluster]` to the header of every read. `cluster_size=[number of reads in the cluster]` will only be appended to the header of a consensus read for an entire group/cluster. `same_umi=[number of reads with the same UMI` will be appended to the header of the "best" read of a group of reads with the exact same UMI (not allowing mismatches). In `sam`/`bam` mode, then all reads but the consensus reads will be marked with the duplicate flag. The `IM` attribute will be set with the `cluster_id` and the `RX` attribute will be set with the UMI of the consensus read. If applicable, the `cs` attribute is set with the `cluster_size`, and the `su` attribute is set with the `same_umi` count. For paired-end reads, only the forwards reads are tagged. This does not work with the `--two-pass` feature.

## Java Virtual Machine Memory
If you need more memory to process larger datasets, then modify the `umicollapse` file. `-Xms` represents the initial heap size, `-Xmx` represents the max heap size, and `-Xss` represents the stack size. If you do not know how much memory is needed, it may be a good idea to set a small initial heap size, and a very large max heap size, so the heap can grow when necessary. If memory usage is still is an issue, use the `--two-pass` option to save memory when the reads are approximately sorted (this is not a strict requirement, its just that when reads with the same alignment coordinate are close together in the file, they do not have to be kept in memory for very long).

## Issues
Please open an issue if you have any questions/bugs/suggestions!
