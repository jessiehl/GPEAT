package gp;

public class InputNode extends BaseNode {
	protected int m_inputIdx;

	public InputNode(int numPosInputs) {
		m_numChildren = 0;
		m_inputIdx = m_rng.nextInt(numPosInputs);
		setValues(m_inputIdx);
	}
	
	public void setValues(int inIdx) {
		m_label = new String("In"+inIdx);
		m_inputIdx = inIdx;
	}
	
	@Override
	public double evaluate(double inVal) {
		return inVal;
	}

	@Override
	public BaseNode clone() {
		InputNode toReturn = new InputNode(1);
		toReturn.setInnovationNumber(m_innovationNum);
		toReturn.setFitness(m_fitness);
		toReturn.setAdjustedFitness(m_adjustedFitness);
		toReturn.setValues(this.m_inputIdx);
		return toReturn;
	}

}
