package org.rcsb.idmapper.test;

import com.google.gson.Gson;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.ByteBufPayload;
import org.rcsb.idmapper.AppConfigs;
import org.rcsb.idmapper.input.GroupInput;
import org.rcsb.idmapper.input.Input;

import java.util.List;

import static org.rcsb.idmapper.IdMapper.GROUP;

//TODO use test containers library to spin up this container (possibly with some limited data provider) and run the tests
public class RSocketTestClient {
    public static void main(String[] args) {
        RSocketConnector.create()
                .payloadDecoder(PayloadDecoder.ZERO_COPY);
        RSocket client =
                RSocketConnector
                        .connectWith(TcpClientTransport.create(AppConfigs.DEFAULT_RSOCKET_PORT))
                        .block();

        GroupInput groupInput = new GroupInput();

        for (int i = 0; i < 3; i++) {
            groupInput.ids = List.of("4HHB");
            groupInput.aggregation_method = Input.AggregationMethod.values()[i];
            client
                    .requestResponse(ByteBufPayload.create(new Gson().toJson(groupInput),GROUP))
                    .map(Payload::getDataUtf8)
                    .onErrorReturn("error")
                    .doOnNext(System.out::println)
                    .block();
        }

        client.dispose();
    }
}
