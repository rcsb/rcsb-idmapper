package org.rcsb.idmapper.test.backend.data;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rcsb.common.constants.ContentType;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.backend.data.repository.ComponentRepository;
import org.rcsb.idmapper.backend.data.repository.GroupRepository;
import org.rcsb.idmapper.backend.data.repository.StructureRepository;
import org.rcsb.idmapper.input.Input;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private static final StructureRepository structureMock = new StructureRepository();
    private static final ComponentRepository componentMock = new ComponentRepository();
    private static final GroupRepository groupMock = new GroupRepository();

    private static final List<String> polyComps_e1 = List.of("AAA", "BBB", "CCC");
    private static final List<String> polyComps_e2 = List.of("AAA", "CCC", "DDD", "EEE");
    private static final List<String> polyComps = Stream
            .concat(polyComps_e1.stream(), polyComps_e2.stream()).distinct().toList();

    private static final List<String> branchedComps_e3 = List.of("NAD", "NAG");
    private static final List<String> branchedPrd_e3 = List.of("PRD_000000");
    private static final List<String> branchedComps_e4 = List.of("MAG");
    private static final List<String> branchedComps = Stream
            .concat(Stream.concat(branchedComps_e3.stream(), branchedComps_e4.stream()),
                    branchedPrd_e3.stream()).distinct().toList();
    private static final List<String> nonPolyComps = List.of("XXX", "YYY", "ZZZ");

    private static final List<String> entryComps = Stream
            .concat(Stream.concat(polyComps.stream(), branchedComps.stream()), nonPolyComps.stream()).toList();

    private static void populateStructureMock() {
        structureMock.addEntryToAssembly("1ABC", List.of("1"));
        structureMock.addEntryToPubmed("1ABC", 1234567);
        structureMock.addEntryToComps("1ABC", entryComps);

        // POLYMER
        structureMock.addEntryToPolymerEntity("1ABC", List.of("1", "2"));
        structureMock.addPolymerEntityToInstance("1ABC", "1", List.of("A"));
        structureMock.addPolymerEntityToInstance("1ABC", "1", List.of("B"));
        structureMock.addPolymerEntityToInstance("1ABC", "2", List.of("C"));
        structureMock.addPolymerEntityToCcd("1ABC", "1", polyComps_e1);
        structureMock.addPolymerEntityToCcd("1ABC", "2", polyComps_e2);
        structureMock.addPolymerEntityToUniprot("1ABC", "1", List.of("P00001"));
        structureMock.addPolymerEntityToUniprot("1ABC", "2", List.of("P00002", "P00003"));

        // BRANCHED
        structureMock.addEntryToBranchedEntity("1ABC", List.of("3", "4"));
        structureMock.addBranchedEntityToInstance("1ABC", "3", List.of("D"));
        structureMock.addBranchedEntityToInstance("1ABC", "4", List.of("E"));
        structureMock.addBranchedEntityToCcd("1ABC", "3", branchedComps_e3);
        structureMock.addBranchedEntityToBird("1ABC", "3", branchedPrd_e3.get(0));
        structureMock.addBranchedEntityToCcd("1ABC", "4", branchedComps_e4);

        // NON-POLYMER
        structureMock.addEntryToNonPolymerEntity("1ABC", List.of("5", "6", "7"));
        structureMock.addNonPolymerEntityToInstance("1ABC", "5", List.of("F"));
        structureMock.addNonPolymerEntityToInstance("1ABC", "6", List.of("G"));
        structureMock.addNonPolymerEntityToInstance("1ABC", "7", List.of("I"));
        structureMock.addNonPolymerEntityToComps("1ABC", "5", "XXX");
        structureMock.addNonPolymerEntityToComps("1ABC", "6", "YYY");
        structureMock.addNonPolymerEntityToComps("1ABC", "7", "ZZZ");
    }

    private static void populateComponentMock() {
        componentMock.addChemCompsToDrugBank("XXX", "DB00001");
        componentMock.addChemCompsToDrugBank("YYY", "DB00002");
    }

    private static void populateGroupMock() {
        groupMock.addGroupMembers(Input.AggregationMethod.sequence_identity, 100, "1_100", List.of("1ABC_1", "1ABC_2"));
        groupMock.addGroupProvenance("1_100", "match_sequence_identity");
    }

    @BeforeAll
    public static void setup() {
        populateStructureMock();
        populateComponentMock();
        populateGroupMock();
    }

    // ENTRY

    @Test
    public void entryToPolymerEntity_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.polymer_entity, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC", Input.Type.entry, Input.Type.polymer_entity, ContentType.experimental);
        assertEquals(2, ids.size());
        assertTrue(ids.contains("1ABC_1"));
        assertTrue(ids.contains("1ABC_2"));
    }

    @Test
    public void entryToBranchedEntity_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.branched_entity, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC", Input.Type.entry, Input.Type.branched_entity, ContentType.experimental);
        assertEquals(2, ids.size());
        assertTrue(ids.contains("1ABC_3"));
        assertTrue(ids.contains("1ABC_4"));
    }

    @Test
    public void entryToNonPolymerEntity_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.non_polymer_entity, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC", Input.Type.entry, Input.Type.non_polymer_entity, ContentType.experimental);
        assertEquals(3, ids.size());
        assertTrue(ids.contains("1ABC_5"));
        assertTrue(ids.contains("1ABC_6"));
        assertTrue(ids.contains("1ABC_7"));
    }

    @Test
    public void entryToPolymerInstance_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.polymer_instance, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.polymer_instance, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_2", Input.Type.polymer_entity, Input.Type.polymer_instance, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC", Input.Type.entry, Input.Type.polymer_instance, ContentType.experimental);
        assertEquals(3, ids.size());
        assertTrue(ids.contains("1ABC.A"));
        assertTrue(ids.contains("1ABC.B"));
        assertTrue(ids.contains("1ABC.C"));
    }

    @Test
    public void entryToBranchedInstance_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.branched_instance, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_3", Input.Type.branched_entity, Input.Type.branched_instance, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_4", Input.Type.branched_entity, Input.Type.branched_instance, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC", Input.Type.entry, Input.Type.branched_instance, ContentType.experimental);
        assertEquals(2, ids.size());
        assertTrue(ids.contains("1ABC.D"));
        assertTrue(ids.contains("1ABC.E"));
    }

    @Test
    public void entryToNonPolymerInstance_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.non_polymer_instance, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_5", Input.Type.non_polymer_entity, Input.Type.non_polymer_instance, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_6", Input.Type.non_polymer_entity, Input.Type.non_polymer_instance, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_7", Input.Type.non_polymer_entity, Input.Type.non_polymer_instance, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC", Input.Type.entry, Input.Type.non_polymer_instance, ContentType.experimental);
        assertEquals(3, ids.size());
        assertTrue(ids.contains("1ABC.F"));
        assertTrue(ids.contains("1ABC.G"));
        assertTrue(ids.contains("1ABC.I"));
    }

    @Test
    public void entryToAssembly_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.assembly, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC", Input.Type.entry, Input.Type.assembly, ContentType.experimental);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("1ABC-1"));
    }

    @Test
    public void entryToPubmed_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.pubmed, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC", Input.Type.entry, Input.Type.pubmed, ContentType.experimental);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("1234567"));
    }

    @Test
    public void entryToUniprot_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.uniprot, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.uniprot, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_2", Input.Type.polymer_entity, Input.Type.uniprot, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC", Input.Type.entry, Input.Type.uniprot, ContentType.experimental);
        assertEquals(3, ids.size());
        assertTrue(ids.contains("P00001"));
        assertTrue(ids.contains("P00002"));
        assertTrue(ids.contains("P00003"));
    }

    @Test
    public void entryToChemComps_MustPass() {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);

        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.mol_definition, ContentType.experimental)).thenCallRealMethod();
        Collection<String> ids = repo.lookup("1ABC", Input.Type.entry, Input.Type.mol_definition, ContentType.experimental);

        assertEquals(entryComps.size(), ids.size());
        assertEquals(entryComps.stream().sorted().toList(), ids.stream().sorted().toList());
    }

    @Test
    public void entryToDrugbank_MustPass() {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.getComponentRepository()).thenReturn(componentMock);
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.drug_bank, ContentType.experimental)).thenCallRealMethod();
        entryComps.forEach(cId ->  when(repo.lookup(cId, Input.Type.mol_definition, Input.Type.drug_bank, ContentType.experimental)).thenCallRealMethod());

        Collection<String> ids = repo.lookup("1ABC", Input.Type.entry, Input.Type.drug_bank, ContentType.experimental);

        assertEquals(2, ids.size());
        assertTrue(ids.contains("DB00001"));
        assertTrue(ids.contains("DB00002"));
    }

    // POLYMER ENTITY

    @Test
    public void polymerEntityToEntry_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.entry, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.entry, ContentType.experimental);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("1ABC"));
    }

    @Test
    public void polymerEntityToInstance_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.polymer_instance, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.polymer_instance, ContentType.experimental);
        assertEquals(2, ids.size());
        assertTrue(ids.contains("1ABC.A"));
        assertTrue(ids.contains("1ABC.B"));
    }

    @Test
    public void polymerToNonPolymerEntity_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.non_polymer_entity, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.non_polymer_entity, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.non_polymer_entity, ContentType.experimental);
        assertEquals(3, ids.size());
        assertTrue(ids.contains("1ABC_5"));
        assertTrue(ids.contains("1ABC_6"));
        assertTrue(ids.contains("1ABC_7"));
    }

    @Test
    public void polymerEntityToAssembly_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.assembly, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.assembly, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.assembly, ContentType.experimental);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("1ABC-1"));
    }

    @Test
    public void polymerEntityToChemComps_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.mol_definition, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.mol_definition, ContentType.experimental);
        assertEquals(polyComps_e1.size(), ids.size());
        assertEquals(polyComps_e1.stream().sorted().toList(), ids.stream().sorted().toList());
    }

    @Test
    public void polymerEntityToUniprot_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC_2", Input.Type.polymer_entity, Input.Type.uniprot, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC_2", Input.Type.polymer_entity, Input.Type.uniprot, ContentType.experimental);
        assertEquals(2, ids.size());
        assertTrue(ids.contains("P00002"));
        assertTrue(ids.contains("P00003"));
    }

    // BRANCHED ENTITY

    @Test
    public void branchedEntityToEntry_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC_3", Input.Type.branched_entity, Input.Type.entry, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC_3", Input.Type.branched_entity, Input.Type.entry, ContentType.experimental);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("1ABC"));
    }

    @Test
    public void branchedEntityToInstance_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC_3", Input.Type.branched_entity, Input.Type.branched_instance, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC_3", Input.Type.branched_entity, Input.Type.branched_instance, ContentType.experimental);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("1ABC.D"));
    }

    @Test
    public void branchedEntityToChemComps_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC_3", Input.Type.branched_entity, Input.Type.mol_definition, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC_3", Input.Type.branched_entity, Input.Type.mol_definition, ContentType.experimental);
        List<String> comps = Stream.concat(branchedComps_e3.stream(), branchedPrd_e3.stream()).toList();
        assertEquals(comps.size(), ids.size());
        assertEquals(comps.stream().sorted().toList(), ids.stream().sorted().toList());
    }

    // NON-POLYMER ENTITY

    @Test
    public void nonPolymerEntityToEntry_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC_5", Input.Type.non_polymer_entity, Input.Type.entry, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC_5", Input.Type.non_polymer_entity, Input.Type.entry, ContentType.experimental);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("1ABC"));
    }

    @Test
    public void nonPolymerEntityToInstance_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC_6", Input.Type.non_polymer_entity, Input.Type.non_polymer_instance, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC_6", Input.Type.non_polymer_entity, Input.Type.non_polymer_instance, ContentType.experimental);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("1ABC.G"));
    }

    @Test
    public void nonPolymerEntityToChemComps_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC_6", Input.Type.non_polymer_entity, Input.Type.mol_definition, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC_6", Input.Type.non_polymer_entity, Input.Type.mol_definition, ContentType.experimental);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("YYY"));
    }

    // ASSEMBLY

    @Test
    public void assemblyToEntry_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC-1", Input.Type.assembly, Input.Type.entry, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC-1", Input.Type.assembly, Input.Type.entry, ContentType.experimental);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("1ABC"));
    }

    @Test
    public void assemblyToPolymerEntity_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC-1", Input.Type.assembly, Input.Type.polymer_entity, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.polymer_entity, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC-1", Input.Type.assembly, Input.Type.polymer_entity, ContentType.experimental);
        assertEquals(2, ids.size());
        assertTrue(ids.contains("1ABC_1"));
        assertTrue(ids.contains("1ABC_2"));
    }

    @Test
    public void assemblyToNonPolymerEntity_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC-1", Input.Type.assembly, Input.Type.non_polymer_entity, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.non_polymer_entity, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC-1", Input.Type.assembly, Input.Type.non_polymer_entity, ContentType.experimental);
        assertEquals(3, ids.size());
        assertTrue(ids.contains("1ABC_5"));
        assertTrue(ids.contains("1ABC_6"));
        assertTrue(ids.contains("1ABC_7"));
    }

    @Test
    public void assemblyToNonPolymerInstance_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC-1", Input.Type.assembly, Input.Type.polymer_instance, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.polymer_entity, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.polymer_instance, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_2", Input.Type.polymer_entity, Input.Type.polymer_instance, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC-1", Input.Type.assembly, Input.Type.polymer_instance, ContentType.experimental);
        assertEquals(3, ids.size());
        assertTrue(ids.contains("1ABC.A"));
        assertTrue(ids.contains("1ABC.B"));
        assertTrue(ids.contains("1ABC.C"));
    }

    @Test
    public void assemblyToChemComps_MustPass() {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC-1", Input.Type.assembly, Input.Type.mol_definition, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.mol_definition, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC-1", Input.Type.assembly, Input.Type.mol_definition, ContentType.experimental);

        assertEquals(entryComps.size(), ids.size());
        assertEquals(entryComps.stream().sorted().toList(), ids.stream().sorted().toList());
    }

    // POLYMER INSTANCE

    @Test
    public void polymerInstanceToEntry_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC.A", Input.Type.polymer_instance, Input.Type.entry, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.entry, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC.A", Input.Type.polymer_instance, Input.Type.entry, ContentType.experimental);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("1ABC"));
    }

    @Test
    public void polymerInstanceToEntity_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC.A", Input.Type.polymer_instance, Input.Type.polymer_entity, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC.A", Input.Type.polymer_instance, Input.Type.polymer_entity, ContentType.experimental);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("1ABC_1"));
    }

    @Test
    public void polymerInstanceToNonPolymerEntity_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC.A", Input.Type.polymer_instance, Input.Type.non_polymer_entity, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.entry, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.non_polymer_entity, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC.A", Input.Type.polymer_instance, Input.Type.non_polymer_entity, ContentType.experimental);
        assertEquals(3, ids.size());
        assertTrue(ids.contains("1ABC_5"));
        assertTrue(ids.contains("1ABC_6"));
        assertTrue(ids.contains("1ABC_7"));
    }

    @Test
    public void polymerInstanceToAssembly_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC.A", Input.Type.polymer_instance, Input.Type.assembly, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.entry, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC", Input.Type.entry, Input.Type.assembly, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC.A", Input.Type.polymer_instance, Input.Type.assembly, ContentType.experimental);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("1ABC-1"));
    }

    @Test
    public void polymerInstanceToChemComps_MustPass()  {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("1ABC.C", Input.Type.polymer_instance, Input.Type.mol_definition, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_2", Input.Type.polymer_entity, Input.Type.mol_definition, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC.C", Input.Type.polymer_instance, Input.Type.mol_definition, ContentType.experimental);
        assertEquals(polyComps_e2.size(), ids.size());
        assertEquals(polyComps_e2.stream().sorted().toList(), ids.stream().sorted().toList());
    }

    // CHEM COMP

    @Test
    public void chemCompToEntry_MustPass() {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);

        when(repo.lookup("XXX", Input.Type.mol_definition, Input.Type.entry, ContentType.experimental)).thenCallRealMethod();
        Collection<String> ids = repo.lookup("XXX", Input.Type.mol_definition, Input.Type.entry, ContentType.experimental);

        assertEquals(1, ids.size());
        assertTrue(ids.contains("1ABC"));
    }

    @Test
    public void chemCompToPolymerEntity_MustPass() {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("AAA", Input.Type.mol_definition, Input.Type.polymer_entity, ContentType.experimental)).thenCallRealMethod();
        Collection<String> ids = repo.lookup("AAA", Input.Type.mol_definition, Input.Type.polymer_entity, ContentType.experimental);

        assertEquals(2, ids.size());
        assertTrue(ids.contains("1ABC_1"));
        assertTrue(ids.contains("1ABC_2"));
    }

    @Test
    public void chemCompToBranchedEntity_MustPass() {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("NAD", Input.Type.mol_definition, Input.Type.branched_entity, ContentType.experimental)).thenCallRealMethod();
        Collection<String> ids = repo.lookup("NAD", Input.Type.mol_definition, Input.Type.branched_entity, ContentType.experimental);

        assertEquals(1, ids.size());
        assertTrue(ids.contains("1ABC_3"));
    }

    @Test
    public void chemCompToNonPolymerEntity_MustPass() {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("XXX", Input.Type.mol_definition, Input.Type.non_polymer_entity, ContentType.experimental)).thenCallRealMethod();
        Collection<String> ids = repo.lookup("XXX", Input.Type.mol_definition, Input.Type.non_polymer_entity, ContentType.experimental);

        assertEquals(1, ids.size());
        assertTrue(ids.contains("1ABC_5"));
    }

    @Test
    public void chemCompToPolymerInstance_MustPass() {
        when(repo.getStructureRepository(ContentType.experimental)).thenReturn(structureMock);
        when(repo.lookup("AAA", Input.Type.mol_definition, Input.Type.polymer_instance, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_1", Input.Type.polymer_entity, Input.Type.polymer_instance, ContentType.experimental)).thenCallRealMethod();
        when(repo.lookup("1ABC_2", Input.Type.polymer_entity, Input.Type.polymer_instance, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("AAA", Input.Type.mol_definition, Input.Type.polymer_instance, ContentType.experimental);

        assertEquals(3, ids.size());
        assertTrue(ids.contains("1ABC.A"));
        assertTrue(ids.contains("1ABC.B"));
        assertTrue(ids.contains("1ABC.C"));
    }

    @Test
    public void chemCompToDrugbank_MustPass() {
        when(repo.getComponentRepository()).thenReturn(componentMock);
        when(repo.lookup("XXX", Input.Type.mol_definition, Input.Type.drug_bank, ContentType.experimental)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("XXX", Input.Type.mol_definition, Input.Type.drug_bank, ContentType.experimental);

        assertEquals(1, ids.size());
        assertTrue(ids.contains("DB00001"));
    }

    @Test
    public void polymerEntityToGroup_MustPass() {
        when(repo.getGroupRepository()).thenReturn(groupMock);
        when(repo.lookup("1ABC_1", Input.AggregationMethod.sequence_identity, 100)).thenCallRealMethod();

        Collection<String> ids = repo.lookup("1ABC_1", Input.AggregationMethod.sequence_identity, 100);

        assertEquals(1, ids.size());
        assertTrue(ids.contains("1_100"));
    }

    @Test
    public void notImplemented_MustReturnEmptyCollection()  {
        when(repo.lookup("P00002", Input.Type.uniprot, Input.Type.polymer_entity, ContentType.experimental)).thenCallRealMethod();
        Collection<String> ids = repo.lookup("P00002", Input.Type.uniprot, Input.Type.polymer_entity, ContentType.experimental);
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
    }
}
