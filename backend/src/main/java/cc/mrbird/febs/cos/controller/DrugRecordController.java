package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.DrugRecord;
import cc.mrbird.febs.cos.entity.StaffInfo;
import cc.mrbird.febs.cos.service.IDrugRecordService;
import cc.mrbird.febs.cos.service.IStaffInfoService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author FanK
 */
@RestController
@RequestMapping("/cos/drug-record")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DrugRecordController {

    private final IDrugRecordService drugRecordService;

    private final IStaffInfoService staffInfoService;

    /**
     * 分页获取药品名称记录信息
     *
     * @param page       分页对象
     * @param drugRecord 药品名称记录信息
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<DrugRecord> page, DrugRecord drugRecord) {
        return R.ok(drugRecordService.queryDrugRecord(page, drugRecord));
    }

    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(drugRecordService.getById(id));
    }

    @GetMapping("/list")
    public R list() {
        return R.ok(drugRecordService.list());
    }

    /**
     * 新增药品名称记录信息
     *
     * @param drugRecord 药品名称记录信息
     * @return 结果
     */
    @PostMapping
    public R save(DrugRecord drugRecord) {
        drugRecord.setCreateDate(DateUtil.formatDateTime(new Date()));
        StaffInfo staffInfo = staffInfoService.getOne(Wrappers.<StaffInfo>lambdaQuery().eq(StaffInfo::getUserId, drugRecord.getStaffId()));
        if (staffInfo != null) {
            drugRecord.setStaffId(staffInfo.getId());
        }
        return R.ok(drugRecordService.save(drugRecord));
    }

    /**
     * 修改药品名称记录信息
     *
     * @param drugRecord 药品名称记录信息
     * @return 结果
     */
    @PutMapping
    public R edit(DrugRecord drugRecord) {
        return R.ok(drugRecordService.updateById(drugRecord));
    }

    /**
     * 删除药品名称记录信息
     *
     * @param ids ids
     * @return 药品名称记录信息
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(drugRecordService.removeByIds(ids));
    }
}
