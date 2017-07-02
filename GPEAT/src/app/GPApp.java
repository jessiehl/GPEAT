package app;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import gp.BaseNode;
import gp.GeneticOperators;
import gp.Species;

public class GPApp {
	public static final String SAVE_DIRNAME = "C:/Users/jessiehl/Documents/DEMO/GrandTheoryOfModularity/GPEAT2/results/eqn2/";
	public static final int NUM_INIT_TREES = 500;
	public static final int NUM_GENERATIONS = 2000;
	public static final int NUM_DIE_PER_GEN = 90; //how much of the pop we loose per generation
	public static final int NUM_INPUTS = 9;
	public static final double CROSSOVER_RATE = 0.1;
	protected static ArrayList<Species> m_species = new ArrayList<Species>();
	protected static int m_speciesMinSize = 1;
	protected static int m_allSpeciesEverNum = 0;
	protected static double m_speciationThreshold = 0.15;
	public static int m_tournamentSize = 3;
	public static int m_nMaxTries = 10;
	protected static BaseNode m_bestEver;
	protected static int m_nModulesEver = 0;
	
	public static void main(String[] args) {
		String filename = GPApp.SAVE_DIRNAME;
		filename = filename.concat("Trial19");
		filename = filename.concat(".txt");
		FileWriter writer = null;
		try {
			writer = new FileWriter(filename, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		double sumAbsErr, err = 0.0, err1 = 0.0;
		//create array to store N trees
		BaseNode[] initTrees = new BaseNode[NUM_INIT_TREES];
		
		double[] inputs = {1, 2, 3, 4, 5, 6, 7, 8, 9}; // length NUM_INPUTS
		double[] outputs = {6, 15, 28, 45, 66, 91, 120, 153, 190}; //2x^2 + 3x + 1, length NUM_INPUTS
		double[] outputs2 = {12, 58, 134, 294, 552, 932, 1458, 2154, 3044}; //4x^3 + x^2 + 5x + 2
		
		// create the initial tree population 
		for (int i=0; i<NUM_INIT_TREES; i++){
			initTrees[i] = GeneticOperators.createRandomTree(0, GeneticOperators.INIT_TREE_DEPTH);
		}
		
		for (int gen = 0; gen < NUM_GENERATIONS; gen++) {
			// test the performance of each one
			// this should be problem-dependent, and implemented in another file
			for (int i = 0; i < NUM_INIT_TREES; i++){
				sumAbsErr = 0.0;
				for (int j = 0; j < NUM_INPUTS; j++) {
					err = outputs[j] - (initTrees[i].evaluate(inputs[j]));
					err1 = outputs2[j] - (initTrees[i].evaluate(inputs[j]));
					sumAbsErr += Math.abs(err);
					sumAbsErr += Math.abs(err1);
				}
				//System.out.println("Evaluating tree " + i + ": " + sumAbsErr +  '\n');
				if (sumAbsErr == 0){
					System.out.println("Found a solution with 0 error\n");
					GeneticOperators.printTree(initTrees[i], 0);
					break;
				}
				if (Double.isNaN(sumAbsErr)) sumAbsErr = Double.MAX_VALUE;
				initTrees[i].setFitness(sumAbsErr);
			}
			
			//sort by performance (sort in increasing order so we work on first N)
			Arrays.sort(initTrees);
			if(m_bestEver == null || initTrees[0].getFitness() < m_bestEver.getFitness()) {
				m_bestEver = initTrees[0].clone();
			}
			//BaseNode.networkize(initTrees[0], gen);
			int modulesThisGen = BaseNode.countModules(initTrees[0]);
			m_nModulesEver += modulesThisGen;
			try {
				writer.write(Integer.toString(modulesThisGen) + " ");
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Gen " + gen + " best: " + initTrees[0].getFitness() + ", Best Ever: " + m_bestEver.getFitness()+ ", NumSpecies: " + m_species.size() + ", TotSpecies: " + m_allSpeciesEverNum + ", Modules: " + modulesThisGen);
			
			for (int i = initTrees.length-NUM_DIE_PER_GEN; i < initTrees.length; i++){
				GeneticOperators.deleteTree(initTrees[i]);	// get rid of bad performers
				//randomly select one of the survivors and clone
				initTrees[i] = initTrees[getRandSurvivor()].clone();
				//do cross over with survivors 
				//GeneticOperators.crossover(initTrees[i], initTrees[getRandSurvivor()]);
			}
			/*for(int i = 0; i < initTrees.length; i++) {
				GeneticOperators.mutate(initTrees[i], 0);	//mutate
			}*/
			speciate(initTrees);
			
			// adjusting fitness
			int numOff[] = new int[m_species.size()];
			int sumNumOff = 0;
			double sumAdjFit[] = new double[m_species.size()];
			double sumSumAdjFit=0;

			// Adjusting according to the species size
			for(int i = 0; i < m_species.size(); i++) {
				Species s = m_species.get(i);
				double spSize = s.size();
				sumAdjFit[i] = 0;
				for (BaseNode gi : s.getTrees()) {
					gi.setAdjustedFitness(gi.getFitness()/spSize);
					sumAdjFit[i] += gi.getAdjustedFitness();
				}
				sumSumAdjFit+=sumAdjFit[i];
			}
			adjustSpeciationThresholds();
			// calculating offspring number for each species
			for (int i = 0; i < m_species.size(); i++) {
				numOff[i] = (int)(sumAdjFit[i]/sumSumAdjFit*NUM_INIT_TREES);
				sumNumOff += numOff[i];
			}
			// correcting approximation errors
			Random rng = new Random();
			while (sumNumOff < NUM_INIT_TREES) {
				int rnd=(int)(rng.nextDouble()*m_species.size());
				numOff[rnd]+=1;
				sumNumOff++;
			}

			// renewing species
			// initTrees = new BaseNode[initTrees.length]; // clear initTrees, trees are in species
			int nTrees;
			for (int i = 0; i < m_species.size(); i++) {
				Species s = m_species.get(i);
				BaseNode bestInSpecies = s.getBestTree();
				nTrees = 0;
				// adding crossovered trees
				int nbFailedCross = 0;
				while (nTrees+nbFailedCross < CROSSOVER_RATE*numOff[i]) {
					BaseNode g1=s.tournamentSelect(m_tournamentSize,rng);
					BaseNode g2=s.tournamentSelect(m_tournamentSize,rng);
					int tries=0;
					while (g1==g2 && tries++<m_nMaxTries) {
						g2=s.tournamentSelect(m_tournamentSize,rng);
					}
					if (tries<10) {
						//GeneticOperators.deleteTree(initTrees[i]);	// get rid of bad performers
						// assign a selected parent there
						//initTrees[i] = g1;
						//do crossover with survivors 
						GeneticOperators.crossover(g1, g2);
						//BaseNode child = crossovers.get(indexCrossover).reproduce(g1, g2, rng);
						//offsprings.add(child);
						nTrees++;
					} else {
						nbFailedCross++;
					}
				}
				// adding mutated trees
				for(int j = 0; j < s.size(); j++) {
					// mutation probability is handled by GeneticOperators.mutate method
					BaseNode selectGen = s.tournamentSelect(m_tournamentSize, rng);
					int idx = s.getTrees().indexOf(selectGen);
					selectGen = GeneticOperators.mutate(selectGen, 0);	//mutate
					s.getTrees().set(idx, selectGen);
					//offsprings.add(mutatedGen);
					nTrees++;
				}
				// adding best tree back in to replace random member
				s.addTree(bestInSpecies);
				/*if (numOff[i]>0) {
					offsprings.add(s.getBestTree());
					nTrees++;
					//System.err.print(s.getBestFitness()+"  ");
*/				// completing with existing genomes
				/*while (nTrees<numOff[i]) {
					offsprings.add(s.tournamentSelect(tournamentSize, rng));
					nTrees++;
				}*/
			}
			// dump everything back into overall tree list
			int treeIdx = 0;
			for(int i = 0; i < m_species.size(); i++) {
				for(int j = 0; j < m_species.get(i).size(); j++) {
					initTrees[treeIdx] = m_species.get(i).getTrees().get(j);
					treeIdx++;
				}
			}
		}
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Arrays.sort(initTrees);
		System.out.println("nModulesEver: " + m_nModulesEver);
		GeneticOperators.printTree(initTrees[0], 0);
		//GeneticOperators.printTree(initTrees[NUM_INIT_TREES - 1], 0);
	}
	
	private static void adjustSpeciationThresholds() {
		// Adjusting speciation threshold to regulate the sizes
		double speciesAvgSize = NUM_INIT_TREES / m_species.size();
		for (Species s : m_species) {
			if (s.size()<speciesAvgSize) {
                s.setSpeciationThreshold(Math.min(0.5,s.getSpeciationThreshold()+0.01));//speciationThreshold/10;
            } else {
                s.setSpeciationThreshold(Math.max(0.03, s.getSpeciationThreshold()-0.01));//speciationThreshold/10;
            }
		}
	}
	
	private static int getRandSurvivor(){
		Random rng = new Random();
		int randn = rng.nextInt(NUM_INIT_TREES - NUM_DIE_PER_GEN);
		return (NUM_DIE_PER_GEN + randn);
	}
	
	public static void speciate(BaseNode[] population) {
		Random rng = new Random();
		for (Species s : m_species) {
			s.chooseRandomRepresentative( rng );
			s.removeAllTrees();
		}
		for (BaseNode g : population) {
			Species bestMatch=null;
			double minDist=Double.MAX_VALUE;
			for (Species s : m_species) {
				double currentDist=g.distanceTo(s.representativeTree());
				if (currentDist<minDist && currentDist < s.getSpeciationThreshold()) {
					bestMatch=s;
					minDist=currentDist;
				}
			}
			if (bestMatch==null || minDist>bestMatch.getSpeciationThreshold()) {
				// Creating a new Species
				Species s=new Species(g, m_speciationThreshold, true);
				m_species.add(s);
				m_allSpeciesEverNum++;
			} else {
				// add the tree to the best matching species
				bestMatch.addTree(g, false);
			}
		}
		// removing empty species
		int numtreeDelete = 0;//populationSize-population.size();
		int numSpeciesDeleted = 0;
		for (int i = 0; i < m_species.size(); i++) {
			Species s = m_species.get(i);
			//System.out.println( s.size() );
			if (s.size()<m_speciesMinSize && m_species.size() > 1) {
				numtreeDelete += s.size();
				m_species.remove(s);
				i--;
				numSpeciesDeleted++;
			}
		}
		while (numtreeDelete > 0) {
			int rnd = (int)(rng.nextDouble()*m_species.size());
			Species s = m_species.get(rnd);
			BaseNode g = s.tournamentSelect(m_tournamentSize, rng);
			boolean treeAdd=false;
			int tries=0;
			while (!treeAdd && tries++ < m_nMaxTries) {
				// mutate
				//BaseNode mutatedGen = g.clone();
				GeneticOperators.mutate(g, 0);	//mutate
				
				if (g != null) {
					treeAdd=s.addTree(g, true);
				}
			}
			if (treeAdd) {
				numtreeDelete--;
			}
		}
	}
}
