package org.rcsb.idmapper.backend.data.repository;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.rcsb.idmapper.input.Input;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created on 4/19/23.
 *
 * @author Yana Rose
 */
public class GroupRepository extends AnyRepository {

    private final Multimap<String, String> groupToProvenance = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);

    private final Map<Input.AggregationMethod,
            Multimap<String, String>> identity = new HashMap<>(); // members ID -> group IDs

    private final Map<Input.AggregationMethod,
            Map<Integer, // similarity cutoff
            Multimap<String, String>>> similarity = new HashMap<>(); // members ID -> group IDs

    public void addGroupProvenance(String groupId, String provenanceId) {
        groupToProvenance.put(groupId, provenanceId);
    }

    public void addGroupMembers(Input.AggregationMethod method, Integer cutoff, String gId, List<String> mIds) {
        Multimap<String, String> map;
        if (cutoff != null) {
            map = similarity.computeIfAbsent(method, k -> new HashMap<>())
                    .computeIfAbsent(cutoff, k -> HashMultimap.create());
        } else {
            map = identity.computeIfAbsent(method, k -> HashMultimap.create());
        }
        mIds.forEach(id -> map.put(id, gId));
    }

    public Collection<String> getGroupToProvenance(String groupId) {
        return groupToProvenance.get(groupId);
    }

    public Collection<String> getMemberToGroup(Input.AggregationMethod method, Integer cutoff, String mId) {
        if (cutoff != null)
            return similarity.get(method).get(cutoff).get(mId);
        else
            return identity.get(method).get(mId);
    }

    public Long countGroups(Input.AggregationMethod method) {
        if (method == Input.AggregationMethod.sequence_identity) {
            AtomicReference<Long> count = new AtomicReference<>(0L);
            similarity.get(method).keySet()
                    .forEach(cutoff -> count.updateAndGet(v -> v + similarity.get(method).get(cutoff)
                            .values().stream().distinct().count()));
            return count.get();
        } else {
            return identity.get(method).values().stream().distinct().count();
        }
    }
}
