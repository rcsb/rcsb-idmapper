package org.rcsb.idmapper.test.backend;

import org.junit.jupiter.api.Test;
import org.rcsb.common.constants.PdbIdFlavor;
import org.rcsb.idmapper.backend.BackendImpl;
import org.rcsb.idmapper.backend.data.DataProvider;
import org.rcsb.idmapper.backend.data.DataProviderConfig;
import org.rcsb.idmapper.backend.data.DataSource;
import org.rcsb.idmapper.backend.data.Repository;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BackendImplFlavorValidationTest {
    @Test
    public void initialize_WhenDetectedFlavorMatchesConfigured_MustPass() throws Exception {
        DataProvider coreProvider = mockCoreProvider(PdbIdFlavor.EXTENDED);
        BackendImpl backend = new BackendImpl(
                List.of(new DataProviderConfig(coreProvider, DataSource.CORE_PDB)),
                new Repository(),
                PdbIdFlavor.EXTENDED
        );

        assertDoesNotThrow(backend::initialize);
    }

    @Test
    public void initialize_WhenDetectedFlavorMismatchesConfigured_MustThrow() throws Exception {
        DataProvider coreProvider = mockCoreProvider(PdbIdFlavor.EXTENDED);
        BackendImpl backend = new BackendImpl(
                List.of(new DataProviderConfig(coreProvider, DataSource.CORE_PDB)),
                new Repository(),
                PdbIdFlavor.LEGACY
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class, backend::initialize);
        org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains("PDB ID flavor mismatch"));
    }

    private static DataProvider mockCoreProvider(PdbIdFlavor detectedFlavor) throws Exception {
        DataProvider provider = mock(DataProvider.class);
        when(provider.connect(any())).thenReturn(new Closeable() {
            @Override
            public void close() throws IOException {
            }
        });
        when(provider.detectCorePdbFlavor()).thenReturn(detectedFlavor);
        when(provider.initialize(any(), any())).thenReturn(CompletableFuture.completedFuture(null));
        doNothing().when(provider).postInitializationCheck(any(), any());
        return provider;
    }
}
