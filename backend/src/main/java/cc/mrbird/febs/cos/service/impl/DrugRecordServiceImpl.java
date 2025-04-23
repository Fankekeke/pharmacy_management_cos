package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.DrugRecord;
import cc.mrbird.febs.cos.dao.DrugRecordMapper;
import cc.mrbird.febs.cos.service.IDrugRecordService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
@Service
public class DrugRecordServiceImpl extends ServiceImpl<DrugRecordMapper, DrugRecord> implements IDrugRecordService {

    /**
     * 分页获取药品名称记录信息
     *
     * @param page       分页对象
     * @param drugRecord 药品名称记录信息
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryDrugRecord(Page<DrugRecord> page, DrugRecord drugRecord) {
        return baseMapper.queryDrugRecord(page, drugRecord);
    }
}
