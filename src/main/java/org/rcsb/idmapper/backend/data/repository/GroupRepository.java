package org.rcsb.idmapper.backend.data.repository;

import org.rcsb.idmapper.frontend.input.Input;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 4/19/23.
 *
 * @author Yana Rose
 */
public class GroupRepository extends AnyRepository {

    private final Map<String, String[]> groupToProvenance = new ConcurrentHashMap<>();

    private final Map<Input.AggregationMethod,
            Map<String, String[]>> identity = new ConcurrentHashMap<>(); // members ID -> group IDs

    private final Map<Input.AggregationMethod,
            Map<Integer, // similarity cutoff
                Map<String, String[]>>> similarity = new ConcurrentHashMap<>(); // members ID -> group IDs

    public void addGroupProvenance(String groupId, String provenanceId) {
        groupToProvenance.put(groupId, new String[]{provenanceId});
    }

    public void addGroupMembers(Input.AggregationMethod method, Integer cutoff, String gId, List<String> mIds) {
        var gIds = new String[]{gId};
        mIds.forEach(mId -> addValuesToMap(getMemberToGroup(method, cutoff), mId, gIds));
    }
    public Map <String, String[]> getGroupToProvenance() {
        return groupToProvenance;
    }

    public Map<String, String[]> getMemberToGroup(Input.AggregationMethod method, Integer cutoff) {
        if (cutoff != null)
            return similarity
                    .computeIfAbsent(method, k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(cutoff, k -> new ConcurrentHashMap<>());
        else
            return identity
                    .computeIfAbsent(method, k -> new ConcurrentHashMap<>());
    }
}
