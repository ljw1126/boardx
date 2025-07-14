package com.example.outboxmessagerelay;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.LongStream;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssignedShard {
    private List<Long> shards;

    public static AssignedShard of(String appId, List<String> appIds, long shardCount) {
        AssignedShard assignedShard = new AssignedShard();
        assignedShard.shards = assign(appId, appIds, shardCount);
        return assignedShard;
    }

    private static List<Long> assign(String appId, List<String> appIds, long shardCount) {
        int appIdx = findAppIndex(appId, appIds);
        if(appIdx == -1) {
            return Collections.emptyList();
        }

        long start = appIdx * shardCount / appIds.size();
        long end = (appIdx + 1) * shardCount / appIds.size() - 1;

        return LongStream.rangeClosed(start, end).boxed().toList();
    }

    private static int findAppIndex(String appId, List<String> appIds) {
        for(int i = 0; i < appIds.size(); i++) {
            if(appIds.get(i).equals(appId)) {
                return i;
            }
        }
        return -1;
    }

}
