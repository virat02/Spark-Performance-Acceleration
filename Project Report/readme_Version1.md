** **
# Big data platform (Spark) performance acceleration

## 1. Vision and Goals Of The Project: 

Shuffling could become the scaling bottleneck when running many small tasks in multistage data analysis jobs. In certain circumstances, data is too large to fit into memory, intermediate data has to keep on disks, which makes a large amount of small random I/O requests significantly slow down the performance of spark.


Optimize the partitioning and shuffle algorithms in Spark, to perform more efficient I/O and shuffling. There is believed to be a significant opportunity for improvement in changing the I/O patterns so that large data files are read more efficiently from disk.

* Research the current spark architecture/workflow
* Provide an analysis of an existing project highlighting areas of possible improvements
* Provide possible Spark performance acceleration strategies by optimizing the shuffle and partitioning algorithms.
* Implement a new shuffle/merge manager, and insert it into the Spark software stack.

## 2. Users/Personas Of The Project
Engineers or Data scientists working on Big Data projects that use batch processing and/or real-time processing using Spark. 

## 3. Scope and Features Of The Project:
### Scope:
* Researching the existing solutions/papers to identify the best approach.
* Find a dataset and upload it to AWS S3.
* Run an existing spark project on the AWS EC2 cluster and analyze run times with different sizes of the dataset.
* Use spark history server and other tools to analyze stages, I/O operations and amount of shuffled data, to analyze what can be the areas for improvement.
* Provide improved speed-up metrics.


## 4. Solution Concept

![image alt text](sparkArch.png)

More research work needs to be done to correctly identify possible solutions.

 Here are some references we are using at the moment to work on the solution:
https://haoyuzhang.org/publications/riffle-eurosys18.pdf
https://databricks.com/session/sos-optimizing-shuffle-i-o
https://pdfs.semanticscholar.org/d746/505bad055c357fa50d394d15eb380a3f1ad3.pdf
http://bigdatatn.blogspot.com/2017/05/spark-performance-optimization-shuffle.html
https://ieeexplore.ieee.org/document/8125977
https://www.ijert.org/research/shuffle-performance-in-apache-spark-IJERTV4IS020241.pdf
http://iqua.ece.toronto.edu/papers/sliu-icdcs17.pdf


## 5. Acceptance criteria
Improvements during the shuffling phase:
* Fewer I/O requests in general
* Less disk I/O during shuffling phase
* Less running time

**Stretch Goals:**
* Test the service/plug-in on different categories of Spark applications and deployment environment.
* Design some simple strategies that decide when to merge shuffling overhead when not to.
* Provide explanations of why there is no general method which could apply to all datasets
* Implement improvements on data partitioning phase 

## 6. Release Planning
### Tasks: ###

* Setting up the Spark Environment (latest version)
* Learn more about Spark and Hadoop
* Finding an appropriate dataset/project to perform analysis
* Run Spark applications and profile Spark performance on the dataset identified in step 3
* Evaluate possible benchmarks:
  * Total time of Spark application (e.g., Sorting, Spark Query)
  * IOPs on hard drives
* Research: Where to modify/extend 
* Implement a new shuffle/merge manager, and insert it into the Spark software stack.
* Profile the after enhancement Spark performance

### Timeline: ###

**16th September - 29th September:** 

Setup environment, find two or three proper datasets, detailed research on possible solutions, run some sample queries/jobs

**30th September - 13th October:**

Find the bottleneck of queries/jobs, hard-code some plugins to test if directions are correct, design algorithm/strategies

**14th October - 27th October:**

Work on the backlog, Test the correctness of algorithm

**28th October - 10th November:**

Improvements/ Fine tune of algorithm/strategies

**11th November - 24th November:**

Continue working on possible improvements

**25th November - 8th December:**

Make a final presentation, Focus on stretch goals

** **
