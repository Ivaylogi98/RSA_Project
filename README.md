## RSA_Project
 Parallel computing Pi using Chudonovsky power series and binary splitting. **Apfloat** library used for high performance arbitrary precision arithmetics.
## Usage
 Run program with `java -jar <path to jar file>`
 Command line arguments:
  * -p <number> - precision; number of digits of Pi to compute
  * -t <number> - parallelism; number of threads to use
  * -g <number> = granularity
  * -q - quiet mode
  * -o <path_to_file> - output file
 ## Performance analysis
  Found in [Project.pdf](https://github.com/Ivaylogi98/RSA_Project/blob/master/Project.pdf) (page 8)
 ## Sources
  [Pi - Chudnovsky](https://www.craig-wood.com/nick/articles/pi-chudnovsky/) Craig Wood
  [Binary splitting method](http://numbers.computation.free.fr/Constants/Algorithms/splitting.html) Xavier Gourdon and Pascal Sebah
  [Fast multiprecision evaluation of series of rational numbers](https://www.ginac.de/CLN/binsplit.pdf)Bruno Haible and Thomas Papanikolaou
  [Computation of 2700 billion decimal digits of Pi using a Desktop Computer](https://pdfs.semanticscholar.org/6cf7/1234c8662100277b1057467d5917c5954f40.pdf) Fabrice Bellard
  [Java - Multithreading](https://www.tutorialspoint.com/java/java_multithreading.htm)
