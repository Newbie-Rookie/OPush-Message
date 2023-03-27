package com.lin.opush.dao;

import com.lin.opush.domain.SmsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 短信记录 Dao
 */
public interface SmsRecordDao extends JpaRepository<SmsRecord, Long>, JpaSpecificationExecutor<SmsRecord> {
    /**
     * 根据手机号和日期获取短信下发记录
     * @param phone 手机号
     * @param sendDate 日期
     * @param creator  下发者
     * @return 短信下发记录
     */
    List<SmsRecord> findByPhoneEqualsAndSendDateEqualsAndCreatorEquals(Long phone, Integer sendDate, String creator);

    /**
     * 根据下发流水号回去对应短信下发记录
     * @param seriesId 下发流水号
     * @return 短信下发记录
     */
    SmsRecord findBySeriesIdEquals(String seriesId);
}