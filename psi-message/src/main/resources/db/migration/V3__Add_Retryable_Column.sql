USE psi_message;

-- 为死信表添加可重试字段
ALTER TABLE msg_dead_letter 
ADD COLUMN retryable TINYINT DEFAULT 1 COMMENT '是否可重试 0-不可重试 1-可重试' AFTER last_failed_time;

-- 添加索引
CREATE INDEX idx_retryable ON msg_dead_letter(retryable);