package Veritas;

import java.util.EventListener;

/**
 *
 * @author Tobias Oskarsson och Adnan Dervisevic
 */
public interface ICalcEventListener extends EventListener {
    /**
     * En metod som körs varje gång equalBtnActionPerformed körs.
     * @param result Resultatet av uträkningen.
     */
    void calculationDone(double result);
}