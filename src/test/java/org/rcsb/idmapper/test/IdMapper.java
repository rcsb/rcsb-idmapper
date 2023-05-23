package org.rcsb.idmapper.test;

import org.rcsb.common.constants.ContentType;
import org.rcsb.idmapper.backend.BackendImpl;
import org.rcsb.idmapper.backend.data.DataProvider;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.frontend.JsonMapper;
import org.rcsb.idmapper.frontend.RSocketFrontendImpl;
import org.rcsb.idmapper.frontend.UndertowFrontendImpl;
import org.rcsb.idmapper.input.Input;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.rcsb.idmapper.IdMapper.DEFAULT_HTTP_PORT;
import static org.rcsb.idmapper.IdMapper.DEFAULT_RSOCKET_PORT;

public class IdMapper {
    public static void main(String[] args) {

        var mockDataProvider = mock(DataProvider.class);
        var mockRepository = mock(Repository.class);

        when(mockDataProvider.initialize(any())).thenAnswer(invocationOnMock ->{
            return CompletableFuture.completedFuture(null);
        });


        when(mockRepository.lookup("BHH4", Input.Type.entry, Input.Type.polymer_entity, ContentType.experimental))
                .then(invocationOnMock -> {
                    return List.of("BHH4_1", "BHH4_2");
                });


        var backend = new BackendImpl(
                mockDataProvider,
                mockRepository
        );

        //TODO there may be multiple frontends e.g. one for RSocket, another for Undertow. Hence a factory will be needed
        var mapper = new JsonMapper().create();
        var undertow = new UndertowFrontendImpl<>(backend, DEFAULT_HTTP_PORT, mapper);
        var rsocket = new RSocketFrontendImpl<>(backend, DEFAULT_RSOCKET_PORT, mapper);

        try {
            backend.initialize();
            undertow.initialize();
            rsocket.initialize();

            backend.start();
            CompletableFuture.allOf(
                    undertow.start(),
                    rsocket.start()
            ).join();
        } catch (Exception e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        } finally {
            backend.stop();
        }
    }
}
