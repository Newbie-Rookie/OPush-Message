package com.lin.opush.domain.sms;

import lombok.*;

/**
 * 短信下发回执
 *      短信渠道商UniSMS目前仅支持WebHook模式拉取【接收】短信回执
 *      仅用于接收UniSMS短信回执
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SmsReceipt {
    private String id;
    private String status;
    private String to;
    private String regionCode;
    private String countryCode;
    private Integer messageCount;
    private String price;
    private String currency;
    private String errorCode;
    private String errorMessage;
    private String submitDate;
    private String doneDate;
}
