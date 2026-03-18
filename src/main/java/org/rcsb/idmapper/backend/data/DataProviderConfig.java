package org.rcsb.idmapper.backend.data;

public final class DataProviderConfig {
    public final DataProvider dataProvider;
    public final DataSource dataSource;

    public DataProviderConfig(DataProvider dataProvider, DataSource dataSource) {
        this.dataProvider = dataProvider;
        this.dataSource = dataSource;
    }
}
