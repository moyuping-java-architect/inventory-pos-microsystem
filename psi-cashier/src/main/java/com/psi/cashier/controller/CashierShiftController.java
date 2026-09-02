package com.psi.cashier.controller;

import com.psi.cashier.entity.CashierShiftEntity;
import com.psi.cashier.entity.CashierShiftPayEntity;
import com.psi.cashier.service.CashierShiftService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 班次结算控制�?
 * 提供班次结算相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/shift")
@RequiredArgsConstructor
public class CashierShiftController {

    private final CashierShiftService cashierShiftService;

    /**
     * 创建班次结算（下班结账）
     */
    @PostMapping("/create")
    public CommonResult<CashierShiftEntity> createShift(@RequestBody Map<String, Object> params) {
        try {
            Integer operatorId = ((Number) params.get("operatorId")).intValue();
            String operatorName = (String) params.get("operatorName");
            BigDecimal cashBegin = params.get("cashBegin") != null ? 
                    new BigDecimal(params.get("cashBegin").toString()) : BigDecimal.ZERO;
            
            CashierShiftEntity shift = cashierShiftService.createShift(operatorId, operatorName, cashBegin);
            return CommonResult.success(shift);
        } catch (Exception e) {
            log.error("创建班次结算失败", e);
            return CommonResult.fail(e.getMessage());
        }
    }

    /**
     * 根据班次单号查询
     */
    @GetMapping("/{shiftNo}")
    public CommonResult<CashierShiftEntity> getByShiftNo(@PathVariable String shiftNo) {
        CashierShiftEntity shift = cashierShiftService.getByShiftNo(shiftNo);
        if (shift == null) {
            return CommonResult.fail("班次记录不存在");
        }
        return CommonResult.success(shift);
    }

    /**
     * 根据收银员ID查询班次记录
     */
    @GetMapping("/operator/{operatorId}")
    public CommonResult<List<CashierShiftEntity>> getByOperatorId(@PathVariable Integer operatorId) {
        List<CashierShiftEntity> shifts = cashierShiftService.getByOperatorId(operatorId);
        return CommonResult.success(shifts);
    }

    /**
     * 根据日期查询班次记录
     */
    @GetMapping("/date/{dateStr}")
    public CommonResult<List<CashierShiftEntity>> getByDate(@PathVariable String dateStr) {
        List<CashierShiftEntity> shifts = cashierShiftService.getByDate(dateStr);
        return CommonResult.success(shifts);
    }

    /**
     * 分页查询班次记录
     */
    @GetMapping("/page")
    public CommonResult<PageResult<CashierShiftEntity>> queryPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer operatorId,
            @RequestParam(required = false) Integer status) {
        PageResult<CashierShiftEntity> page = cashierShiftService.queryPage(pageNum, pageSize, operatorId, status);
        return CommonResult.success(page);
    }

    /**
     * 更新班次结算（录入实际现金金额）
     */
    @PutMapping("/update")
    public CommonResult<Boolean> updateShift(@RequestBody CashierShiftEntity entity) {
        try {
            CashierShiftEntity existing = cashierShiftService.getByShiftNo(entity.getShiftNo());
            if (existing == null) {
                return CommonResult.fail("班次记录不存在");
            }
            
            // 更新实际现金金额和差�?
            if (entity.getCashReality() != null) {
                existing.setCashReality(entity.getCashReality());
                BigDecimal cashDiff = existing.getCashEnd().subtract(entity.getCashReality());
                existing.setCashDiff(cashDiff);
                
                // 根据差异设置状�?
                if (cashDiff.abs().compareTo(new BigDecimal("0.01")) > 0) {
                    existing.setStatus(2); // 有差异待处理
                } else {
                    existing.setStatus(1); // 已完�?
                }
            }
            
            if (entity.getRemark() != null) {
                existing.setRemark(entity.getRemark());
            }
            
            boolean success = cashierShiftService.update(existing);
            return CommonResult.success(success);
        } catch (Exception e) {
            log.error("更新班次结算失败", e);
            return CommonResult.fail(e.getMessage());
        }
    }

    /**
     * 确认班次结算
     */
    @PostMapping("/confirm/{shiftNo}")
    public CommonResult<Boolean> confirmShift(@PathVariable String shiftNo) {
        try {
            boolean success = cashierShiftService.confirmShift(shiftNo);
            if (!success) {
                return CommonResult.fail("确认失败，班次记录不存在");
            }
            return CommonResult.success(true);
        } catch (Exception e) {
            log.error("确认班次结算失败", e);
            return CommonResult.fail(e.getMessage());
        }
    }

    /**
     * 删除班次结算
     */
    @DeleteMapping("/{shiftNo}")
    public CommonResult<Boolean> deleteShift(@PathVariable String shiftNo) {
        try {
            boolean success = cashierShiftService.delete(shiftNo);
            if (!success) {
                return CommonResult.fail("删除失败，班次记录不存在");
            }
            return CommonResult.success(true);
        } catch (Exception e) {
            log.error("删除班次结算失败", e);
            return CommonResult.fail(e.getMessage());
        }
    }

    /**
     * 获取班次支付明细
     */
    @GetMapping("/{shiftNo}/pay")
    public CommonResult<List<CashierShiftPayEntity>> getShiftPayList(@PathVariable String shiftNo) {
        List<CashierShiftPayEntity> payList = cashierShiftService.getShiftPayList(shiftNo);
        return CommonResult.success(payList);
    }

    /**
     * 检查收银员是否有未完成的班�?
     */
    @GetMapping("/check-unfinished/{operatorId}")
    public CommonResult<Boolean> checkUnfinishedShift(@PathVariable Integer operatorId) {
        boolean hasUnfinished = cashierShiftService.hasUnfinishedShift(operatorId);
        return CommonResult.success(hasUnfinished);
    }

    /**
     * 获取收银员最新的班次记录
     */
    @GetMapping("/last/{operatorId}")
    public CommonResult<CashierShiftEntity> getLastShift(@PathVariable Integer operatorId) {
        CashierShiftEntity shift = cashierShiftService.getLastShift(operatorId);
        return CommonResult.success(shift);
    }

    /**
     * 计算班次统计预览（未保存）
     */
    @PostMapping("/preview")
    public CommonResult<Map<String, Object>> previewShift(@RequestBody Map<String, Object> params) {
        try {
            Integer operatorId = ((Number) params.get("operatorId")).intValue();
            String beginTime = (String) params.get("beginTime");
            String endTime = (String) params.get("endTime");
            
            Map<String, Object> shiftData = cashierShiftService.calculateShiftData(operatorId, beginTime, endTime);
            return CommonResult.success(shiftData);
        } catch (Exception e) {
            log.error("计算班次统计失败", e);
            return CommonResult.fail(e.getMessage());
        }
    }
    
    /**
     * 获取班次统计预览（下班结账前预览，自动计算时间范围）
     */
    @GetMapping("/preview/{operatorId}")
    public CommonResult<Map<String, Object>> getShiftPreview(@PathVariable Integer operatorId) {
        try {
            // 获取上次班次结束时间作为本次开始时间
            // 如果没有历史班次，使用当天的开始时间（08:00:00）作为默认上班时间
            CashierShiftEntity lastShift = cashierShiftService.getLastShift(operatorId);
            String beginTime;
            String endTime = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            if (lastShift != null) {
                beginTime = lastShift.getEndTime();
            } else {
                // 没有历史班次，使用当天 08:00:00 作为开始时间
                beginTime = java.time.LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 08:00:00";
            }
            
            Map<String, Object> shiftData = cashierShiftService.calculateShiftData(operatorId, beginTime, endTime);
            return CommonResult.success(shiftData);
        } catch (Exception e) {
            log.error("获取班次统计预览失败", e);
            return CommonResult.fail(e.getMessage());
        }
    }
}