package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.PharmacyInventory;
import cc.mrbird.febs.cos.entity.PurchaseInfo;
import cc.mrbird.febs.cos.service.IPharmacyInventoryService;
import cc.mrbird.febs.cos.service.IPurchaseInfoService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author FanK
 */
@RestController
@RequestMapping("/cos/purchase-info")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PurchaseInfoController {

    private final IPurchaseInfoService purchaseInfoService;

    private final IPharmacyInventoryService pharmacyInventoryService;

    /**
     * 分页获取药品采购信息
     *
     * @param page         分页对象
     * @param purchaseInfo 药品采购信息
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<PurchaseInfo> page, PurchaseInfo purchaseInfo) {
        return R.ok(purchaseInfoService.selectPurchasePage(page, purchaseInfo));
    }

    /**
     * 查询采购列表
     *
     * @param pharmacyId 采购ID
     * @param drugId     药品ID
     * @return 结果
     */
    @GetMapping("/queryPurchaseList")
    public R queryPurchaseList(Integer pharmacyId, Integer drugId) {
        List<PurchaseInfo> purchaseInfoList = purchaseInfoService.list(Wrappers.<PurchaseInfo>lambdaQuery().eq(PurchaseInfo::getPharmacyId, pharmacyId));
        if (CollectionUtil.isEmpty(purchaseInfoList)) {
            return R.ok(Collections.emptyList());
        }
        List<Map> result = new ArrayList<>();
        for (PurchaseInfo purchaseInfo : purchaseInfoList) {
            List<PharmacyInventory> inventoryList = JSONUtil.toList(purchaseInfo.getPurchaseDrug(), PharmacyInventory.class);
            List<PharmacyInventory> matchedRecords = inventoryList.stream().filter(inventory -> inventory.getDrugId().equals(drugId)).collect(Collectors.toList());
            if (CollectionUtil.isEmpty(matchedRecords)) {
                continue;
            }
            PharmacyInventory inventory = matchedRecords.get(0);
            // 找到匹配的药品，记录相关信息
            Map<String, Object> record = new HashMap<>();
            record.put("drugId", inventory.getDrugId());
            record.put("reserve", inventory.getReserve()); // 入库数量
            record.put("purchaseCode", purchaseInfo.getCode()); // 采购单号
            record.put("purchaser", purchaseInfo.getPurchaser()); // 采购人
            record.put("purchaseDate", purchaseInfo.getCreateDate()); // 采购时间
            result.add(record);
        }
        return R.ok(result);
    }

    /**
     * 减库存
     *
     * @param pharmacyId 采购ID
     * @param drugId     药品ID
     * @param num        数量
     * @return 结果
     */
    @GetMapping("/cancelStock")
    public R cancelStock(Integer pharmacyId, Integer drugId, Integer num) {
        PharmacyInventory pharmacyInventory = pharmacyInventoryService.getOne(Wrappers.<PharmacyInventory>lambdaQuery().eq(PharmacyInventory::getPharmacyId, pharmacyId).eq(PharmacyInventory::getDrugId, drugId));
        Integer updateNum = pharmacyInventory.getReserve() - num;
        pharmacyInventory.setReserve(updateNum < 0 ? 0 : updateNum);
        return R.ok(pharmacyInventoryService.updateById(pharmacyInventory));
    }

    /**
     * 收货
     *
     * @param id 采购ID
     * @return 结果
     */
    @GetMapping("/receipt/{id}")
    public R receipt(@PathVariable("id") Integer id) throws Exception {
        return R.ok(purchaseInfoService.receipt(id));
    }

    /**
     * 采购单详情-药品物流
     *
     * @param purchaseId 采购ID
     * @return 结果
     */
    @GetMapping("/detail/{purchaseId}")
    public R detailPurchase(@PathVariable("purchaseId") String purchaseId) {
        return R.ok(purchaseInfoService.detailPurchase(purchaseId));
    }

    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(purchaseInfoService.getById(id));
    }

    @GetMapping("/list")
    public R list() {
        return R.ok(purchaseInfoService.list());
    }

    /**
     * 新增药品采购信息
     *
     * @param purchaseInfo 药品采购信息
     * @return 结果
     */
    @PostMapping
    public R save(PurchaseInfo purchaseInfo) {
        purchaseInfo.setCode("PUR-" + System.currentTimeMillis());
        purchaseInfo.setCreateDate(DateUtil.formatDateTime(new Date()));
        return R.ok(purchaseInfoService.purchaseAdd(purchaseInfo));
    }

    /**
     * 修改药品采购信息
     *
     * @param purchaseInfo 药品采购信息
     * @return 结果
     */
    @PutMapping
    public R edit(PurchaseInfo purchaseInfo) {
        return R.ok(purchaseInfoService.updateById(purchaseInfo));
    }

    /**
     * 删除药品采购信息
     *
     * @param ids ids
     * @return 药品采购信息
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(purchaseInfoService.removeByIds(ids));
    }
}
