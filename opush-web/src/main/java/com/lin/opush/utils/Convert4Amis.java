package com.lin.opush.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.lin.opush.domain.ChannelAccount;
import com.lin.opush.domain.SmsRecord;
import com.lin.opush.enums.AnchorState;
import com.lin.opush.enums.ChannelType;
import com.lin.opush.enums.SmsStatus;
import com.lin.opush.vo.amis.CommonAmisVo;
import com.lin.opush.vo.amis.EchartsDataVo;
import com.lin.opush.vo.amis.SmsDataVo;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AMIS在回显【表单】回显时，不支持嵌套动态语法，需编写工具类将List/Object铺平成Map以及相关的格式
 * https://baidu.gitee.io/amis/zh-CN/components/form/index#%E8%A1%A8%E5%8D%95%E9%A1%B9%E6%95%B0%E6%8D%AE%E5%88%9D%E5%A7%8B%E5%8C%96
 */
@Slf4j
public class Convert4Amis {
    /**
     * 标识当前未读到'$'和'{'字符
     */
    public static final int IGNORE_TG = 0;
    /**
     * 标识已读取到'$'字符
     */
    public static final int START_TG = 1;
    /**
     * 标识已读取到'{'字符
     */
    public static final int READ_TG = 2;

    /**
     * 需要打散的字段，将json字符串打散为一个个字段
     * msgContent：消息内容
     */
    private static final List<String> FLAT_FIELD_NAME = Arrays.asList("msgContent");

    /**
     * 需要格式化为jsonArray返回的字段
     * (前端是一个JSONArray传递进来)
     */
    private static final List<String> PARSE_JSON_ARRAY = Arrays.asList("feedCards", "btns", "articles");

    /**
     * 前端是一个JSONObject传递进来，返回一个JSONArray回去
     */
    private static final List<String> PARSE_JSON_OBJ_TO_ARRAY = Arrays.asList("officialAccountParam", "miniProgramParam");

    /**
     * 钉钉工作消息OA实际的映射
     */
    private static final List<String> DING_DING_OA_FIELD = Arrays.asList("dingDingOaHead", "dingDingOaBody");

    /**
     * 钉钉OA字段名实际的映射
     */
    private static final Map<String, String> DING_DING_OA_NAME_MAPPING = new HashMap<>();

    static {
        DING_DING_OA_NAME_MAPPING.put("bgcolor", "dingDingOaHeadBgColor");
        DING_DING_OA_NAME_MAPPING.put("text", "dingDingOaHeadTitle");
        DING_DING_OA_NAME_MAPPING.put("title", "dingDingOaTitle");
        DING_DING_OA_NAME_MAPPING.put("image", "media_id");
        DING_DING_OA_NAME_MAPPING.put("author", "dingDingOaAuthor");
        DING_DING_OA_NAME_MAPPING.put("content", "dingDingOaContent");
    }

    /**
     * 将List对象转换成Map(无嵌套)
     * @param param
     * @return
     */
    public static <T> List<Map<String, Object>> flatListMap(List<T> param) {
        // 将List中每一个对象转为一个map
        List<Map<String, Object>> result = new ArrayList<>();
        for (T t : param) {
            Map<String, Object> map = flatSingleMap(t);
            result.add(map);
        }
        return result;
    }

    /**
     * 将单个对象转换成Map(无嵌套)
     * @param obj
     * @return
     */
    public static Map<String, Object> flatSingleMap(Object obj) {
        // 获取32个大小的map
        Map<String, Object> result = MapUtil.newHashMap(32);
        // 反射获取obj类中的属性对象
        Field[] fields = ReflectUtil.getFields(obj.getClass());
        // 对属性深挖打散
        for (Field field : fields) {
            // 需要打散的字段
            if (FLAT_FIELD_NAME.contains(field.getName())) {
                String fieldValue = (String) ReflectUtil.getFieldValue(obj, field);
                JSONObject jsonObject = JSONObject.parseObject(fieldValue);
                for (String key : jsonObject.keySet()) {
                    // 钉钉OA消息回显
                    if (DING_DING_OA_FIELD.contains(key)) {
                        JSONObject object = jsonObject.getJSONObject(key);
                        for (String objKey : object.keySet()) {
                            result.put(DING_DING_OA_NAME_MAPPING.get(objKey), object.getString(objKey));
                        }
                    } else if (PARSE_JSON_ARRAY.contains(key)) {
                        // 部分字段是直接传入数组，把数组直接返回（用于回显）
                        result.put(key, JSON.parseArray(jsonObject.getString(key)));
                    } else if (PARSE_JSON_OBJ_TO_ARRAY.contains(key)) {
                        // 部分字段是直接传入Obj，把数组直接返回（用于回显）
                        String value = "[" + jsonObject.getString(key) + "]";
                        result.put(key, JSON.parseArray(value));
                    } else {
                        result.put(key, jsonObject.getString(key));
                    }
                }
            }
            result.put(field.getName(), ReflectUtil.getFieldValue(obj, field));
        }
        return result;
    }

    /**
     * 适配amis前端，得到渠道账号信息，返回给前端做展示
     * @return
     */
    public static List<CommonAmisVo> getChannelAccountVo(List<ChannelAccount> channelAccounts, Integer channelType) {
        List<CommonAmisVo> result = new ArrayList<>();
        // 短信和硬件渠道可走系统动态流量配置（channelAccount = 0）
        if (ChannelType.SMS.getCode().equals(channelType) || ChannelType.EMAIL.getCode().equals(channelType)) {
            CommonAmisVo commonAmisVo = CommonAmisVo.builder().label("Auto").value("0").build();
            result.add(commonAmisVo);
        }
        for (ChannelAccount channelAccount : channelAccounts) {
            CommonAmisVo commonAmisVo = CommonAmisVo.builder().label(channelAccount.getName()).value(String.valueOf(channelAccount.getId())).build();
            result.add(commonAmisVo);
        }
        return result;
    }

    /**
     * 适配amis前端，获取占位符的参数
     * @param msgContent 渠道配置内容
     * @return
     */
    public static CommonAmisVo getTestContent(String msgContent) {
        // 获取消息内容中的占位符参数
        Set<String> placeholderList = getPlaceholderList(msgContent);
        if (CollUtil.isEmpty(placeholderList)) {
            return null;
        }
        // 若placeholderList不为null，则说明消息内容存在占位符参数
        // 封装表格（input-table：可增加一行、可编辑、可删除、无需确认操作）
        CommonAmisVo testParam = CommonAmisVo.builder().type("input-table").name("testParam")
                                            .addable(true).editable(true).removable(true).needConfirm(true).build();
        // 封装表格中列集合
        List<CommonAmisVo.ColumnsDTO> columnsDtoS = new ArrayList<>();
        for (String param : placeholderList) {
            // 多列输入框（必填、快速编辑）
            CommonAmisVo.ColumnsDTO dto = CommonAmisVo.ColumnsDTO.builder().name(param).label(param).type("input-text")
                                                                                    .required(true).quickEdit(true).build();
            columnsDtoS.add(dto);
        }
        testParam.setColumns(columnsDtoS);
        return testParam;
    }

    /**
     * 获取占位符的参数（${content}、${title}、${url}等）存入list中，再转为content、title、url等存入set中
     * @param content 渠道配置内容
     * @return content、title、url中有占位符的参数的set集合
     */
    public static Set<String> getPlaceholderList(String content) {
        // 消息内容打散为一个个字符
        char[] textChars = content.toCharArray();
        StringBuilder textSofar = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        // 存储占位符的位置信息集合
        List<String> placeholderList = new ArrayList<>();
        // 当前标识
        int modeTg = IGNORE_TG;
        // 遍历字符数组
        for (int m = 0; m < textChars.length; m++) {
            char c = textChars[m];
            textSofar.append(c);
            switch (c) {
                case '{': {
                    modeTg = START_TG;
                    sb.append(c);
                }
                break;
                case '$': {
                    if (modeTg == START_TG) {
                        sb.append(c);
                        modeTg = READ_TG;
                    } else {
                        if (modeTg == READ_TG) {
                            sb = new StringBuilder();
                            modeTg = IGNORE_TG;
                        }
                    }
                }
                break;
                case '}': {
                    if (modeTg == READ_TG) {
                        modeTg = IGNORE_TG;
                        sb.append(c);
                        String str = sb.toString();
                        if (StrUtil.isNotEmpty(str)) {
                            placeholderList.add(str);
                            textSofar = new StringBuilder();
                        }
                        sb = new StringBuilder();
                    } else if (modeTg == START_TG) {
                        modeTg = IGNORE_TG;
                        sb = new StringBuilder();
                    }
                    break;
                }
                default: {
                    if (modeTg == READ_TG) {
                        sb.append(c);
                    } else if (modeTg == START_TG) {
                        modeTg = IGNORE_TG;
                        sb = new StringBuilder();
                    }
                }
            }
        }
        Set<String> result = placeholderList.stream().map(s -> s.replaceAll("\\{", "").replaceAll("\\$", "").replaceAll("\\}", "")).collect(Collectors.toSet());
        return result;
    }

    /**
     * 适配amis前端，获取 SmsTimeLineVo
     * @return
     */
    public static SmsDataVo getSmsDataVo(Map<String, List<SmsRecord>> maps) {
        // 表格列内容
        List<SmsDataVo.ItemsVO> itemsVoS = new ArrayList<>();
        // 表格整体内容
        SmsDataVo smsDataVo = SmsDataVo.builder().items(itemsVoS).build();
        // 遍历Map<手机号+下发批次id, 短信下发记录列表>
        for (Map.Entry<String, List<SmsRecord>> entry : maps.entrySet()) {
            // 创建每一个手机号+下发批次id对应的短信下发记录列表VO
            SmsDataVo.ItemsVO itemsVO = SmsDataVo.ItemsVO.builder().build();
            for (SmsRecord smsRecord : entry.getValue()) {
                // 短信发送记录messageTemplateId > 0 , 短信回执记录messageTemplateId = 0
                if (smsRecord.getMessageTemplateId() > 0) {
                    itemsVO.setBusinessId(String.valueOf(smsRecord.getMessageTemplateId()));
                    itemsVO.setSupplierName(smsRecord.getSupplierName());
                    itemsVO.setPhone(smsRecord.getPhone());
                    itemsVO.setContent(smsRecord.getMsgContent());
                    itemsVO.setSendType(SmsStatus.getDescriptionByStatus(smsRecord.getStatus()));
                    itemsVO.setSendTime(DateUtil.format(new Date(Long.valueOf(smsRecord.getCreated() * 1000L)), DatePattern.NORM_DATETIME_PATTERN));
                } else {
                    itemsVO.setReceiveType(SmsStatus.getDescriptionByStatus(smsRecord.getStatus()));
                    itemsVO.setReceiveContent(smsRecord.getReportContent());
                    itemsVO.setReceiveTime(DateUtil.format(new Date(Long.valueOf(smsRecord.getUpdated() * 1000L)), DatePattern.NORM_DATETIME_PATTERN));
                }
            }
            itemsVoS.add(itemsVO);
        }
        return smsDataVo;
    }

    /**
     * 适配amis前端，获取EchartsVo
     * @param anchorResult 数据
     * @param templateName 模板标题
     * @param businessId 业务Id
     * @return 图表VO
     */
    public static EchartsDataVo getEchartsVo(Map<Object, Object> anchorResult, String templateName, String businessId) {
        // x轴
        List<String> xAxisList = new ArrayList<>();
        // x轴上的数据
        List<Integer> actualData = new ArrayList<>();
        if (CollUtil.isNotEmpty(anchorResult)) {
            // 将Redis中查询结果按埋点类型【Integer】排序
            anchorResult = MapUtil.sort(anchorResult);
            for (Map.Entry<Object, Object> entry : anchorResult.entrySet()) {
                // 埋点信息描述【x轴】
                String description = AnchorState.getDescriptionByCode(Integer.valueOf(String.valueOf(entry.getKey())));
                xAxisList.add(description);
                // 数据【发送条数】
                actualData.add(Integer.valueOf(String.valueOf(entry.getValue())));
            }
        }
        // 标题
        String title = "消息模板「" + templateName + "」在「" +
                DateUtil.format(
                        DateUtil.parse(
                                String.valueOf(TaskInfoUtils.getDateFromBusinessId(Long.valueOf(businessId)))
                        ), DatePattern.CHINESE_DATE_FORMATTER
                ) + "」的下发情况";
        return EchartsDataVo.builder()
                .title(EchartsDataVo.TitleVO.builder().left("30%").text(title).build())
                .legend(EchartsDataVo.LegendVO.builder().right("10%").data(Arrays.asList("人数")).build())
                .xAxis(EchartsDataVo.XaxisVO.builder().type("category").name("下发情况").data(xAxisList).build())
                .series(Arrays.asList(EchartsDataVo.SeriesVO.builder().name("人数").type("bar").data(actualData).build()))
                .yAxis(EchartsDataVo.YaxisVO.builder().type("value").name("人数").build())
                .tooltip(EchartsDataVo.TooltipVO.builder().build())
                .build();
    }

    /**
     * 适配amis前端
     * 得到模板的参数 组装好 返回给前端展示
     * @param wxTemplateId
     * @param allPrivateTemplate
     * @return
     */
//    public static CommonAmisVo getWxMpTemplateParam(String wxTemplateId, List<WxMpTemplate> allPrivateTemplate) {
//        CommonAmisVo officialAccountParam = null;
//        for (WxMpTemplate wxMpTemplate : allPrivateTemplate) {
//            if (wxTemplateId.equals(wxMpTemplate.getTemplateId())) {
//                String[] data = wxMpTemplate.getContent().split(StrUtil.LF);
//                officialAccountParam = CommonAmisVo.builder()
//                        .type("input-table")
//                        .name("officialAccountParam")
//                        .addable(true)
//                        .editable(true)
//                        .needConfirm(false)
//                        .build();
//                List<CommonAmisVo.ColumnsDTO> columnsDtoS = new ArrayList<>();
//                for (String datum : data) {
//                    String name = datum.substring(datum.indexOf("{{") + 2, datum.indexOf("."));
//                    CommonAmisVo.ColumnsDTO.ColumnsDTOBuilder dtoBuilder = CommonAmisVo.ColumnsDTO.builder().name(name).type("input-text").required(true).quickEdit(true);
//                    if (datum.contains("first")) {
//                        dtoBuilder.label("名字");
//                    } else if (datum.contains("remark")) {
//                        dtoBuilder.label("备注");
//                    } else {
//                        dtoBuilder.label(datum.split("：")[0]);
//                    }
//                    columnsDtoS.add(dtoBuilder.build());
//                }
//                officialAccountParam.setColumns(columnsDtoS);
//
//            }
//        }
//        return officialAccountParam;
//    }

    /**
     * 适配amis前端
     * 得到模板的参数 组装好 返回给前端展示
     * @param wxTemplateId
     * @param templateList
     * @return
     */
//    public static CommonAmisVo getWxMaTemplateParam(String wxTemplateId, List<TemplateInfo> templateList) {
//        CommonAmisVo officialAccountParam = null;
//        for (TemplateInfo templateInfo : templateList) {
//            if (wxTemplateId.equals(templateInfo.getPriTmplId())) {
//                String[] data = templateInfo.getContent().split(StrUtil.LF);
//                officialAccountParam = CommonAmisVo.builder()
//                        .type("input-table")
//                        .name("miniProgramParam")
//                        .addable(true)
//                        .editable(true)
//                        .needConfirm(false)
//                        .build();
//                List<CommonAmisVo.ColumnsDTO> columnsDtoS = new ArrayList<>();
//                for (String datum : data) {
//                    String name = datum.substring(datum.indexOf("{{") + 2, datum.indexOf("."));
//                    String label = datum.split(":")[0];
//                    CommonAmisVo.ColumnsDTO columnsDTO = CommonAmisVo.ColumnsDTO.builder()
//                            .name(name).type("input-text").required(true).quickEdit(true).label(label).build();
//                    columnsDtoS.add(columnsDTO);
//                }
//                officialAccountParam.setColumns(columnsDtoS);
//
//            }
//        }
//        return officialAccountParam;
//    }
}
