package cc.mrbird.febs.cos.service;

import cc.mrbird.febs.cos.entity.DrugRecord;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
public interface IDrugRecordService extends IService<DrugRecord> {

    /**
     * 分页获取药品名称记录信息
     *
     * @param page       分页对象
     * @param drugRecord 药品名称记录信息
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> queryDrugRecord(Page<DrugRecord> page, DrugRecord drugRecord);
}
