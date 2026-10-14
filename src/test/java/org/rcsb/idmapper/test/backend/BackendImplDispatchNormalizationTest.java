package org.rcsb.idmapper.test.backend;

import org.junit.jupiter.api.Test;
import org.rcsb.common.constants.ContentType;
import org.rcsb.common.constants.PdbIdFlavor;
import org.rcsb.idmapper.backend.BackendImpl;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.input.GroupInput;
import org.rcsb.idmapper.input.Input;
import org.rcsb.idmapper.input.TranslateInput;
import org.rcsb.idmapper.output.TranslateOutput;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BackendImplDispatchNormalizationTest {
    @Test
    public void dispatchTranslate_MustLookupNormalizedButReturnOriginalInput() {
        Repository repository = mock(Repository.class);
        when(repository.lookup("1ABC", Input.Type.entry, Input.Type.assembly, ContentType.experimental))
                .thenReturn(List.of("1ABC-1"));

        BackendImpl backend = new BackendImpl(Collections.emptyList(), repository, PdbIdFlavor.LEGACY);

        TranslateInput input = new TranslateInput();
        input.from = Input.Type.entry;
        input.to = Input.Type.assembly;
        input.ids = List.of("PDB_00001abc");
        input.content_type = List.of(ContentType.experimental);

        TranslateOutput output = (TranslateOutput) backend.dispatch(input).block();

        verify(repository).lookup("1ABC", Input.Type.entry, Input.Type.assembly, ContentType.experimental);
        assertTrue(output.results.get("1ABC-1").contains("PDB_00001abc"));
    }

    @Test
    public void dispatchGroup_MustLookupNormalizedButReturnOriginalInput() {
        Repository repository = mock(Repository.class);
        when(repository.lookup("1ABC_1", Input.AggregationMethod.sequence_identity, 100))
                .thenReturn(List.of("SEQ_GROUP_1"));

        BackendImpl backend = new BackendImpl(Collections.emptyList(), repository, PdbIdFlavor.LEGACY);

        GroupInput input = new GroupInput();
        input.ids = List.of("PDB_00001abc_1");
        input.aggregation_method = Input.AggregationMethod.sequence_identity;
        input.similarity_cutoff = 100;

        TranslateOutput output = (TranslateOutput) backend.dispatch(input).block();

        verify(repository).lookup("1ABC_1", Input.AggregationMethod.sequence_identity, 100);
        assertTrue(output.results.get("SEQ_GROUP_1").contains("PDB_00001abc_1"));
    }
}
