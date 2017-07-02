package gp;

import java.text.DecimalFormat;

public class ConstantNode extends BaseNode {

	protected double m_constVal;
	
	public ConstantNode() {
		m_numChildren = 0;
		m_constVal = m_rng.nextDouble();
		m_label = "";
	}
	
	public ConstantNode(double constVal) {
		m_numChildren = 0;
		m_constVal = constVal;
		m_label = new DecimalFormat("#.##").format(constVal);
	}
	
	@Override
	public double evaluate(double inVal) {
		return m_constVal;
	}

	@Override
	public BaseNode clone() {
		ConstantNode toReturn = new ConstantNode(m_constVal);
		toReturn.setInnovationNumber(m_innovationNum);
		toReturn.setFitness(m_fitness);
		toReturn.setAdjustedFitness(m_adjustedFitness);
		return toReturn;
	}

}
