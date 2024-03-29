//For SE465 Winter 2018
//All Rights Reserved

package change.clean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.ADTree;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;

public class DefectPrediction {
  public String TrainingPath;
  public String TestingPath;
  public String project;
  public String resultPath;
  public String MappingPath;

  /**
   * used for F1
   */
  public double TruthPositive = 0;
  public double TrueNegative = 0;
  public double FalseNegative = 0;
  public double FalsePositive = 0;
  public double TestingInstance = 0;

  public  Instances TrainingData;
  public  Instances TestingData ;

  public ArrayList<InstancesResult> instances = new ArrayList<InstancesResult>();// results
  public Map<String, String> mappings = new HashMap<String, String>();

  private FileWriter writer;
  private FileReader fr;
  private BufferedReader br;


  public DefectPrediction(String project, String trainingPath, String testingPath, String resultPath) {
    super();
    TrainingPath = trainingPath;
    TestingPath = testingPath;
    this.project = project;
    this.resultPath = resultPath;
  }

  /**
   * @desc loading training and testing data
   * @throws Exception
   */
  public void loadData() throws Exception {
    ArffLoader loader = new ArffLoader();
    loader.setSource(new File(this.TrainingPath));
    TrainingData = loader.getDataSet();
    TrainingData.setClassIndex(TrainingData.numAttributes() - 1);

    loader = new ArffLoader();
    loader.setSource(new File(this.TestingPath));
    TestingData = loader.getDataSet();
    TestingData.setClassIndex(TestingData.numAttributes() - 1);
  }

  /**
   * @desc: undersample
   * @param type
   * @throws Exception
   */
  public  void undersampleForTraining() throws Exception{
    TrainingData = sample(TrainingData);
  }

  private  Instances sample(Instances data) throws Exception{
    SpreadSubsample sampler = new SpreadSubsample();
    String Fliteroptions="-M 1.0 -X 0.0 -S 1";
    sampler.setOptions(weka.core.Utils.splitOptions(Fliteroptions));
    sampler.setInputFormat(data);
    Instances newData = Filter.useFilter(data, sampler);
    return newData;
  }

  public  void ADTree() throws Exception{
     String[] option = new String[4];
     //-3(all), -2(weight), -1(z_pure), >=0 seed for random walk
     option[0] = "-B";
     option[1] = "20";
     option[2] = "-E";
     option[3] = "-3";
     ADTree tree = new ADTree();

     tree.setOptions(option);

     Classifier cls = tree;
     cls.buildClassifier(TrainingData);
     Evaluation eval = new Evaluation(TrainingData);
     eval.evaluateModel(cls, TestingData);

     TruthPositive = eval.numTruePositives(1);
     TrueNegative = eval.numTrueNegatives(1);
     FalseNegative = eval.numFalseNegatives(1);
     FalsePositive = eval.numFalsePositives(1);
     TestingInstance = eval.numInstances();

     System.out.println("ADTree(): parameters: "+option);
     System.out.println("prediction precision: "+eval.precision(1));
     System.out.println("prediction recall: " +eval.recall(1));
     System.out.println("prediction fMeasure: "+eval.fMeasure(1));
     System.out.println("prediction AUC: "+eval.areaUnderROC(1));
     System.out.println(cls.toString());
  }

  //write to file
  public void write2File() throws Exception{
     writer = new FileWriter(this.resultPath, true);
     PrintWriter out = new PrintWriter(writer);
     out.println(this.project+","+this.TruthPositive+","+
                 this.TrueNegative+","+this.FalsePositive+","+this.FalseNegative);
     out.flush();
  }

  public ArrayList<InstancesResult> getInstances() {
    return instances;
  }

  public static void main(String[]args) throws Exception{
    Map  <String, Integer> project  = new HashMap<String, Integer>();
    project.put("jackrabbit", 10);
    project.put("lucene", 8);
    project.put("jdt", 6);
    project.put("xorg", 6);
    project.put("postgresql", 6);

    for (Map.Entry<String, Integer> entry:project.entrySet()) {
      String projectname = entry.getKey();
      int num = entry.getValue();
      ArrayList<Integer> results = new ArrayList<Integer>();

      for (int fold=0; fold<num; fold++) {
        String TrainingPath = "/home/song/SE465-18/data/"+projectname+"/"+fold+"/train.arff";
        String TestingPath = "/home/song/SE465-18/data/"+projectname+"/"+fold+"/test.arff";
        String resultPath = "/home/song/SE465-18/data/"+projectname+"/results-"+fold+".txt";

        DefectPrediction adtree = new DefectPrediction(projectname, TrainingPath, TestingPath, resultPath);
        adtree.loadData();
        adtree.undersampleForTraining();
        adtree.ADTree();

        adtree.write2File();
      }
    }
  }
}
