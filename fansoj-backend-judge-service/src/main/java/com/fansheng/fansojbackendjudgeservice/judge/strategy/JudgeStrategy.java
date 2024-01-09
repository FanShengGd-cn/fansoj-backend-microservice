package com.fansheng.fansojbackendjudgeservice.judge.strategy;


import com.fansheng.fansojbackendmodel.model.dto.codesandbox.JudgeInfo;

/**
 * 判题策略
 */
public interface JudgeStrategy {
    /**
     * 判题
     */
    JudgeInfo doJudge(JudgeContext judgeContext);
}
