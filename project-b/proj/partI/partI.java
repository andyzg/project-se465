package proj.partI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.ADTree;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;

public class DefectPrediction {
  public String TrainingPath;
  public String TestingPath;
  public String project;

  private Evaluation eval;

  public Instances TrainingData;
  public Instances TestingData ;

  public DefectPrediction(String project, String trainingPath, String testingPath) {
    super();
    TrainingPath = trainingPath;
    TestingPath = testingPath;
    this.project = project;
  }

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

  public void run() throws Exception{
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
    eval = new Evaluation(TrainingData);
    eval.evaluateModel(cls, TestingData);
  }

  public getPrecision() {
    return eval.precision(1);
  }

  public getRecall() {
    return eval.recall(1);
  }

  public getF1() {
    return eval.fMeasure(1);
  }

  public static void main(String[] args) throws Exception{
    Map  <String, Integer> project  = new HashMap<String, Integer>();
    project.put("jackrabbit", 10);
    project.put("lucene", 8);
    project.put("jdt", 6);
    project.put("xorg", 6);
    project.put("postgresql", 6);

    for(Map.Entry<String, Integer> entry:project.entrySet()){
      String projectname = entry.getKey();
      int num = entry.getValue();

      System.out.println("Experiment Project:  " + projectname);
      System.out.println("Algorithm:  ADTree");

      double preTotal = 0;
      double recTotal = 0;
      double f1Total = 0;
      
      for (int fold=0;fold<num; fold++) {
        String TrainingPath = "/home/a45zhang/project-se465/project-b/data/"+projectname+"/"+fold+"/train.arff";
        String TestingPath = "/home/a45zhang/project-se465/project-b/data/"+projectname+"/"+fold+"/test.arff";

        DefectPrediction model = new DefectPrediction(projectname, TrainingPath, TestingPath);
        model.loadData();
        model.run();
        
        preTotal += model.getPrecision();
        recTotal += model.getRecall();
        f1Total += model.getF1();


      }
      
      System.out.println("Precision:  " + preTotal/num);
      System.out.println("Recall:  " + recTotal/num);
      System.out.println("F1:  " + f1Total/num);
    }
  }
}
