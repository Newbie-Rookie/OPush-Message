package com.lin.opush.controller;

import com.lin.opush.domain.BatchSendRequest;
import com.lin.opush.domain.SendRequest;
import com.lin.opush.domain.SendResponse;
import com.lin.opush.service.SendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 外部系统接入使用接口
 */
@RestController
public class SendController {

    @Autowired
    private SendService sendService;

    /**
     * 单个文案下发相同的人
     * @param sendRequest 单个请求
     * @return
     */
    @PostMapping("/send")
    public SendResponse send(@RequestBody SendRequest sendRequest) {
        return sendService.send(sendRequest);
    }

    /**
     * 不同文案下发到不同的人
     * @param batchSendRequest 批量请求
     * @return
     */
    @PostMapping("/batchSend")
    public SendResponse batchSend(@RequestBody BatchSendRequest batchSendRequest) {
        return sendService.batchSend(batchSendRequest);
    }
}
