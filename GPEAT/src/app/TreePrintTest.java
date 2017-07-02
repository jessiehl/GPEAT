package app;

import gp.BaseNode;
import gp.GeneticOperators;

public class TreePrintTest {
	public static void main(String[] args) {
		BaseNode test = GeneticOperators.createRandomTree(0, 4);
		test.print();
		BaseNode.networkize(test, 0);
	}
}
