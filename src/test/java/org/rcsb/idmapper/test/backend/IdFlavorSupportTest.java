package org.rcsb.idmapper.test.backend;

import org.junit.jupiter.api.Test;
import org.rcsb.common.constants.PdbIdFlavor;
import org.rcsb.idmapper.backend.IdFlavorSupport;
import org.rcsb.idmapper.input.Input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IdFlavorSupportTest {
    @Test
    public void parseFlavor_MissingOrBlank_MustDefaultLegacy() {
        assertEquals(PdbIdFlavor.LEGACY, IdFlavorSupport.parseConfiguredFlavor(null));
        assertEquals(PdbIdFlavor.LEGACY, IdFlavorSupport.parseConfiguredFlavor("   "));
    }

    @Test
    public void parseFlavor_ValidValues_MustPass() {
        assertEquals(PdbIdFlavor.LEGACY, IdFlavorSupport.parseConfiguredFlavor("legacy"));
        assertEquals(PdbIdFlavor.EXTENDED, IdFlavorSupport.parseConfiguredFlavor("EXTENDED"));
    }

    @Test
    public void parseFlavor_InvalidValue_MustThrow() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> IdFlavorSupport.parseConfiguredFlavor("hybrid"));
        assertTrue(exception.getMessage().contains("Invalid value for env var DB_FLAVOR"));
    }

    @Test
    public void normalizeTranslate_EntryToLegacy_MustAcceptLegacyAndExtendedPdb() {
        assertEquals("1ABC", IdFlavorSupport.normalizeTranslateInputId("1aBc", Input.Type.entry, PdbIdFlavor.LEGACY));
        assertEquals("1ABC", IdFlavorSupport.normalizeTranslateInputId("PDB_00001abc", Input.Type.entry, PdbIdFlavor.LEGACY));
    }

    @Test
    public void normalizeTranslate_EntryToExtended_MustRequireExtendedPdb() {
        assertEquals("pdb_00001abc",
                IdFlavorSupport.normalizeTranslateInputId("PDB_00001ABC", Input.Type.entry, PdbIdFlavor.EXTENDED));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> IdFlavorSupport.normalizeTranslateInputId("1ABC", Input.Type.entry, PdbIdFlavor.EXTENDED));
        assertTrue(exception.getMessage().contains("Invalid PDB ID for db.flavor=extended"));
    }

    @Test
    public void normalizeTranslate_PolymerEntity_MustOnlyNormalizeEntryPart() {
        assertEquals("1ABC_7",
                IdFlavorSupport.normalizeTranslateInputId("PDB_00001abc_7", Input.Type.polymer_entity, PdbIdFlavor.LEGACY));
        assertEquals("pdb_00001abc_7",
                IdFlavorSupport.normalizeTranslateInputId("PDB_00001ABC_7", Input.Type.polymer_entity, PdbIdFlavor.EXTENDED));
    }

    @Test
    public void normalizeTranslate_CsmIds_MustPassThrough() {
        assertEquals("af_a0a000_1", IdFlavorSupport.normalizeTranslateInputId("af_a0a000_1", Input.Type.entry, PdbIdFlavor.LEGACY));
        assertEquals("af_a0a000_1_2", IdFlavorSupport.normalizeTranslateInputId("af_a0a000_1_2", Input.Type.polymer_entity, PdbIdFlavor.EXTENDED));
    }

    @Test
    public void normalizeGroup_MustFollowAggregationMethodRules() {
        assertEquals("1ABC", IdFlavorSupport.normalizeGroupInputId("pdb_00001abc",
                Input.AggregationMethod.matching_deposit_group_id, PdbIdFlavor.LEGACY));
        assertEquals("pdb_00001abc_1", IdFlavorSupport.normalizeGroupInputId("PDB_00001ABC_1",
                Input.AggregationMethod.sequence_identity, PdbIdFlavor.EXTENDED));
    }
}
