import java.util.HashSet;
import java.util.Set;

public class Cell implements Cloneable {
    private Set<Integer> potentialValues;

    public Cell() {
        potentialValues = new HashSet<>();
        clear();
    }

    public Cell(Set<Integer> potentialValues) {
        this.potentialValues = potentialValues;
    }

    public Set<Integer> getPotentialValues() {
        return potentialValues;
    }

    public Integer getValue() {
        return isSolved() ? potentialValues.iterator().next() : null;
    }

    /**
     * Cross out {@code i} from the list of potential values
     * @param i the number to cross out
     * @return -1 if nothing happened, 0 if something was removed but there are
     * still multiple potential values, or the value if there is only one value
     * remaining,
     */
    public Integer crossOut(Integer i) {
        if (potentialValues.contains(i)) {
            potentialValues.remove(i);
            if (potentialValues.size() == 1) {
                Integer value = potentialValues.iterator().next();
                setSolved(value);
                return value;
            }
            return 0;
        }
        return -1;
    }

    public boolean isSolved() {
        return potentialValues.size() == 1;
    }

    public void setSolved(Integer i) {
        getPotentialValues().removeIf(integer -> !integer.equals(i));
    }

    public void clear() {
        for (int i = 1; i < 10; ++i) {
            potentialValues.add(i);
        }
    }

    @Override
    public Cell clone() {
        return new Cell((Set<Integer>) ((HashSet) potentialValues).clone());
    }
}
