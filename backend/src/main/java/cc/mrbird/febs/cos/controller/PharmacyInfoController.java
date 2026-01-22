package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.PharmacyInfo;
import cc.mrbird.febs.cos.service.IPharmacyInfoService;
import cc.mrbird.febs.system.service.UserService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author FanK
 */
@RestController
@RequestMapping("/cos/pharmacy-info")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PharmacyInfoController {

    private final IPharmacyInfoService pharmacyInfoService;

    private final UserService userService;

    /**
     * 分页获取药店信息
     *
     * @param page         分页对象
     * @param pharmacyInfo 药店信息
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<PharmacyInfo> page, PharmacyInfo pharmacyInfo) {
        return R.ok(pharmacyInfoService.selectPharmacyPage(page, pharmacyInfo));
    }

    /**
     * 查询可调配的药店信息
     *
     * @return 列表
     */
    @GetMapping("/queryPharmacyAllocate")
    public R queryPharmacyAllocate(Integer pharmacyId) {
        List<PharmacyInfo> pharmacyList = pharmacyInfoService.list(Wrappers.<PharmacyInfo>lambdaQuery()
                .eq(PharmacyInfo::getBusinessStatus, 1)
                .ne(PharmacyInfo::getId, pharmacyId));

        if (CollectionUtil.isEmpty(pharmacyList)) {
            return R.ok(Collections.emptyList());
        }

        PharmacyInfo currentPharmacy = pharmacyInfoService.getById(pharmacyId);
        if (currentPharmacy == null || currentPharmacy.getLatitude() == null || currentPharmacy.getLongitude() == null) {
            return R.ok(Collections.emptyList());
        }

        // 获取当前药店的经纬度
        Double centerLat = currentPharmacy.getLatitude().doubleValue();
        Double centerLng = currentPharmacy.getLongitude().doubleValue();

        // 筛选出30公里范围内的药店
        List<PharmacyInfo> nearbyPharmacies = new ArrayList<>();
        for (PharmacyInfo pharmacy : pharmacyList) {
            if (pharmacy.getLatitude() != null && pharmacy.getLongitude() != null) {
                double distance = calculateDistance(centerLat, centerLng,
                        pharmacy.getLatitude().doubleValue(), pharmacy.getLongitude().doubleValue());
                if (distance <= 30) {
                    pharmacy.setDistance(distance);
                    nearbyPharmacies.add(pharmacy);
                }
            }
        }
        // 按距离排序
        nearbyPharmacies.sort(Comparator.comparingDouble(PharmacyInfo::getDistance));
        return R.ok(nearbyPharmacies);
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double EARTH_RADIUS = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    /**
     * 查询本月订单数量排行
     *
     * @return 结果
     */
    @GetMapping("/order/rank/{type}")
    public R selectOrderRank(@PathVariable("type") Integer type) {
        return R.ok(pharmacyInfoService.selectOrderRank(type));
    }

    /**
     * 查询药店评价排名
     *
     * @return 结果
     */
    @GetMapping("/evaluate/rank")
    public R selectPharmacyEvaluateRank() {
        return R.ok(pharmacyInfoService.selectPharmacyEvaluateRank());
    }

    /**
     * 统计数据查询
     *
     * @return 结果
     */
    @GetMapping("/selectOrderDays")
    public R selectOrderDays() {
        // todo 接口有问题
        return R.ok(new HashMap<String, Object>(16) {
            {
                put("orderPriceDays", pharmacyInfoService.selectOrderPriceDays());
                put("orderNumDays", pharmacyInfoService.selectOrderNumDays());
            }
        });
    }

    /**
     * 根据账户获取商家信息
     *
     * @param userId 账户ID
     * @return 结果
     */
    @GetMapping("/getMerchantByUser")
    public R getMerchantByUser(Integer userId) {
        return R.ok(pharmacyInfoService.getOne(Wrappers.<PharmacyInfo>lambdaQuery().eq(PharmacyInfo::getUserId, userId)));
    }

    /**
     * 统计数据
     *
     * @return 结果
     */
    @GetMapping("/home/data")
    public R selectHomeData(@RequestParam(value = "userId", required = false) Integer userId) {
        return R.ok(pharmacyInfoService.homeData(userId));
    }

    /**
     * 根据月份获取药品统计情况
     *
     * @param date 日期
     * @return 结果
     */
    @GetMapping("/selectStatisticsByMonth")
    public R selectStatisticsByMonth(@RequestParam("date") String date) throws FebsException {
        return R.ok(pharmacyInfoService.selectStatisticsByMonth(date));
    }

    /**
     * 查询药店评价信息
     *
     * @param pharmacyId 药店ID
     * @return 结果
     */
    @GetMapping("/evaluate/code/{pharmacyId}")
    public R selectPharmacyEvaluateByCode(@PathVariable("pharmacyId") Integer pharmacyId) {
        return R.ok(pharmacyInfoService.selectPharmacyEvaluateByCode(pharmacyId));
    }

    /**
     * 获取药店情况
     *
     * @return 结果
     */
    @GetMapping("/statistics")
    public R selectOrderNumByPharmacy() {
        return R.ok(pharmacyInfoService.selectOrderNumByPharmacy());
    }

    /**
     * 获取详情信息
     *
     * @param id id
     * @return 结果
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(pharmacyInfoService.getById(id));
    }

    /**
     * 获取信息列表
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(pharmacyInfoService.list());
    }

    /**
     * 新增药店信息
     *
     * @param pharmacyInfo 药店信息
     * @return 结果
     */
    @PostMapping
    public R save(PharmacyInfo pharmacyInfo) throws Exception {
        pharmacyInfo.setCode("PM-" + System.currentTimeMillis());
        pharmacyInfo.setName(StrUtil.cleanBlank(pharmacyInfo.getName()));
        pharmacyInfo.setCreateDate(DateUtil.formatDateTime(new Date()));
        userService.registPharmacy(pharmacyInfo.getCode(), "1234qwer", pharmacyInfo);
        return R.ok(true);
    }

    /**
     * 修改药店信息
     *
     * @param pharmacyInfo 药店信息
     * @return 结果
     */
    @PutMapping
    public R edit(PharmacyInfo pharmacyInfo) {
        return R.ok(pharmacyInfoService.updateById(pharmacyInfo));
    }

    /**
     * 删除药店信息
     *
     * @param ids ids
     * @return 药店信息
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(pharmacyInfoService.removeByIds(ids));
    }

}
