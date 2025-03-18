package com.cq.YunPhoto.Manager.sharding;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.cq.YunPhoto.Exception.BusinessException;
import com.cq.YunPhoto.Model.entity.Space;
import com.cq.YunPhoto.Model.enums.SpaceLevelEnum;
import com.cq.YunPhoto.Model.enums.SpaceTypeEnum;
import com.cq.YunPhoto.service.SpaceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 自定义动态分表管理
 */

//@Component
@Slf4j
public class DynamicShardingManger {

    @Resource
    private DataSource dataSource;

    @Resource
    private SpaceService spaceService;

    private static final String LOCAL_TABLE_NAME = "picture";

    private static final String DATABASE_NAME = "yunphoto";

    @PostConstruct//@Component注解的类中，在初始化的时候加载配置方法
    public void init() {
        log.info("初始化分表管理器");
        //在初始化时更新配置文件中的分库配置
        updateShardingTableNodes();
    }

    /**
     * 获取所有动态表名，包括初始化表picture和分表picture_{spaceId}
     */
    public Set<String> findAllTableNames() {
        Set<String> collect = spaceService.getBaseMapper().selectObjs(new QueryWrapper<Space>()
                .eq("spaceType", SpaceTypeEnum.TEAM.getCode())
                //.eq("spaceLevel", SpaceLevelEnum.FLAGSHIP.getValue())暂时对团队空间进行分表，上线时对旗舰版进行分表
                .select("spaceId"))
                .stream().map(spaceId -> LOCAL_TABLE_NAME + "_" +spaceId).collect((Collectors.toSet()));
        collect.add(LOCAL_TABLE_NAME);
        return collect;
    }

    /**
     * 更新动态表名配置：actual-data-nodes
     */
    public void updateShardingTableNodes() {
        //获取所有动态表名，包括初始化表picture和分表picture_{spaceId}
        Set<String> tableNames = findAllTableNames();
        String collect = tableNames.stream().map(
                tableName -> "yunphoto." + tableName
        ).collect(Collectors.joining(","));
        log.info("更新分表配置：actual-data-nodes={}", collect);

        ContextManager contextManager = getContextManager();
        ShardingSphereRuleMetaData ruleMetaData = contextManager.getMetaDataContexts()
                .getMetaData()
                .getDatabases()
                .get(DATABASE_NAME)
                .getRuleMetaData();

        Optional<ShardingRule> shardingRule = ruleMetaData.findSingleRule(ShardingRule.class);
        if (shardingRule.isPresent()) {
            ShardingRuleConfiguration ruleConfig = (ShardingRuleConfiguration) shardingRule.get().getConfiguration();
            List<ShardingTableRuleConfiguration> updatedRules = ruleConfig.getTables()
                    .stream()
                    .map(oldTableRule -> {
                        if (LOCAL_TABLE_NAME.equals(oldTableRule.getLogicTable())) {
                            ShardingTableRuleConfiguration newTableRuleConfig = new ShardingTableRuleConfiguration(LOCAL_TABLE_NAME, collect);
                            newTableRuleConfig.setDatabaseShardingStrategy(oldTableRule.getDatabaseShardingStrategy());
                            newTableRuleConfig.setTableShardingStrategy(oldTableRule.getTableShardingStrategy());
                            newTableRuleConfig.setKeyGenerateStrategy(oldTableRule.getKeyGenerateStrategy());
                            newTableRuleConfig.setAuditStrategy(oldTableRule.getAuditStrategy());
                            return newTableRuleConfig;
                        }
                        return oldTableRule;
                    })
                    .collect(Collectors.toList());
            ruleConfig.setTables(updatedRules);
            contextManager.alterRuleConfiguration(DATABASE_NAME, Collections.singleton(ruleConfig));
            contextManager.reloadDatabase(DATABASE_NAME);
            log.info("动态分表规则更新成功！");
        } else {
            log.error("未找到 ShardingSphere 的分片规则配置，动态分表更新失败。");
        }

    }

    /**
     * 获取 ShardingSphere ContextManager
     */
    private ContextManager getContextManager() {
        try (ShardingSphereConnection connection = dataSource.getConnection().unwrap(ShardingSphereConnection.class)) {
            return connection.getContextManager();
        } catch (SQLException e) {
            throw new RuntimeException("获取 ShardingSphere ContextManager 失败", e);
        }
    }


    /**
     * 创建空间时创造分表
     */
    public void createTable(Space space) {
        String tableName = LOCAL_TABLE_NAME + "_" + space.getId();
        //只为旗舰版空间创建分表
        if(space.getSpaceType().equals(SpaceTypeEnum.TEAM.getCode()) && space.getSpaceLevel().equals(SpaceLevelEnum.FLAGSHIP.getValue())){
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "LIKE PICTURE";
            try {
                SqlRunner.db().update(sql);
                //更新分表
                updateShardingTableNodes();
            }catch (Exception e){
                log.error("创建分表失败");

            }
        }
    }


}
