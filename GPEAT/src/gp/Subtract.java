package gp;

public class Subtract extends OpNode {

	public Subtract() {
		m_numChildren = 2;
		m_label = "-";
	}
	
	@Override
	public double evaluate(double inVal) {
		if (m_children.get(0) != null && m_children.get(1) != null){
			return m_children.get(0).evaluate(inVal) - m_children.get(1).evaluate(inVal);
		}
		else {
			System.err.println("Left and Right not defined for Subtract");
			return -1.0;
		}
	}

	@Override
	public BaseNode clone() {
		Subtract toReturn = new Subtract();
		toReturn.setInnovationNumber(this.getInnovationNumber());
		toReturn.setFitness(m_fitness);
		toReturn.setAdjustedFitness(m_adjustedFitness);
		return this.cloneData(toReturn);
	}

}
