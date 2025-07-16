package com.example.outboxmessagerelay;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssignedShardTest {

    @Test
    void shards() {
        long shardCount = 10L;
        List<String> apps = List.of("appId1", "appId2", "appId3");

        AssignedShard assignedShard1 = AssignedShard.of("appId1", apps, shardCount);
        AssignedShard assignedShard2 = AssignedShard.of("appId2", apps, shardCount);
        AssignedShard assignedShard3 = AssignedShard.of("appId3", apps, shardCount);
        AssignedShard assignedShard4 = AssignedShard.of("invalid", apps, shardCount);

        assertThat(assignedShard1.getShards()).containsExactly(0L, 1L, 2L);
        assertThat(assignedShard2.getShards()).containsExactly(3L, 4L, 5L);
        assertThat(assignedShard3.getShards()).containsExactly(6L, 7L, 8L, 9L);
        assertThat(assignedShard4.getShards()).isEmpty();
    }
}
