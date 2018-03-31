# get the correct classpath
export CLASSPATH=.:./jars/weka.jar:

# compile sources and
javac change/clean/*.java

# run sample code
java change.clean.DefectPrediction
