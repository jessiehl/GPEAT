package gp;

public abstract class OpNode extends BaseNode {
	public BaseNode cloneData(OpNode toReturn) {
		toReturn.setInnovationNumber(m_innovationNum);
		for (int i = 0; i < m_numChildren; i++) {
			toReturn.getChildren().add(m_children.get(i).clone());
		}
		return toReturn;
	}
}
