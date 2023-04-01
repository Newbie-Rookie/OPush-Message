package com.lin.opush.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.constants.OpushConstant;
import com.lin.opush.dao.MessageTemplateDao;
import com.lin.opush.dao.SmsRecordDao;
import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.SimpleAnchorInfo;
import com.lin.opush.domain.SmsRecord;
import com.lin.opush.enums.AnchorState;
import com.lin.opush.enums.ChannelType;
import com.lin.opush.service.DataTraceService;
import com.lin.opush.utils.Convert4Amis;
import com.lin.opush.utils.RedisUtils;
import com.lin.opush.utils.TaskInfoUtils;
import com.lin.opush.vo.DataTraceParam;
import com.lin.opush.vo.amis.EchartsDataVo;
import com.lin.opush.vo.amis.UserDataVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据全链路追踪服务实现类
 */
@Service
public class DataTraceServiceImpl implements DataTraceService {
    /**
     * Redis工具类
     */
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 消息模板 Dao
     */
    @Autowired
    private MessageTemplateDao messageTemplateDao;

    /**
     * 短信记录 Dao
     */
    @Autowired
    private SmsRecordDao smsRecordDao;

    /**
     * 获取短信下发记录列表
     * @param dataTraceParam 数据全链路追踪请求参数
     * @return 分页短信下发记录列表对应VO
     */
    @Override
    public Page<SmsRecord> querySmsDataTraceList(DataTraceParam dataTraceParam){
        // 判断页码和是否合理（page > 0, perPage >= 1）并生成分页请求对象【由于展示到前端为两条记录合并为一条，分页条数需乘2】
        PageRequest pageRequest = PageRequest.of(dataTraceParam.getPage() - 1, dataTraceParam.getPerPage() * 2);
        // root: 代表查询的根对象，即SmsRecord（短信下发记录）
        // criteriaQuery: 顶层查询对象，sql语句关键字，用于自定义查询方式（基本不用）
        // criteriaBuilder: 查询构造器，封装很多的查询条件
        return smsRecordDao.findAll((Specification<SmsRecord>) (root, criteriaQuery, criteriaBuilder) -> {
            // 搜索条件列表
            List<Predicate> predicateList = new ArrayList<>();
            // 添加搜索条件（接收者、下发时间、记录创建者）
            if (StrUtil.isNotBlank(dataTraceParam.getReceiver())) {
                predicateList.add(criteriaBuilder.like(root.get("phone").as(String.class), "%" + dataTraceParam.getReceiver() + "%"));
            }
            if (ObjectUtil.isNotNull(dataTraceParam.getDateTime())) {
                // 查询短信下发记录时间点【yyyyMMdd，查询用户指定时间的该天】
                Integer sendDate = Integer.valueOf(DateUtil.format(new Date(dataTraceParam.getDateTime() * 1000L), DatePattern.PURE_DATE_PATTERN));
                predicateList.add(criteriaBuilder.greaterThanOrEqualTo(root.get("sendDate").as(Integer.class), sendDate));
            }
            predicateList.add(criteriaBuilder.equal(root.get("creator").as(String.class), dataTraceParam.getCreator()));
            Predicate[] predicate = new Predicate[predicateList.size()];
            criteriaQuery.where(criteriaBuilder.and(predicateList.toArray(predicate)));
            // 查询内容按更新时间排序
            criteriaQuery.orderBy(criteriaBuilder.desc(root.get("created")));
            // 返回与where子句限制相对应的predicates，如果未指定限制，则返回null
            return criteriaQuery.getRestriction();
        }, pageRequest);
    }

    /**
     * 获取全链路追踪 用户维度信息【Redis中使用List存储】
     * @param dataTraceParam 数据全链路追踪请求参数
     * @return 用户链路追踪VO
     */
    @Override
    public UserDataVo getTraceUserInfo(DataTraceParam dataTraceParam) {
        // Redis key
        String key = dataTraceParam.getCreator() + CommonConstant.COLON + dataTraceParam.getReceiver();
        // 获取Redis中指定key的所有数据
        List<String> simpleAnchorInfoList = redisUtils.lRange(key, 0, -1);
        if (CollUtil.isEmpty(simpleAnchorInfoList)) {
            return UserDataVo.builder().items(new ArrayList<>()).build();
        }
        // 按时间排序
        List<SimpleAnchorInfo> sortSimpleAnchorInfoList = simpleAnchorInfoList.stream()
                .map(simpleAnchorInfo -> JSON.parseObject(simpleAnchorInfo, SimpleAnchorInfo.class))
                .sorted((o1, o2) -> Math.toIntExact(o1.getLogTimestamp() - o2.getLogTimestamp()))
                .collect(Collectors.toList());
        // 对埋点信息中相同的businessId进行分类
        // {"businessId":[{businessId,state,timeStamp},{businessId,state,timeStamp}]}
        Map<String, List<SimpleAnchorInfo>> map = MapUtil.newHashMap();
        for (SimpleAnchorInfo simpleAnchorInfo : sortSimpleAnchorInfoList) {
            List<SimpleAnchorInfo> simpleAnchorInfos = map.get(String.valueOf(simpleAnchorInfo.getBusinessId()));
            if (CollUtil.isEmpty(simpleAnchorInfos)) {
                simpleAnchorInfos = new ArrayList<>();
            }
            simpleAnchorInfos.add(simpleAnchorInfo);
            map.put(String.valueOf(simpleAnchorInfo.getBusinessId()), simpleAnchorInfos);
        }
        // 封装vo给到前端渲染展示
        List<UserDataVo.ItemsVO> items = new ArrayList<>();
        for (Map.Entry<String, List<SimpleAnchorInfo>> entry : map.entrySet()) {
            // 从业务id【3 ~ 8位】中切割出消息模板id并获取对应模板信息
            Long messageTemplateId = TaskInfoUtils.getMessageTemplateIdFromBusinessId(Long.valueOf(entry.getKey()));
            MessageTemplate messageTemplate = messageTemplateDao.findById(messageTemplateId).orElse(null);
            if (Objects.isNull(messageTemplate)) {
                continue;
            }
            // 拼接发送细节
            StringBuilder sb = new StringBuilder();
            for (SimpleAnchorInfo simpleAnchorInfo : entry.getValue()) {
                if (AnchorState.RECEIVE.getCode().equals(simpleAnchorInfo.getState())) {
                    sb.append(CommonConstant.CRLF);
                }
                // 日志产生时间转为yyyy-MM-dd HH:mm:ss格式，「埋点描述信息」，各埋点信息用" ➢ "连接
                sb.append(DateUtil.format(new Date(simpleAnchorInfo.getLogTimestamp()), DatePattern.NORM_DATETIME_PATTERN))
                        .append(CommonConstant.LEFT)
                        .append(AnchorState.getDescriptionByCode(simpleAnchorInfo.getState()))
                        .append(CommonConstant.RIGHT)
                        .append(CommonConstant.JOIN);
            }
            // 组装用户链路追踪VO
            for (String detail : sb.toString().split(CommonConstant.CRLF)) {
                if (StrUtil.isNotBlank(detail)) {
                    UserDataVo.ItemsVO itemsVO = UserDataVo.ItemsVO.builder()
                            .businessId(entry.getKey())
                            .channelType(ChannelType.getEnumByCode(messageTemplate.getSendChannel()).getDescription())
                            .title(messageTemplate.getName())
                            .detail(detail).build();
                    items.add(itemsVO);
                }
            }
        }
        return UserDataVo.builder().items(items).count(Long.valueOf(map.size())).build();
    }

    /**
     * 获取全链路追踪 消息模板维度信息【Redis中使用Hash存储】
     * @param dataTraceParam 数据全链路追踪请求参数
     * @return 图表VO
     */
    @Override
    public EchartsDataVo getTraceMessageTemplateInfo(DataTraceParam dataTraceParam) {
        // 获取businessId并获取模板信息
        String businessId = getRealBusinessId(dataTraceParam.getBusinessId());
        Optional<MessageTemplate> optional = messageTemplateDao.findById(
                TaskInfoUtils.getMessageTemplateIdFromBusinessId(Long.valueOf(businessId)));
        if (!optional.isPresent()) {
            return null;
        }
        /**
         * 获取Redis中清洗好的数据
         *      key : creator:businessId
         *      field : state
         *      value : stateCount
         */
        String key = dataTraceParam.getCreator() + CommonConstant.COLON + getRealBusinessId(businessId);
        Map<Object, Object> anchorResult = redisUtils.hGetAll(key);
        return Convert4Amis.getEchartsVo(anchorResult, optional.get().getName(), businessId);
    }

    /**
     * 若传入的是模板ID，则生成【当天】的businessId进行查询
     * 若传入的是businessId，则按默认的businessId进行查询
     * @param businessId 业务Id
     * @return 业务Id
     */
    private String getRealBusinessId(String businessId) {
        // 判断是否为businessId则判断长度是否为16位【businessId长度固定16】
        if (OpushConstant.BUSINESS_ID_LENGTH == businessId.length()) {
            return businessId;
        }
        // 判断模板id是否存在
        Optional<MessageTemplate> optional = messageTemplateDao.findById(Long.valueOf(businessId));
        if (optional.isPresent()) {
            MessageTemplate messageTemplate = optional.get();
            return String.valueOf(TaskInfoUtils.generateBusinessId(
                    messageTemplate.getId(), messageTemplate.getTemplateType()));
        }
        return businessId;
    }
}
