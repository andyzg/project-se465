package change.clean;

public class InstancesResult {
	public int number;
	public String commit;
	public int RealLabel;
	public int CleanLabel;
	public double probAsBuggy;
	public int predictLabel;
	
	public InstancesResult(int number, String commit, int RealLabel, double probAsBuggy) {
		super();
		this.number = number;
		this.commit = commit;
		this.RealLabel = RealLabel;
		this.probAsBuggy = probAsBuggy;
	}
	
	
	public InstancesResult(int number, String commit, int RealLabel, int CleanLabel, double probAsBuggy) {
		super();
		this.number = number;
		this.commit = commit;
		this.RealLabel = RealLabel;
		this.CleanLabel = CleanLabel;
		this.probAsBuggy = probAsBuggy;
	}
	
	public int getPredictLabel() {
		return predictLabel;
	}


	public void setPredictLabel(int predictLabel) {
		this.predictLabel = predictLabel;
	}


	public String getCommit() {
		return commit;
	}


	public void setCommit(String commit) {
		this.commit = commit;
	}


	public int getCleanLabel() {
		return CleanLabel;
	}

	public void setCleanLabel(int cleanLabel) {
		this.CleanLabel = cleanLabel;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getRealLabel() {
		return RealLabel;
	}

	public void setRealLabel(int label) {
		this.RealLabel = label;
	}

	public double getProbAsBuggy() {
		return probAsBuggy;
	}

	public void setProbAsBuggy(double probAsBuggy) {
		this.probAsBuggy = probAsBuggy;
	}
	
	public String toString(){
		return "";
	}
}
