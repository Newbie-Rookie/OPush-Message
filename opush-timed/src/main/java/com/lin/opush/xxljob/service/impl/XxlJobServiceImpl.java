package com.lin.opush.xxljob.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.lin.opush.enums.RespStatusEnum;
import com.lin.opush.vo.BasicResultVO;
import com.lin.opush.xxljob.constants.XxlJobConstant;
import com.lin.opush.xxljob.domain.XxlJobGroup;
import com.lin.opush.xxljob.domain.XxlJobInfo;
import com.lin.opush.xxljob.service.XxlJobService;
import com.xxl.job.core.biz.model.ReturnT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 定时任务服务实现
 */
@Slf4j
@Service
public class XxlJobServiceImpl implements XxlJobService {
    /**
     * 登录xxl-job-admin的用户名
     */
    @Value("${xxl.job.admin.username}")
    private String xxlUserName;

    /**
     * 登录xxl-job-admin的密码
     */
    @Value("${xxl.job.admin.password}")
    private String xxlPassword;

    /**
     * xxl-job-admin的请求ip:port
     */
    @Value("${xxl.job.admin.addresses}")
    private String xxlAddresses;

    /**
     * 创建或更新定时任务
     * @param xxlJobInfo 任务信息
     * @return 创建/更新结果
     */
    @Override
    public BasicResultVO saveTimedJob(XxlJobInfo xxlJobInfo) {
        // 请求参数【定时任务信息】
        Map<String, Object> params = JSON.parseObject(JSON.toJSONString(xxlJobInfo), Map.class);
        // 通过任务id判断定时任务是否已存在，不存在则请求/jobinfo/add，已存在则请求/jobinfo/update【JobInfoController】
        String path = Objects.isNull(xxlJobInfo.getId()) ?
                        xxlAddresses + XxlJobConstant.ADD_JOB_URL :
                        xxlAddresses + XxlJobConstant.UPDATE_JOB_URL;
        // 响应和返回数据
        HttpResponse response;
        ReturnT returnT = null;
        try {
            response = HttpRequest.post(path).form(params).cookie(getCookie()).execute();
            returnT = JSON.parseObject(response.body(), ReturnT.class);
            if (response.isOk() && ReturnT.SUCCESS_CODE == returnT.getCode()) {
                // 插入定时任务时需要返回任务Id，而更新定时任务时不需要
                if (path.contains(XxlJobConstant.ADD_JOB_URL)) {
                    Integer timedJobId = Integer.parseInt(String.valueOf(returnT.getContent()));
                    return BasicResultVO.success(timedJobId);
                } else if (path.contains(XxlJobConstant.UPDATE_JOB_URL)) {
                    return BasicResultVO.success();
                }
            }
        } catch (Exception e) {
            log.error("XxlJobServiceImpl#saveTimedJob fail,e:{},param:{},response:{}",
                    Throwables.getStackTraceAsString(e), JSON.toJSONString(xxlJobInfo), JSON.toJSONString(returnT));
        }
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, JSON.toJSONString(returnT));
    }

    /**
     * 开启定时任务
     * @param timedJobId 定时任务id
     * @return 启动结果
     */
    @Override
    public BasicResultVO startTimedJob(Integer timedJobId) {
        // JobInfoController的/jobinfo/start
        String path = xxlAddresses + XxlJobConstant.START_JOB_URL;
        // 请求参数【定时任务id】
        HashMap<String, Object> params = MapUtil.newHashMap();
        params.put("id", timedJobId);
        // 响应和返回数据
        HttpResponse response;
        ReturnT returnT = null;
        try {
            // post请求
            response = HttpRequest.post(path).form(params).cookie(getCookie()).execute();
            returnT = JSON.parseObject(response.body(), ReturnT.class);
            if (response.isOk() && ReturnT.SUCCESS_CODE == returnT.getCode()) {
                return BasicResultVO.success();
            }
        } catch (Exception e) {
            log.error("XxlJobServiceImpl#startTimedJob fail,e:{},param:{},response:{}",
                    Throwables.getStackTraceAsString(e), JSON.toJSONString(params), JSON.toJSONString(returnT));
        }
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, JSON.toJSONString(returnT));
    }

    /**
     * 暂停定时任务
     * @param timedJobId 定时任务id
     * @return 暂停结果
     */
    @Override
    public BasicResultVO stopTimedJob(Integer timedJobId) {
        // JobInfoController的/jobinfo/stop
        String path = xxlAddresses + XxlJobConstant.STOP_JOB_URL;
        // 请求参数【定时任务id】
        HashMap<String, Object> params = MapUtil.newHashMap();
        params.put("id", timedJobId);
        // 响应和返回数据
        HttpResponse response;
        ReturnT returnT = null;
        try {
            response = HttpRequest.post(path).form(params).cookie(getCookie()).execute();
            returnT = JSON.parseObject(response.body(), ReturnT.class);
            if (response.isOk() && ReturnT.SUCCESS_CODE == returnT.getCode()) {
                return BasicResultVO.success();
            }
        } catch (Exception e) {
            log.error("XxlJobServiceImpl#stopTimedJob fail,e:{},param:{},response:{}",
                    Throwables.getStackTraceAsString(e), JSON.toJSONString(params), JSON.toJSONString(returnT));
        }
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, JSON.toJSONString(returnT));
    }

    /**
     * 删除定时任务
     * @param timedJobId 定时任务id
     * @return 删除结果
     */
    @Override
    public BasicResultVO deleteTimedJob(Integer timedJobId) {
        // JobInfoController中/jobinfo/remove
        String path = xxlAddresses + XxlJobConstant.DELETE_JOB_URL;
        // 请求参数【定时任务id】
        HashMap<String, Object> params = MapUtil.newHashMap();
        params.put("id", timedJobId);
        // 响应和返回数据
        HttpResponse response;
        ReturnT returnT = null;
        try {
            response = HttpRequest.post(path).form(params).cookie(getCookie()).execute();
            returnT = JSON.parseObject(response.body(), ReturnT.class);
            if (response.isOk() && ReturnT.SUCCESS_CODE == returnT.getCode()) {
                return BasicResultVO.success();
            }
        } catch (Exception e) {
            log.error("XxlJobServiceImpl#deleteTimedJob fail,e:{},param:{},response:{}",
                    Throwables.getStackTraceAsString(e), JSON.toJSONString(params), JSON.toJSONString(returnT));
        }
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, JSON.toJSONString(returnT));
    }


    /**
     * 根据执行器的appname和title获取执行器id
     * @param appName 执行器appname
     * @param title   执行器名
     * @return 获取结果【执行器id】
     */
    @Override
    public BasicResultVO getJobGroupId(String appName, String title) {
        // 执行器列表请求地址：JobGroupController的/jobgroup/pageList
        String path = xxlAddresses + XxlJobConstant.JOBGROUP_PAGELIST_URL;
        // 请求参数【执行器的appname和title】
        HashMap<String, Object> params = MapUtil.newHashMap();
        params.put("appname", appName);
        params.put("title", title);
        // 响应
        HttpResponse response = null;
        try {
            // post请求
            response = HttpRequest.post(path).form(params).cookie(getCookie()).execute();
            // 获取返回执行器列表中第一个执行器的id【目前仅有一个执行器】
            Integer id = JSON.parseObject(response.body()).getJSONArray("data")
                                        .getJSONObject(0).getInteger("id");
            if (response.isOk() && Objects.nonNull(id)) {
                return BasicResultVO.success(id);
            }
        } catch (Exception e) {
            log.error("XxlJobServiceImpl#getJobGroupId fail,e:{},param:{},response:{}",
                    Throwables.getStackTraceAsString(e), JSON.toJSONString(params), JSON.toJSONString(response.body()));
        }
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, JSON.toJSONString(response.body()));
    }

    /**
     * 创建执行器
     * @param xxlJobGroup 执行器信息
     * @return 创建结果【成功/失败】
     */
    @Override
    public BasicResultVO createXxlJobGroup(XxlJobGroup xxlJobGroup) {
        // 请求参数【执行器信息】
        Map<String, Object> params = JSON.parseObject(JSON.toJSONString(xxlJobGroup), Map.class);
        // JobGroupController的/jobgroup/save
        String path = xxlAddresses + XxlJobConstant.JOBGROUP_SAVE_URL;
        // 请求响应
        HttpResponse response;
        // 请求返回数据结果（ReturnT类型）
        ReturnT returnT = null;
        try {
            // post请求
            response = HttpRequest.post(path).form(params).cookie(getCookie()).execute();
            // 判断执行器是否创建成功
            returnT = JSON.parseObject(response.body(), ReturnT.class);
            if (response.isOk() && ReturnT.SUCCESS_CODE == returnT.getCode()) {
                return BasicResultVO.success();
            }
        } catch (Exception e) {
            log.error("XxlJobServiceImpl#createXxlJobGroup fail,e:{},param:{},response:{}",
                    Throwables.getStackTraceAsString(e), JSON.toJSONString(params), JSON.toJSONString(returnT));
        }
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, JSON.toJSONString(returnT));
    }

    /**
     * 获取xxl-job-admin登录cookie
     * 【由于不确定cookie读应的session何时过期，每次进行定时任务操作时都要去拿cookie，并传入唯一id】
     * @return cookie
     */
    private String getCookie() {
        // 请求参数【登录用户、密码、唯一id】
        Map<String, Object> params = MapUtil.newHashMap();
        params.put("userName", xxlUserName);
        params.put("password", xxlPassword);
        params.put("randomCode", IdUtil.fastSimpleUUID());
        // xxl-job-admin登录请求url【IndexController的/login】
        String path = xxlAddresses + XxlJobConstant.LOGIN_URL;
        // 响应
        HttpResponse response = null;
        try {
            // 为避免该过程失败，允许最多重试3次
            for (int times = 1;times <= 3;times++) {
                // post请求
                response = HttpRequest.post(path).form(params).execute();
                if (response.isOk()) {
                    List<HttpCookie> cookies = response.getCookies();
                    StringBuilder sb = new StringBuilder();
                    for (HttpCookie cookie : cookies) {
                        sb.append(cookie.toString());
                    }
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            log.error("XxlJobServiceImpl#createXxlJobGroup getCookie,e:{},param:{},response:{}",
                    Throwables.getStackTraceAsString(e), JSON.toJSONString(params), JSON.toJSONString(response));
        }
        return null;
    }
}
