package com.psi.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.customer.entity.SalesScriptLibraryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 销冠话术库 Mapper（旅程触达话术）
 */
@Mapper
public interface SalesScriptLibraryMapper extends BaseMapper<SalesScriptLibraryEntity> {

    /**
     * 查询启用状态的话术列表
     */
    @Select("SELECT * FROM sales_script_library " +
            "WHERE del_flag = 0 AND tenant_id = #{tenantId} AND is_enabled = 1 " +
            "ORDER BY stage_code ASC, day_start ASC, priority DESC, id ASC")
    List<SalesScriptLibraryEntity> selectEnabledList(@Param("tenantId") String tenantId);

    /**
     * 按分类查询话术
     */
    @Select("SELECT * FROM sales_script_library " +
            "WHERE del_flag = 0 AND tenant_id = #{tenantId} AND category = #{category} " +
            "ORDER BY stage_code ASC, day_start ASC, priority DESC, id ASC")
    List<SalesScriptLibraryEntity> selectByCategory(@Param("tenantId") String tenantId, @Param("category") String category);

    /**
     * 按阶段和停留天数查询可用话术
     */
    @Select("SELECT * FROM sales_script_library " +
            "WHERE del_flag = 0 AND tenant_id = #{tenantId} AND is_enabled = 1 " +
            "AND stage_code = #{stageCode} AND day_start <= #{dayInStage} AND day_end >= #{dayInStage} " +
            "ORDER BY priority DESC, day_start ASC, id ASC")
    List<SalesScriptLibraryEntity> selectByStageAndDay(@Param("tenantId") String tenantId,
                                                       @Param("stageCode") String stageCode,
                                                       @Param("dayInStage") Integer dayInStage);
}
