package org.rcsb.idmapper.test;

import com.google.gson.Gson;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.ByteBufPayload;
import io.rsocket.util.DefaultPayload;
import org.rcsb.idmapper.IdMapper;
import org.rcsb.idmapper.frontend.GroupInput;
import org.rcsb.idmapper.frontend.Input;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.rcsb.idmapper.IdMapper.GROUP;

public class RSocketTestClient {
    public static void main(String[] args) {
        RSocket client =
                RSocketConnector.create()
                        .payloadDecoder(PayloadDecoder.ZERO_COPY)
                        .connectWith(TcpClientTransport.create(IdMapper.DEFAULT_RSOCKET_PORT)).block();



        GroupInput groupInput = new GroupInput();

        for (int i = 0; i < 3; i++) {
            groupInput.id = "4HHB";
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
