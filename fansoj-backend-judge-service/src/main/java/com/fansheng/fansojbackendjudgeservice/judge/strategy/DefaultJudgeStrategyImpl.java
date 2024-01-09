package com.fansheng.fansojbackendjudgeservice.judge.strategy;


import cn.hutool.json.JSONUtil;
import com.fansheng.fansojbackendmodel.model.dto.codesandbox.JudgeInfo;
import com.fansheng.fansojbackendmodel.model.dto.question.JudgeCase;
import com.fansheng.fansojbackendmodel.model.dto.question.JudgeConfig;
import com.fansheng.fansojbackendmodel.model.entity.Question;
import com.fansheng.fansojbackendmodel.model.enums.JudgeInfoMessageEnum;

import java.util.List;

public class DefaultJudgeStrategyImpl implements JudgeStrategy {
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        JudgeInfo outputJudgeInfo = judgeContext.getJudgeInfo();
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();

        // 待返回运行结果
        JudgeInfo judgeInfoResp = new JudgeInfo();


        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;
        if (outputList.size() != inputList.size()) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResp.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResp;
        }
        for (int i = 0; i < judgeCaseList.size(); i++) {
            if (!judgeCaseList.get(i).getOutput().equals(outputList.get(i))) {
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResp.setMessage(judgeInfoMessageEnum.getValue());
                return judgeInfoResp;
            }
        }
        Long memory = outputJudgeInfo.getMemory();
        Long time = outputJudgeInfo.getTime();
        String judgeConfig = question.getJudgeConfig();
        JudgeConfig judgeConfigBean = JSONUtil.toBean(judgeConfig, JudgeConfig.class);
        Long timeLimit = judgeConfigBean.getTimeLimit();
        Long memoryLimit = judgeConfigBean.getMemoryLimit();
        if (memory > memoryLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResp.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResp;
        }
        if (time > timeLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResp.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResp;
        }


        judgeInfoResp.setMessage(judgeInfoMessageEnum.getValue());
        judgeInfoResp.setMemory(memory);
        judgeInfoResp.setTime(time);


        return judgeInfoResp;
    }
}
