package org.rcsb.idmapper.test.backend.data;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rcsb.common.constants.ContentType;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.backend.data.repository.StructureRepository;
import org.rcsb.idmapper.input.Input;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created on 5/30/23.
 *
 * @author Yana Rose
 */
@ExtendWith(MockitoExtension.class)
public class RepositoryTest {

    @Mock
    private Repository repo;

    private static final StructureRepository structureExperimentalMock = new StructureRepository();

    @BeforeAll
    public static void setup() {
        structureExperimentalMock.addEntryToAssembly("1ABC", List.of("1"));
        structureExperimentalMock.addEntryToPubmed("1ABC", 1234567);
        structureExperimentalMock.addEntryToComps("1ABC", List.of("AAA", "BBB", "CCC", "DDD",
                "EEE", "NAG", "NAD", "MAG", "PRD_000000", "XXX", "YYY", "ZZZ"));

        // POLYMER
        structureExperimentalMock.addEntryToPolymerEntity("1ABC", List.of("1", "2"));
        structureExperimentalMock.addPolymerEntityToInstance("1ABC", "1", List.of("A"));
        structureExperimentalMock.addPolymerEntityToInstance("1ABC", "1", List.of("B"));
        structureExperimentalMock.addPolymerEntityToInstance("1ABC", "2", List.of("C"));
        structureExperimentalMock.addPolymerEntityToCcd("1", List.of("AAA", "BBB", "CCC"));
        structureExperimentalMock.addPolymerEntityToCcd("2", List.of("AAA", "CCC", "DDD"));
        // structureExperimentalMock.addPolymerEntityToBird
        structureExperimentalMock.addPolymerEntityToUniprot("1", List.of("P00001"));
        structureExperimentalMock.addPolymerEntityToUniprot("2", List.of("P00002", "P00003"));

        // BRANCHED
        structureExperimentalMock.addEntryToBranchedEntity("1ABC", List.of("3", "4"));
        structureExperimentalMock.addBranchedEntityToInstance("1ABC", "3", List.of("D"));
        structureExperimentalMock.addBranchedEntityToInstance("1ABC", "4", List.of("E"));
        structureExperimentalMock.addBranchedEntityToCcd("3", List.of("NAG", "NAD"));
        structureExperimentalMock.addBranchedEntityToCcd("4", List.of("MAG"));
        structureExperimentalMock.addBranchedEntityToBird("3", "PRD_000000");

        // NON-POLYMER
        structureExperimentalMock.addEntryToNonPolymerEntity("1ABC", List.of("5", "6", "7"));
        structureExperimentalMock.addNonPolymerEntityToInstance("1ABC", "5", List.of("F"));
        structureExperimentalMock.addNonPolymerEntityToInstance("1ABC", "6", List.of("G"));
        structureExperimentalMock.addNonPolymerEntityToInstance("1ABC", "7", List.of("I"));
        structureExperimentalMock.addNonPolymerEntityToComps("5", "XXX");
        structureExperimentalMock.addNonPolymerEntityToComps("6", "YYY");
        structureExperimentalMock.addNonPolymerEntityToComps("7", "ZZZ");
    }
    
    @Test
    public void testEntryToBranchedEntity()  {

        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureExperimentalMock);
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.branched_instance, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_3", Input.Type.branched_entity, Input.Type.branched_instance, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_4", Input.Type.branched_entity, Input.Type.branched_instance, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC", Input.Type.entry, Input.Type.branched_instance, ContentType.experimental);

        assertEquals(2, ids.size());
        assertTrue(ids.contains("1ABC.D"));
        assertTrue(ids.contains("1ABC.E"));
    }
}
