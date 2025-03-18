package com.cq.YunPhoto.Manager.sharding;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 *
 */
@Component
public class PictureShardingAlgorithm implements StandardShardingAlgorithm<Long> {


    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> preciseShardingValue) {
        // 编写分表逻辑，返回实际要查询的表名
        // picture_0 物理表，picture 逻辑表
        Long spaceId = preciseShardingValue.getValue();
        if(spaceId == null){
            return preciseShardingValue.getLogicTableName();
        }
        String tableName = "picture_"+spaceId;
        if(availableTargetNames.contains(tableName)){
            return tableName;
        }else {
            return preciseShardingValue.getLogicTableName();
        }
    }

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {
        return new ArrayList<>();
    }

    @Override
    public Properties getProps() {
        return null;
    }

    @Override
    public void init(Properties properties) {

    }
}
