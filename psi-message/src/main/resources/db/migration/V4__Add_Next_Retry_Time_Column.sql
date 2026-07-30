USE psi_message;

-- 为死信表添加下次可重试时间字段，支持指数退避
ALTER TABLE msg_dead_letter
    ADD COLUMN next_retry_time DATETIME DEFAULT NULL COMMENT '下次可重试时间' AFTER retryable;

-- 添加索引，用于定时重试任务扫描
CREATE INDEX idx_next_retry_time ON msg_dead_letter(next_retry_time);
