UMBC_STS
========
UMBC_STS is used for computing text semantic similarity. Necessary setting is given below. 


Step 1: Setting the class path to the folder where source packages are stored in the computer

Example command for exporting ClassPath:

	export CLASSPATH=$CLASSPATH:/Users/Mahbub/Desktop/GitHub/UMBC_STS/src/:/Users/Mahbub/Desktop/GitHub/UMBC_STS/lib/ 


Step 2: Writing the config file. This file has two lines. Each of which are the locations of models and testbeds. First line is the path in your computer where model folder is located and second line is the path in your computer where testbed folder is located. Model and testbed folders contain model files and testbed files respectively. They can be in same folder or in different folders. 

Example

/Users/Mahbub/Desktop/Research/Data
/Users/Mahbub/Desktop/Research/Data



Step 3: Compiling the code with encoding

Command to compile:

	javac -O -encoding UTF8 -d .  *.java



Step 4: Running some sample code 

Command to run sample code:

	java -cp . edu.umbc.dbpedia.model.STS_Example

If you get heap size exception then increase heap memory using -Xmx flag. Sample command with flag for heap memory is 

	java -Xmx2g -cp . edu.umbc.dbpedia.model.STS_Example




—- The End —- 