package org.rcsb.idmapper.backend.data.repository;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.rcsb.idmapper.input.Input;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created on 4/19/23.
 *
 * @author Yana Rose
 */
public class GroupRepository extends AnyRepository {

    private final Multimap<String, String> groupToProvenance = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);

    private final Map<Input.AggregationMethod,
            Map<Integer, // similarity cutoff (can be NULL for identity-based groups)
            Multimap<String, String>>> memberToGroup = new HashMap<>(); // members ID -> group IDs

    public void addGroupProvenance(String groupId, String provenanceId) {
        groupToProvenance.put(groupId, provenanceId);
    }

    public void addGroupMembers(Input.AggregationMethod method, Integer cutoff, String gId, List<String> mIds) {
        Multimap<String, String> map = memberToGroup.computeIfAbsent(method, k -> new HashMap<>())
                .computeIfAbsent(cutoff, k -> HashMultimap.create());
        mIds.forEach(id -> map.put(id, gId));
    }

    public Collection<String> getGroupToProvenance(String groupId) {
        return groupToProvenance.get(groupId);
    }

    private boolean isSimilarityBasedGrouping(Input.AggregationMethod method) {
        return method == Input.AggregationMethod.sequence_identity;
    }

    public Collection<String> getMemberToGroup(Input.AggregationMethod method, Integer cutoff, String mId) {
        if (isSimilarityBasedGrouping(method)) {
            if (cutoff == null) {
                List<String> all = new ArrayList<>();
                memberToGroup.get(method).keySet()
                        .forEach(c -> {
                            Multimap<String, String> map = memberToGroup.get(method).get(c);
                            if (map.containsKey(mId)) {
                                all.addAll(map.get(mId));
                            }
                        });
                return all;
            }
            else return memberToGroup.get(method).get(cutoff).get(mId);
        } else {
            return memberToGroup.get(method).get(null).get(mId);
        }
    }

    public Long countGroups(Input.AggregationMethod method) {
        AtomicReference<Long> count = new AtomicReference<>(0L);
        memberToGroup.get(method).keySet()
                .forEach(cutoff -> count.updateAndGet(v -> v + memberToGroup.get(method).get(cutoff)
                        .values().stream().distinct().count()));
        return count.get();
    }
}
