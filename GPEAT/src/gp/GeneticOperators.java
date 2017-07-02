package gp;

import java.util.ArrayList;
import java.util.Random;

public class GeneticOperators {
	private static final int NUM_INPUTS = 1;
	public static final int INIT_TREE_DEPTH = 1;
	private static final int MAX_TREE_DEPTH = 50; 
	private static final double MUTATION_THRESH = 0.75;
	public static final double EXCESS_COEFF = 0.2;
	public static final double DISJOINT_COEFF = 0.2;
	public static final int MATCH = 0;
	public static final int EXCESS = 1;
	public static final int DISJOINT = 2;
	
	private static Random m_rng = new Random();
	private static int m_currInnovNum = 0;
	
	public static BaseNode mutate(BaseNode inTree, int depth){
		double randDouble = m_rng.nextDouble();
		BaseNode newNode = null;
		if(randDouble < MUTATION_THRESH) {
			if(randDouble < MUTATION_THRESH/2) {
				// modify the root node
				newNode = createRandomNode(depth, inTree.getMaxDepthOfTree());
				ArrayList<BaseNode> children = inTree.getChildren();
				newNode.setChildren(children);
				if(newNode.getChildren().size() == 0 && !(newNode instanceof InputNode || newNode instanceof ConstantNode)) {
					// then add two random terminal children
					children.add(createRandomChildlessNode());
					children.add(createRandomChildlessNode());
					newNode.setChildren(children);
				}
			}
			else {
				// if adding a new level, switch a child node to an OpNode and give it children
				// can only be done if you are at bottom level already and inTree is a terminal
				// otherwise, switch a child node to a different child node
				if(randDouble >= MUTATION_THRESH * 0.8 && randDouble < MUTATION_THRESH && depth == inTree.getMaxDepthOfTree()) {
					// adding a new level
					if(inTree.getMaxDepthOfTree() < MAX_TREE_DEPTH) {
						inTree.incrMaxDepthOfTree();
					}
					newNode = createRandomNode(depth, inTree.getMaxDepthOfTree());
					ArrayList<BaseNode> children = new ArrayList<BaseNode>();
					if(!(newNode instanceof InputNode || newNode instanceof ConstantNode)) {
						// then add two random terminal children
						children.add(createRandomChildlessNode());
						children.add(createRandomChildlessNode());
						newNode.setChildren(children);
					}
				}
				else {
					if(inTree.getChildren().size() > 0) {
						inTree.getChildren().set(m_rng.nextInt(inTree.getChildren().size()), createRandomChildlessNode());
					}
				}
				/*newNode = createRandomNode(depth + 1, inTree.getMaxDepthOfTree());//create a random node to replace current node
				if(randDouble >= MUTATION_THRESH * 0.8 && randDouble < MUTATION_THRESH) {
					// create new tree level
					if(inTree.getMaxDepthOfTree() < MAX_TREE_DEPTH) {
						inTree.incrMaxDepthOfTree();
					}
					newNode.getChildren().get(0).getChildren().add(createRandomNode(depth + 2, inTree.getMaxDepthOfTree()));
					newNode.getChildren().get(0).getChildren().add(createRandomNode(depth + 2, inTree.getMaxDepthOfTree()));
					newNode.getChildren().get(1).getChildren().add(createRandomNode(depth + 2, inTree.getMaxDepthOfTree()));
					newNode.getChildren().get(1).getChildren().add(createRandomNode(depth + 2, inTree.getMaxDepthOfTree()));
					if(newNode.getChildren().size() == 0 && !(newNode instanceof InputNode || newNode instanceof ConstantNode)) {
						int test = 0;
					}
				}*/
			}
			/*int childrenToMove = Math.min(newNode.getNumChildren(), inTree.getNumChildren());
			for (int i = 0; i < childrenToMove; i++){
				newNode.getChildren().add(inTree.getChildren().get(i));
			}
			int numNewChildren = newNode.getNumChildren() - childrenToMove;
			for (int i=childrenToMove; i<(childrenToMove + numNewChildren); i++){
				newNode.getChildren().add(createRandomTree(depth + 1, inTree.getMaxDepthOfTree()));
			}*/
			if(newNode != null) {
				inTree = newNode;
			}
			if(inTree.getMaxDepthOfTree() > 1) {
				int val = inTree.getMaxDepthOfTree();
				int test = 0;
			}
		}
		for (int i = 0; i < inTree.getNumChildren(); i++) {
			inTree.getChildren().set(i, mutate(inTree.getChildren().get(i), depth + 1));
		}
		return inTree;
	}
	
	public static void crossover(BaseNode firstTree, BaseNode secondTree){
		if (firstTree.getPosNumChildren() > 0 && secondTree.getPosNumChildren() > 0) {
			BaseNode dummyTree = null;
			int firstChild = m_rng.nextInt(firstTree.getPosNumChildren());//randomly select side of treeToAlter and donatingTree
			int secondChild = m_rng.nextInt(secondTree.getPosNumChildren());
			dummyTree = firstTree.getChildren().get(firstChild).clone(); // temp storage
			deleteTree(firstTree.getChildren().get(firstChild));//delete side of treeToAlter
			firstTree.getChildren().set(firstChild, secondTree.getChildren().get(secondChild).clone());// clone side of secondTree
			secondTree.getChildren().set(secondChild, dummyTree);
		}
		else {
			//System.err.println("not enough child nodes to do crossover\n");
			return;
		}
	}
	
	public static BaseNode createRandomNode(int depth, int limit){
		int randn = 0;
		int numFtnChoices = 2;
		BaseNode retFtn = null;
		
		if (depth < limit) numFtnChoices = 6; //if the depth is hitting the limit only allow const or inputs
		else numFtnChoices = 2;
		randn = m_rng.nextInt(numFtnChoices);		//generate random int
		
		switch (randn) {	//move random 
			case 0:
				retFtn = new ConstantNode(m_rng.nextDouble());
				break;
			case 1:
				retFtn = new InputNode(NUM_INPUTS);
				break;
			case 2:
				retFtn = new Add();
				break;
			case 3:
				retFtn = new Subtract();
				break;
			case 4:
				retFtn = new Multiply();
				break;
			case 5:
				retFtn = new Divide();
				break;
			default:
				System.err.println("Invalid random number\n");
				break;
		}
		retFtn.setInnovationNumber(m_currInnovNum);
		m_currInnovNum++;
		retFtn.setMaxDepthOfTree(limit); // make sure all nodes in tree have same limit
		return retFtn;
	}
	
	private static BaseNode createRandomChildlessNode() {
		int randn = 0;
		BaseNode retFtn = null;
		randn = m_rng.nextInt(2);
		if(randn == 0) {
			retFtn = new ConstantNode();
		}
		else {
			retFtn = new InputNode(NUM_INPUTS);
		}
		retFtn.setInnovationNumber(m_currInnovNum);
		m_currInnovNum++;
		return retFtn;
	}
	
	public static BaseNode createRandomTree(int depth, int limit){
		BaseNode retFtn = createRandomNode(depth, limit);
		
		for (int i = 0; i < retFtn.getPosNumChildren(); i++) {
			((OpNode)retFtn).getChildren().add(createRandomTree(depth+1, limit));
		}
		return retFtn;
	}
	
	public static void printTree(BaseNode inTree, int depth){
		for(int j = 0; j < depth; j++) {
			System.out.println("..");
		}
		System.out.println(inTree.getLabel() + '\n');
		for(int i = 0; i < inTree.getPosNumChildren(); i++) {
			printTree(inTree.getChildren().get(i), depth+1);
		}
	}
	
	public static void deleteTree(BaseNode inTree){
		for(int i = 0; i < inTree.getPosNumChildren(); i++) {  //free any children first
			deleteTree(inTree.getChildren().get(i));
		}
		inTree = null;
	}
}
