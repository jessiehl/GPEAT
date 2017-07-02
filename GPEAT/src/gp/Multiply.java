package gp;

public class Multiply extends OpNode {

	public Multiply() {
		m_numChildren = 2;
		m_label = "*";
	}
	
	@Override
	public double evaluate(double inVal) {
		if (m_children.get(0) != null && m_children.get(1) != null){
			return m_children.get(0).evaluate(inVal) * m_children.get(1).evaluate(inVal);
		}
		else {
			System.err.println("Left and Right not defined for Multiply");
			return -1.0;
		}
	}

	@Override
	public BaseNode clone() {
		Multiply toReturn = new Multiply();
		toReturn.setInnovationNumber(this.getInnovationNumber());
		toReturn.setFitness(m_fitness);
		toReturn.setAdjustedFitness(m_adjustedFitness);
		return this.cloneData(toReturn);
	}

}
