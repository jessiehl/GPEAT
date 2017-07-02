package gp;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import app.GPApp;

public abstract class BaseNode implements Comparable<BaseNode> {
	protected int m_innovationNum;
	protected String m_label;
	protected int m_numChildren;
	protected Random m_rng = new Random();
	protected double m_fitness;
	protected double m_adjustedFitness;
	protected int m_maxDepthOfTree = GeneticOperators.INIT_TREE_DEPTH;
	protected ArrayList<BaseNode> m_children = new ArrayList<BaseNode>();
	
	public abstract double evaluate(double inVal);
	public abstract BaseNode clone();
	
	public String getLabel() {
		return m_label;
	}
	
	public int getPosNumChildren() {
		return m_numChildren;
	}
	
	public int getNumChildren() {
		return m_children.size();
	}
	
	public int getInnovationNumber() {
		return m_innovationNum;
	}
	
	public void setInnovationNumber(int innovationNum) {
		m_innovationNum = innovationNum;
	}
	
	public double getFitness() {
		return m_fitness;
	}
	
	public void setFitness(double fitness) {
		m_fitness = fitness;
	}
	
	public ArrayList<BaseNode> getChildren() {
		return m_children;
	}
	
	public void setChildren(ArrayList<BaseNode> children) {
		m_children = children;
	}
	
	public boolean hasChildren() {
		if(m_children.size() > 0) return true;
		else return false;
	}
	
	public int getMaxDepthOfTree() {
		return m_maxDepthOfTree;
	}
	
	public void setMaxDepthOfTree(int depth) {
		m_maxDepthOfTree = depth;
	}
	
	public void incrMaxDepthOfTree() {
		m_maxDepthOfTree++;
	}
	
	public double getAdjustedFitness() {
		return m_adjustedFitness;
	}
	
	public void setAdjustedFitness(double adjustedFitness) {
		m_adjustedFitness = adjustedFitness;
	}
	
	public double distanceTo(BaseNode that) {
		int[] count = {0,0,0,0};
		count = traverse(count, that);
		
		int n = Math.max(count[0], count[1]); // which tree is bigger
		int excess = count[2];
		int disjoint = count[3];
		
		return (GeneticOperators.EXCESS_COEFF * excess) / n +
				(GeneticOperators.DISJOINT_COEFF * disjoint) / n;
	}
	
	public int[] traverse(int[] count, BaseNode root) { 
		count[0]++;
		if(isInnovationShared(root, count) == GeneticOperators.EXCESS) {
			count[2]++;
		}
		else if(isInnovationShared(root, count) == GeneticOperators.DISJOINT) {
			count[3]++;
		}
		if(root.hasChildren()) {
			traverse(count, root.getChildren().get(0));
		}
		if(root.hasChildren() && root.getChildren().size() > 1) {
			traverse(count, root.getChildren().get(1));
		}
		return count;
	}
	
	/**
	 * Determines whether node exists as an innovation in a specified tree
	 * @param root The root of the specified tree
	 * @return The node with the same innovation number in the specified
	 * tree, or null if no such node exists
	 */
	public int isInnovationShared(BaseNode root, int[] count) {
		count[1]++;
		if(root.getInnovationNumber() == this.getInnovationNumber()) {
	        return GeneticOperators.MATCH;
	    }
	    if(root.hasChildren()) { // left
	        return isInnovationShared(root.getChildren().get(0), count);
	    }
	    if(root.hasChildren() && root.getChildren().size() > 1) { // right
	        return isInnovationShared(root.getChildren().get(1), count);
	    }
	    if(root.getInnovationNumber() > this.getInnovationNumber()) {
	    	return GeneticOperators.EXCESS;
	    }
	    else {
	    	return GeneticOperators.DISJOINT;
	    }
	}
	
	public static int countModules(BaseNode root) {
		int count = 0;
		if(root.hasChildren() && root.getChildren().get(0) instanceof OpNode && root.getChildren().get(1) instanceof OpNode) count++;
		if(root.hasChildren()){
	        countModules(root.getChildren().get(0));
	    }
	    if(root.hasChildren() && root.getChildren().size() > 1){
	        countModules(root.getChildren().get(1));
	    }
		return count;
	}
	
	public static void networkizeLabels(BaseNode root, int gen) {
		String filename = GPApp.SAVE_DIRNAME;
		filename = filename.concat("Gen" + Integer.toString(gen));
		filename = filename.concat(".txt");
		try {
			FileWriter writer = new FileWriter(filename, true);
			networkizeLabelsTraversal(root, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void networkizeLabelsTraversal(BaseNode root, FileWriter writer) {
		String leftnum = "", rightnum = "";
		
		if (root.hasChildren()) {
	        networkizeLabelsTraversal(root.getChildren().get(0), writer);
	    }
		if(!root.hasChildren()) {
			leftnum = "-1";
		}
		else {
			leftnum = root.getChildren().get(0).getLabel();
		}
		if(!(root.hasChildren() && root.getChildren().size() > 1)) {
			rightnum = "-1";
		}
		else {
			rightnum = root.getChildren().get(1).getLabel();
		}
	    try {
	    	writer.write('\n');
			writer.write(root.getLabel() + " " + leftnum + " " + rightnum);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    if(root.hasChildren() && root.getChildren().size() > 1){
	        networkizeLabelsTraversal(root.getChildren().get(1), writer);
	    }
	}
	
	public static void networkize(BaseNode root, int gen) {
		String filename = GPApp.SAVE_DIRNAME;
		filename = filename.concat("Gen" + Integer.toString(gen));
		filename = filename.concat(".txt");
		try {
			FileWriter writer = new FileWriter(filename, true);
			networkizeTraversal(root, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void networkizeTraversal(BaseNode root, FileWriter writer) {
		String leftnum = "", rightnum = "";
		
		if (root.hasChildren()) {
	        networkizeTraversal(root.getChildren().get(0), writer);
	    }
		if(!root.hasChildren()) {
			leftnum = "-1";
		}
		else {
			leftnum = Integer.toString(root.getChildren().get(0).getInnovationNumber());
		}
		if(!(root.hasChildren() && root.getChildren().size() > 1)) {
			rightnum = "-1";
		}
		else {
			rightnum = Integer.toString(root.getChildren().get(1).getInnovationNumber());
		}
	    try {
	    	writer.write('\n');
			writer.write(Integer.toString(root.getInnovationNumber()) + " " + leftnum + " " + rightnum);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    if (root.hasChildren() && root.getChildren().size() > 1){
	        networkizeTraversal(root.getChildren().get(1), writer);
	    }
	}
	
	public void print() {
        print(this, 0);
    }
    
    private void print(BaseNode root, int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("   ");
        }
        if (root == null) {
            System.out.println("null");
            return;
        }
        System.out.println(root.getLabel());
        if (root instanceof InputNode || root instanceof ConstantNode) return;
        print(root.getChildren().get(0), indent + 1);
        print(root.getChildren().get(1), indent + 1);
    }
	
	public int compareTo(BaseNode that) {
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;
	    
		if(this.m_fitness == that.m_fitness) return EQUAL;
		else if(this.m_fitness < that.m_fitness) return BEFORE;
		else return AFTER;
	}
}
