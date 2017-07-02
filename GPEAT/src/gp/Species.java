package gp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Species {
	protected ArrayList<BaseNode> m_trees = null;
	protected boolean m_treesAreSorted = true;
	protected double m_speciationThreshold = 0.3;
	protected BaseNode m_representativeTree = null;
	protected double m_fitnessSum = 0;
	protected BaseNode m_bestTree = null;
	protected double m_bestFitness = Double.MAX_VALUE;
	public boolean m_representativeIsFirst = true;		
	public boolean m_replacement = true;	
	public int m_speciesId;
	static public int m_speciesNextId=0;
	
	// No weights coefficient in speciation because these are binary links
	
	public Species(BaseNode representative, double speciationThreshold, boolean addRepresentative) {
		m_speciationThreshold = speciationThreshold;
		m_representativeTree = representative.clone();
		m_trees = new ArrayList<BaseNode>();
		if (addRepresentative) {
			addTree(representative, false);
		}
		m_speciesId = m_speciesNextId++;
	}
	
	/**
	 * Replace random tree with tree of your choice
	 * @param aTree The tree to add
	 */
	public void addTree(BaseNode aTree) {
		Random rng = new Random();
		m_trees.set(rng.nextInt(m_trees.size()), aTree);
	}
	
	public boolean addTree(BaseNode aTree, boolean testSpecies) {
		if (!testSpecies || m_representativeTree.distanceTo(aTree) <= m_speciationThreshold) {
			if(m_representativeTree == null) {
				m_representativeTree = aTree;
			}
			m_trees.add(aTree);
			m_treesAreSorted=false;
			m_fitnessSum += aTree.getFitness();
			if (aTree.getFitness() < m_bestFitness) {
				m_bestFitness = aTree.getFitness();
				m_bestTree = aTree;
				if (!m_representativeIsFirst) {
					m_representativeTree = m_bestTree.clone();
				}
			}
			return true;
		} 
		else {
			return false;
		}
	}
	
	public int size() {
		return m_trees.size();
	}
	
	public BaseNode representativeTree() {
		return m_representativeTree;
	}
	
	public double getSpeciationThreshold() {
		return m_speciationThreshold;
	}
	
	public void setSpeciationThreshold(double speciationThreshold) {
		m_speciationThreshold = speciationThreshold;
	}
	
	public void chooseRandomRepresentative(Random rng) {		
		m_representativeTree = m_trees.get(rng.nextInt(m_trees.size()) ).clone();
	}
	
	public ArrayList<BaseNode> getTrees() {
		return m_trees;
	}
	
	public BaseNode getBestTree() {
		return m_bestTree;
	}
	
	public void removeAllTrees() {
		m_fitnessSum = 0;
		m_bestTree = null;
		m_bestFitness = Double.MAX_VALUE;
		m_trees.clear();
	}
	
	public void recomputeFitness(double input) {
		for( int k = 0; k < m_trees.size(); k++ ) {
			//genomes.set(k, evaluator.evaluate( genomes.get(k) ));
			m_trees.get(k).evaluate(input);
		}
	}
	
	public BaseNode tournamentSelect(int nParticipant, Random rng) {
		/*// Preserve best, others are fair game to be mutated or crossed over
		BaseNode[] candidates = new BaseNode[m_trees.size()];
		candidates = m_trees.toArray(candidates);
		Arrays.sort(candidates);
		m_trees.clear();
		m_trees.addAll(Arrays.asList(candidates));
		List<BaseNode> possibles = new ArrayList<BaseNode>();
		if(m_trees.size() > 0) {
			possibles = m_trees.subList(1, m_trees.size());
		}
		else {
			possibles = m_trees;
		}*/
		
		ArrayList<BaseNode> tournament = new ArrayList<BaseNode>();
		
		while (tournament.size() < nParticipant) {
			// select a participant randomly
			BaseNode p = m_trees.get((int)(rng.nextDouble()*m_trees.size()));
			if (!tournament.contains(p) || m_replacement) {
				tournament.add(p);
			}
		}
		BaseNode[] tournamentArray = new BaseNode[tournament.size()];
		tournamentArray = tournament.toArray(tournamentArray);
		Arrays.sort(tournamentArray);
		return tournamentArray[0];
	}
}
