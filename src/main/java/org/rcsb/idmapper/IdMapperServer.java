package org.rcsb.idmapper;

import org.rcsb.idmapper.backend.BackendImpl;
import org.rcsb.idmapper.backend.data.DataProvider;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.frontend.JsonMapper;
import org.rcsb.idmapper.frontend.RSocketFrontendImpl;
import org.rcsb.idmapper.frontend.UndertowFrontendImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class IdMapperServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdMapperServer.class);

    public static final String TRANSLATE = "/translate";
    public static final String GROUP = "/group";
    public static final String ALL = "/all";
    public static final String MONGODB_URI = "MONGODB_URI";
    public static final String MONGODB_USER = "MONGODB_USER";
    public static final String MONGODB_PWD = "MONGODB_PWD";

    public static void main(String[] args) {

        String connectionString = Objects.requireNonNull(System.getenv(MONGODB_URI),
                String.format("The environment variable [ %s ] with Mongo database connection string (URI) must be set", MONGODB_URI));
        int numPlaceHolders = (int) Pattern.compile("%s").matcher(connectionString).results().count();
        if (numPlaceHolders != 2) {
            LOGGER.error("Mongo connection URI string [ {} ] does not contain exactly 2 placeholders. " +
                    "Please check '{}' env var", connectionString, MONGODB_URI);
            throw new IllegalArgumentException("Mongo connection URI string does not contain exactly 2 placeholders");
        }
        String user = Objects.requireNonNull(System.getenv(MONGODB_USER), String.format("The environment variable [ %s ] with Mongo database user must be set", MONGODB_USER));
        String pwd = Objects.requireNonNull(System.getenv(MONGODB_PWD), String.format("The environment variable [ %s ] with Mongo database password must be set", MONGODB_PWD));
        // note anything that goes into the mongo URI must be URL-encoded
        connectionString = String.format(connectionString,
                URLEncoder.encode(user, StandardCharsets.UTF_8),
                URLEncoder.encode(pwd, StandardCharsets.UTF_8));

        var backend = new BackendImpl(
                new DataProvider(connectionString),
                new Repository()
        );

        //TODO there may be multiple frontends e.g. one for RSocket, another for Undertow. Hence a factory will be needed
        var mapper = new JsonMapper().create();
        var undertow = new UndertowFrontendImpl<>(backend, AppConfigs.DEFAULT_HTTP_PORT, mapper);
        var rsocket = new RSocketFrontendImpl<>(backend, AppConfigs.DEFAULT_RSOCKET_PORT, mapper);

        try {

            final Properties properties = new Properties();
            properties.load(IdMapperServer.class.getClassLoader().getResourceAsStream("project.properties"));
            LOGGER.info("==> Application version is {}", properties.get("version"));

            backend.initialize();
            undertow.initialize();
            rsocket.initialize();

            backend.start();
            CompletableFuture.allOf(
                    undertow.start(),
                    rsocket.start()
            ).join();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } finally {
            backend.stop();
        }
    }
}
