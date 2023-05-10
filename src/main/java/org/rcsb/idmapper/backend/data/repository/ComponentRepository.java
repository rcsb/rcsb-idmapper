package org.rcsb.idmapper.backend.data.repository;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.List;

/**
 * Created on 4/20/23.
 *
 * @author Yana Rose
 */
public class ComponentRepository extends AnyRepository {
    // direct mappings
    private final Multimap<String, String> compsToDrugBank = HashMultimap.create();
    // reverse mappings
    private final Multimap<String, String> drugBankToComps = HashMultimap.create();

    public void addChemCompsToDrugBank(String compId, String drugBankId) {
        if (drugBankId == null) return;
        var ids = List.of(drugBankId);
        addValuesToDirectMap(compsToDrugBank, compId, ids);
        addValuesToReverseMap(drugBankToComps, compId, ids);
    }

    public Collection<String> getCompsToDrugBank(String compId) {
        return compsToDrugBank.get(compId);
    }

    public Collection<String> getDrugBankToComps(String drugBankId) {
        return drugBankToComps.get(drugBankId);
    }
}
