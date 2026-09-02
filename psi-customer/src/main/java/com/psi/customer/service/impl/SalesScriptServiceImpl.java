package com.psi.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psi.common.result.CommonResult;
import com.psi.customer.dto.*;
import com.psi.customer.entity.*;
import com.psi.customer.mapper.*;
import com.psi.customer.service.SalesScriptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 话术库 * 销冠 服务实现（旅程触达计划版）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesScriptServiceImpl implements SalesScriptService {

    private final SalesScriptLibraryMapper scriptMapper;
    private final CustomerScriptSendLogMapper sendLogMapper;
    private final CustomerTagMapper tagMapper;
    private final CustomerTagRelMapper tagRelMapper;
    private final CustomerJourneyMapper journeyMapper;
    private final CustomerJourneyStateMapper stateMapper;
    private final CustomerJourneyStageMapper stageMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_TENANT = "1";

    // ========== 话术库 CRUD ==========

    @Override
    public CommonResult<List<SalesScriptLibraryDTO>> listScripts(String stageCode) {
        LambdaQueryWrapper<SalesScriptLibraryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesScriptLibraryEntity::getTenantId, DEFAULT_TENANT)
                .eq(SalesScriptLibraryEntity::getDelFlag, 0)
                .orderByAsc(SalesScriptLibraryEntity::getStageCode,
                        SalesScriptLibraryEntity::getDayStart,
                        SalesScriptLibraryEntity::getPriority);
        if (StringUtils.hasText(stageCode)) {
            wrapper.eq(SalesScriptLibraryEntity::getStageCode, stageCode);
        }
        List<SalesScriptLibraryEntity> list = scriptMapper.selectList(wrapper);
        return CommonResult.success(list.stream().map(this::toScriptDto).collect(Collectors.toList()));
    }

    @Override
    public CommonResult<SalesScriptLibraryDTO> getScript(Long id) {
        SalesScriptLibraryEntity entity = scriptMapper.selectById(id);
        if (entity == null || entity.getDelFlag() != 0) {
            return CommonResult.fail("话术不存在");
        }
        return CommonResult.success(toScriptDto(entity));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Long> saveScript(SalesScriptLibraryDTO dto) {
        SalesScriptLibraryEntity entity = toScriptEntity(dto);
        entity.setTenantId(DEFAULT_TENANT);
        entity.setDelFlag(0);
        String now = LocalDateTime.now().format(TIME_FORMATTER);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        scriptMapper.insert(entity);
        return CommonResult.success(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> updateScript(SalesScriptLibraryDTO dto) {
        if (dto.getId() == null) {
            return CommonResult.fail("ID 不能为空");
        }
        SalesScriptLibraryEntity entity = toScriptEntity(dto);
        entity.setUpdateTime(LocalDateTime.now().format(TIME_FORMATTER));
        scriptMapper.updateById(entity);
        return CommonResult.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> deleteScript(Long id) {
        SalesScriptLibraryEntity entity = new SalesScriptLibraryEntity();
        entity.setId(id);
        entity.setDelFlag(1);
        entity.setUpdateTime(LocalDateTime.now().format(TIME_FORMATTER));
        scriptMapper.updateById(entity);
        return CommonResult.success();
    }

    // ========== 客户触达 ==========

    @Override
    public CommonResult<List<ScriptMatchCustomerDTO>> listMatchCustomers(Integer limit) {
        List<MemberSummaryDTO> summaries = journeyMapper.selectCustomerOrderSummaries();
        if (CollectionUtils.isEmpty(summaries)) {
            return CommonResult.success(Collections.emptyList());
        }
        int max = (limit == null || limit <= 0) ? 50 : limit;
        List<ScriptMatchCustomerDTO> result = new ArrayList<>();
        for (MemberSummaryDTO summary : summaries.stream().limit(max).collect(Collectors.toList())) {
            ScriptMatchCustomerDTO customer = buildCustomerCard(summary);
            if (customer == null) {
                continue;
            }
            List<ScriptMatchResultDTO> today = findTodayScripts(customer.getCustomerId(),
                    customer.getStageCode(), customer.getDaysInStage());
            customer.setTodayScripts(today);
            customer.setHasPending(today.stream().anyMatch(ScriptMatchResultDTO::getCanSend));
            result.add(customer);
        }
        // 有待发送的排在前面
        result.sort((a, b) -> Boolean.compare(b.getHasPending(), a.getHasPending()));
        return CommonResult.success(result);
    }

    @Override
    public CommonResult<List<ScriptMatchResultDTO>> matchScriptsForCustomer(Long customerId) {
        if (customerId == null) {
            return CommonResult.fail("customerId 不能为空");
        }
        CustomerJourneyStateEntity state = getLatestState(customerId);
        String stageCode = state == null ? null : state.getCurrentStageCode();
        Integer daysInStage = calcDaysInStage(state);
        return CommonResult.success(findTodayScripts(customerId, stageCode, daysInStage));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> sendScript(Long customerId, Long scriptId, Integer dayInStage) {
        SalesScriptLibraryEntity script = scriptMapper.selectById(scriptId);
        if (script == null || script.getDelFlag() != 0) {
            return CommonResult.fail("话术不存在");
        }
        CustomerJourneyStateEntity state = getLatestState(customerId);
        String stageCode = state == null ? null : state.getCurrentStageCode();
        Integer actualDay = dayInStage != null ? dayInStage : calcDaysInStage(state);
        if (actualDay == null) {
            actualDay = 1;
        }

        // 幂等：同一客户同节点同天同话术只能有一条成功记录
        LambdaQueryWrapper<CustomerScriptSendLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerScriptSendLogEntity::getTenantId, DEFAULT_TENANT)
                .eq(CustomerScriptSendLogEntity::getCustomerId, customerId)
                .eq(CustomerScriptSendLogEntity::getScriptId, scriptId)
                .eq(CustomerScriptSendLogEntity::getStageCode, stageCode)
                .eq(CustomerScriptSendLogEntity::getDayInStage, actualDay)
                .eq(CustomerScriptSendLogEntity::getSendStatus, 1);
        Long count = sendLogMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            return CommonResult.fail("该话术已发送过，请勿重复发送");
        }

        String now = LocalDateTime.now().format(TIME_FORMATTER);
        CustomerScriptSendLogEntity logEntity = new CustomerScriptSendLogEntity();
        logEntity.setTenantId(DEFAULT_TENANT);
        logEntity.setCustomerId(customerId);
        logEntity.setScriptId(scriptId);
        logEntity.setStageCode(stageCode);
        logEntity.setDayInStage(actualDay);
        logEntity.setChannel(script.getChannel());
        logEntity.setSendStatus(1);
        logEntity.setSendTime(now);
        logEntity.setCreateTime(now);
        logEntity.setUpdateTime(now);
        logEntity.setDelFlag(0);
        sendLogMapper.insert(logEntity);
        return CommonResult.success();
    }

    @Override
    public CommonResult<List<CustomerScriptSendLogDTO>> listSendLogs(Long customerId) {
        if (customerId == null) {
            return CommonResult.fail("customerId 不能为空");
        }
        List<CustomerScriptSendLogEntity> list = sendLogMapper.selectByCustomer(DEFAULT_TENANT, customerId);
        return CommonResult.success(list.stream().map(this::toSendLogDto).collect(Collectors.toList()));
    }

    // ========== 标签 ==========

    @Override
    public CommonResult<List<CustomerTagDTO>> listTags() {
        LambdaQueryWrapper<CustomerTagEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerTagEntity::getTenantId, DEFAULT_TENANT)
                .eq(CustomerTagEntity::getDelFlag, 0)
                .orderByAsc(CustomerTagEntity::getSort, CustomerTagEntity::getId);
        List<CustomerTagEntity> list = tagMapper.selectList(wrapper);
        return CommonResult.success(list.stream().map(this::toTagDto).collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> saveCustomerTags(Long customerId, List<String> tagCodes) {
        if (customerId == null) {
            return CommonResult.fail("customerId 不能为空");
        }
        LambdaQueryWrapper<CustomerTagRelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerTagRelEntity::getTenantId, DEFAULT_TENANT)
                .eq(CustomerTagRelEntity::getCustomerId, customerId);
        tagRelMapper.delete(wrapper);
        if (!CollectionUtils.isEmpty(tagCodes)) {
            String now = LocalDateTime.now().format(TIME_FORMATTER);
            Set<String> unique = new LinkedHashSet<>(tagCodes);
            for (String code : unique) {
                CustomerTagRelEntity rel = new CustomerTagRelEntity();
                rel.setTenantId(DEFAULT_TENANT);
                rel.setCustomerId(customerId);
                rel.setTagCode(code);
                rel.setCreateTime(now);
                rel.setUpdateTime(now);
                rel.setDelFlag(0);
                tagRelMapper.insert(rel);
            }
        }
        return CommonResult.success();
    }

    // ========== 内部方法 ==========

    private CustomerJourneyStateEntity getLatestState(Long customerId) {
        return stateMapper.selectOne(
                new LambdaQueryWrapper<CustomerJourneyStateEntity>()
                        .eq(CustomerJourneyStateEntity::getTenantId, DEFAULT_TENANT)
                        .eq(CustomerJourneyStateEntity::getSubjectType, "CUSTOMER")
                        .eq(CustomerJourneyStateEntity::getSubjectId, customerId)
                        .orderByDesc(CustomerJourneyStateEntity::getId)
                        .last("LIMIT 1"));
    }

    private ScriptMatchCustomerDTO buildCustomerCard(MemberSummaryDTO summary) {
        if (summary.getMemberId() == null) {
            return null;
        }
        Long customerId = summary.getMemberId().longValue();
        CustomerJourneyStateEntity state = getLatestState(customerId);
        String stageCode = state == null ? null : state.getCurrentStageCode();
        String stageName = stageName(stageCode);
        Integer daysInStage = calcDaysInStage(state);

        ScriptMatchCustomerDTO dto = new ScriptMatchCustomerDTO();
        dto.setCustomerId(customerId);
        dto.setCustomerName(summary.getMemberName());
        dto.setAvatar(avatarOf(summary.getMemberName()));
        dto.setChannel("WHATSAPP");
        dto.setContact(summary.getPhone());
        dto.setStageCode(stageCode);
        dto.setStageName(stageName);
        dto.setDaysInStage(daysInStage);
        dto.setTags(tagMapper.selectTagsByCustomerId(DEFAULT_TENANT, customerId).stream()
                .map(this::toTagDto).collect(Collectors.toList()));
        dto.setLastInteractText(lastInteractText(summary.getLastOrderTime()));
        return dto;
    }

    /**
     * 查找客户当前节点+天数下可触达的话术
     */
    private List<ScriptMatchResultDTO> findTodayScripts(Long customerId, String stageCode, Integer daysInStage) {
        if (customerId == null || !StringUtils.hasText(stageCode) || daysInStage == null) {
            return Collections.emptyList();
        }
        List<SalesScriptLibraryEntity> scripts = scriptMapper.selectByStageAndDay(DEFAULT_TENANT, stageCode, daysInStage);
        if (CollectionUtils.isEmpty(scripts)) {
            return Collections.emptyList();
        }
        Set<String> customerTagCodes = new HashSet<>(tagMapper.selectTagCodesByCustomerId(DEFAULT_TENANT, customerId));
        List<Long> sentIds = sendLogMapper.selectSentScriptIds(DEFAULT_TENANT, customerId, stageCode, daysInStage);
        Set<Long> sentIdSet = new HashSet<>(sentIds);

        List<ScriptMatchResultDTO> result = new ArrayList<>();
        for (SalesScriptLibraryEntity script : scripts) {
            ScriptMatchResultDTO dto = evaluateScript(script, customerId, customerTagCodes, sentIdSet, daysInStage);
            result.add(dto);
        }
        // 可发送的优先，再按优先级和天数排序
        result.sort((a, b) -> {
            int c = Boolean.compare(b.getCanSend(), a.getCanSend());
            if (c != 0) return c;
            c = Integer.compare(b.getPriority(), a.getPriority());
            if (c != 0) return c;
            return Integer.compare(a.getDayStart(), b.getDayStart());
        });
        return result;
    }

    private ScriptMatchResultDTO evaluateScript(SalesScriptLibraryEntity script, Long customerId,
                                                Set<String> customerTagCodes, Set<Long> sentIdSet,
                                                Integer daysInStage) {
        ScriptMatchResultDTO dto = new ScriptMatchResultDTO();
        dto.setScriptId(script.getId());
        dto.setScriptCode(script.getScriptCode());
        dto.setScriptName(script.getScriptName());
        dto.setCategory(script.getCategory());
        dto.setContent(script.getContent());
        dto.setRenderedContent(renderScript(script.getContent(), customerId));
        dto.setChannel(script.getChannel());
        dto.setStageCode(script.getStageCode());
        dto.setStageName(stageName(script.getStageCode()));
        dto.setDayStart(script.getDayStart());
        dto.setDayEnd(script.getDayEnd());
        dto.setPriority(script.getPriority());

        boolean alreadySent = sentIdSet.contains(script.getId());
        dto.setAlreadySent(alreadySent);

        // 标签匹配
        List<String> scriptTagCodes = parseTags(script.getTags());
        List<CustomerTagDTO> matchedTagDtos = new ArrayList<>();
        boolean tagMatch;
        if (scriptTagCodes.isEmpty()) {
            tagMatch = true;
        } else {
            int matched = 0;
            for (String code : scriptTagCodes) {
                if (customerTagCodes.contains(code)) {
                    matched++;
                    CustomerTagEntity tag = tagMapper.selectByCode(DEFAULT_TENANT, code);
                    if (tag != null) {
                        matchedTagDtos.add(toTagDto(tag));
                    }
                }
            }
            if ("ALL".equalsIgnoreCase(script.getTagMatchMode())) {
                tagMatch = matched == scriptTagCodes.size();
            } else {
                tagMatch = matched > 0;
            }
        }
        dto.setMatchedTags(matchedTagDtos);

        if (alreadySent) {
            dto.setCanSend(false);
            dto.setBlockReason("该话术已发送过");
        } else if (!tagMatch) {
            dto.setCanSend(false);
            dto.setBlockReason("标签未命中");
        } else {
            dto.setCanSend(true);
            dto.setBlockReason(null);
        }
        return dto;
    }

    private String renderScript(String content, Long customerId) {
        if (!StringUtils.hasText(content)) {
            return content;
        }
        String rendered = content;
        rendered = rendered.replace("{{name}}", "John");
        rendered = rendered.replace("{{school}}", "学校");
        rendered = rendered.replace("{{topic}}", "采购方案");
        String product = journeyMapper.selectLastProductByCustomer(customerId);
        rendered = rendered.replace("{{product}}", product == null ? "产品" : product);
        return rendered;
    }

    private String stageName(String stageCode) {
        if (!StringUtils.hasText(stageCode)) {
            return "";
        }
        CustomerJourneyStageEntity stage = stageMapper.selectOne(
                new LambdaQueryWrapper<CustomerJourneyStageEntity>()
                        .eq(CustomerJourneyStageEntity::getTenantId, DEFAULT_TENANT)
                        .eq(CustomerJourneyStageEntity::getStageCode, stageCode)
                        .last("LIMIT 1"));
        return stage == null ? stageCode : stage.getStageName();
    }

    private Integer calcDaysInStage(CustomerJourneyStateEntity state) {
        if (state == null || !StringUtils.hasText(state.getEnterStageTime())) {
            return null;
        }
        try {
            LocalDateTime enter = LocalDateTime.parse(state.getEnterStageTime(), TIME_FORMATTER);
            return (int) ChronoUnit.DAYS.between(enter.toLocalDate(), LocalDate.now());
        } catch (Exception e) {
            return null;
        }
    }

    private String lastInteractText(String lastOrderTime) {
        if (!StringUtils.hasText(lastOrderTime)) {
            return "暂无互动";
        }
        try {
            LocalDateTime t = LocalDateTime.parse(lastOrderTime, TIME_FORMATTER);
            long days = ChronoUnit.DAYS.between(t.toLocalDate(), LocalDate.now());
            if (days <= 0) {
                return "今天";
            }
            return days + " 天前";
        } catch (Exception e) {
            return "未知";
        }
    }

    private String avatarOf(String name) {
        if (!StringUtils.hasText(name)) {
            return "?";
        }
        String[] arr = name.trim().split("\\s+");
        if (arr.length == 1) {
            return name.substring(0, Math.min(1, name.length())).toUpperCase();
        }
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            if (StringUtils.hasText(s)) {
                sb.append(Character.toUpperCase(s.charAt(0)));
            }
            if (sb.length() >= 2) {
                break;
            }
        }
        return sb.toString();
    }

    // ========== 转换方法 ==========

    private SalesScriptLibraryDTO toScriptDto(SalesScriptLibraryEntity e) {
        SalesScriptLibraryDTO dto = new SalesScriptLibraryDTO();
        dto.setId(e.getId());
        dto.setTenantId(e.getTenantId());
        dto.setScriptCode(e.getScriptCode());
        dto.setScriptName(e.getScriptName());
        dto.setCategory(e.getCategory());
        dto.setContent(e.getContent());
        dto.setChannel(e.getChannel());
        dto.setStageCode(e.getStageCode());
        dto.setStageName(stageName(e.getStageCode()));
        dto.setTags(parseTags(e.getTags()));
        dto.setTagList(listTagDtos(e.getTags()));
        dto.setTagMatchMode(e.getTagMatchMode());
        dto.setDayStart(e.getDayStart());
        dto.setDayEnd(e.getDayEnd());
        dto.setPriority(e.getPriority());
        dto.setSendLimit(e.getSendLimit());
        dto.setIsEnabled(e.getIsEnabled());
        return dto;
    }

    private List<String> parseTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    private List<CustomerTagDTO> listTagDtos(String tags) {
        List<String> codes = parseTags(tags);
        if (codes.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<CustomerTagEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerTagEntity::getTenantId, DEFAULT_TENANT)
                .eq(CustomerTagEntity::getDelFlag, 0)
                .in(CustomerTagEntity::getTagCode, codes);
        return tagMapper.selectList(wrapper).stream()
                .map(this::toTagDto)
                .collect(Collectors.toList());
    }

    private SalesScriptLibraryEntity toScriptEntity(SalesScriptLibraryDTO dto) {
        SalesScriptLibraryEntity e = new SalesScriptLibraryEntity();
        e.setId(dto.getId());
        e.setScriptCode(dto.getScriptCode());
        e.setScriptName(dto.getScriptName());
        e.setCategory(dto.getCategory());
        e.setContent(dto.getContent());
        e.setChannel(dto.getChannel());
        e.setStageCode(dto.getStageCode());
        e.setStageName(stageName(dto.getStageCode()));
        e.setTags(dto.getTags() == null ? null : String.join(",", dto.getTags()));
        e.setTagMatchMode(dto.getTagMatchMode());
        e.setDayStart(dto.getDayStart());
        e.setDayEnd(dto.getDayEnd());
        e.setPriority(dto.getPriority());
        e.setSendLimit(dto.getSendLimit());
        e.setIsEnabled(dto.getIsEnabled());
        return e;
    }

    private CustomerScriptSendLogDTO toSendLogDto(CustomerScriptSendLogEntity e) {
        CustomerScriptSendLogDTO dto = new CustomerScriptSendLogDTO();
        dto.setId(e.getId());
        dto.setTenantId(e.getTenantId());
        dto.setCustomerId(e.getCustomerId());
        dto.setScriptId(e.getScriptId());
        dto.setStageCode(e.getStageCode());
        dto.setDayInStage(e.getDayInStage());
        dto.setChannel(e.getChannel());
        dto.setSendStatus(e.getSendStatus());
        dto.setSendTime(e.getSendTime());
        dto.setOperatorName(e.getOperatorName());
        dto.setRemark(e.getRemark());
        SalesScriptLibraryEntity script = scriptMapper.selectById(e.getScriptId());
        dto.setScriptName(script == null ? "" : script.getScriptName());
        return dto;
    }

    private CustomerTagDTO toTagDto(CustomerTagEntity e) {
        CustomerTagDTO dto = new CustomerTagDTO();
        dto.setId(e.getId());
        dto.setTenantId(e.getTenantId());
        dto.setTagCode(e.getTagCode());
        dto.setTagName(e.getTagName());
        dto.setCategory(e.getCategory());
        dto.setColor(e.getColor());
        dto.setSort(e.getSort());
        return dto;
    }
}
