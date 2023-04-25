package org.rcsb.idmapper.backend.data.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 4/20/23.
 *
 * @author Yana Rose
 */
public class ComponentRepository extends AnyRepository {
    // direct mappings
    private final Map<String, String[]> compsToDrugBank = new ConcurrentHashMap<>();
    // reverse mappings
    private final Map<String, String[]> drugBankToComps = new ConcurrentHashMap<>();

    public void addChemCompsToDrugBank(String compId, String drugBankId) {
        if (drugBankId == null) return;
        String[] ids = new String[]{drugBankId};
        addValuesToMap(compsToDrugBank, compId, ids);
        //addNonEmptyValuesReverse(drugBankToComps, compId, ids);
    }

    public Map<String, String[]> getCompsToDrugBank() {
        return compsToDrugBank;
    }

    public Map<String, String[]> getDrugBankToComps() {
        return drugBankToComps;
    }
}
