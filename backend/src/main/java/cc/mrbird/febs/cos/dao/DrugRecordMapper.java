package cc.mrbird.febs.cos.dao;

import cc.mrbird.febs.cos.entity.DrugRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
public interface DrugRecordMapper extends BaseMapper<DrugRecord> {

    /**
     * 分页获取药品名称记录信息
     *
     * @param page       分页对象
     * @param drugRecord 药品名称记录信息
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> queryDrugRecord(Page<DrugRecord> page, @Param("drugRecord") DrugRecord drugRecord);
}
