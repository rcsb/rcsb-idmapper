package org.rcsb.idmapper.backend.data;

public final class DataProviderConfig {
    public final DataProvider dataProvider;
    public final DataProvider.TaskProfile taskProfile;

    public DataProviderConfig(DataProvider dataProvider, DataProvider.TaskProfile taskProfile) {
        this.dataProvider = dataProvider;
        this.taskProfile = taskProfile;
    }
}
