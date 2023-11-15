# UIRMiner
This repo hosts the code for paper **"Discovering Utility-driven Interval Rules"**, which was submitted to the IEEE TKDE Journal.

## Requirements
Java programming language.

## Running the program
A simple way is to run the java test file.

## Benchmarks
- The data format used is the same as in the file CONTEXT.txt

## Introduction
For artificial intelligence, high-utility sequential rule mining (HUSRM) is a knowledge discovery method that can reveal the associations between events in the sequences. Recently, abundant methods have been proposed to discover high-utility sequence rules. However, the existing methods are all related to point-based sequences. Interval events that persist for some time are common. Traditional interval-event sequence knowledge discovery tasks mainly focus on pattern discovery, but patterns cannot reveal the correlation between interval events well. Moreover, the existing HUSRM algorithms cannot be directly applied to interval-event sequences since the relation in interval-event sequences is much more intricate than those in point-based sequences. In this work, we propose a utility-driven interval rule mining (UIRMiner) algorithm that can extract all utility-driven interval rules (UIRs) from the interval-event sequence database to solve the problem. In UIRMiner, we first introduce a numeric encoding relation representation, which can save much time on relation computation and storage on relation representation. Furthermore, to shrink the search space, we also propose a utility complement pruning strategy, which incorporates the utility upper bound with the relation. Finally, plentiful experiments implemented on both real-world and synthetic datasets verify that UIRMiner is an effective and efficient algorithm.
