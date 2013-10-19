package Veritas;

import java.util.EventListener;

/**
 *
 * @author Tobias Oskarsson och Adnan Dervisevic
 */
public interface ICalcConversionEventListener extends EventListener {
    /**
     * En metod som körs varje gång convertBtnActionPerformed körs.
     * @param result Resultatet av konverteringen.
     */    
    void conversionDone(double result);
}