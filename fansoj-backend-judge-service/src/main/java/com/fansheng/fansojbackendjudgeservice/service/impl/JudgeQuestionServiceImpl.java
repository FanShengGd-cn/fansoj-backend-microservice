package com.fansheng.fansojbackendjudgeservice.service.impl;

import cn.hutool.json.JSONUtil;
import com.fansheng.fansojbackendcommon.common.ErrorCode;
import com.fansheng.fansojbackendcommon.exception.BusinessException;
import com.fansheng.fansojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import com.fansheng.fansojbackendjudgeservice.judge.factory.SandBoxFactory;
import com.fansheng.fansojbackendjudgeservice.judge.proxy.SandBoxProxy;
import com.fansheng.fansojbackendjudgeservice.judge.strategy.JudgeContext;
import com.fansheng.fansojbackendjudgeservice.judge.strategy.JudgeStrategyManager;
import com.fansheng.fansojbackendjudgeservice.service.JudgeQuestionService;
import com.fansheng.fansojbackendmodel.model.dto.codesandbox.ExecuteQuestionRequest;
import com.fansheng.fansojbackendmodel.model.dto.codesandbox.ExecuteQuestionResponse;
import com.fansheng.fansojbackendmodel.model.dto.codesandbox.JudgeInfo;
import com.fansheng.fansojbackendmodel.model.dto.question.JudgeCase;
import com.fansheng.fansojbackendmodel.model.entity.Question;
import com.fansheng.fansojbackendmodel.model.entity.QuestionSubmit;
import com.fansheng.fansojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.fansheng.fansojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class JudgeQuestionServiceImpl implements JudgeQuestionService {
    @Value("${codeSandBox.type}")
    private String type;
    @Resource
    private QuestionFeignClient questionFeignClient;

    @Resource
    private JudgeStrategyManager judgeStrategyManager;

    @Override
    public QuestionSubmit doJudge(Long questionSubmitId) {
        // Todo 非原子操作，待加锁
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交信息不存在");
        }

        Long id = questionSubmit.getId();

        String judgeInfo = questionSubmit.getJudgeInfo();
        Long userId = questionSubmit.getUserId();
        Integer status = questionSubmit.getStatus();
        Long questionId = questionSubmit.getQuestionId();

        if (!Objects.equals(status, QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "正在判题中");
        }
        Question question = questionFeignClient.getQuestionById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "问题不存在");
        }
//        // 题目提交数加一
//        Question questionUpdate = new Question();
//        questionUpdate.setId(questionId);
//        questionUpdate.setSubmitNum(question.getSubmitNum()+1);
//        boolean questionUpdateFir = questionFeignClient.(questionUpdate);
//        if(!questionUpdateFir){
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目状态更新失败");
//        }
        // 更改题目提交状态
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(id);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean updateStatus = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!updateStatus) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        String judgeCase = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCase, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteQuestionRequest executeQuestionRequest = ExecuteQuestionRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        // 应用代理模式和工厂模式调用代码沙箱
        CodeSandBox codeSandBox = new SandBoxProxy(SandBoxFactory.getCodeSandBox(type));
        ExecuteQuestionResponse executeQuestionResponse = codeSandBox.doExecute(executeQuestionRequest);
        // 检查沙箱输出结果
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeQuestionResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(executeQuestionResponse.getOutputList());
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        judgeContext.setJudgeCaseList(judgeCaseList);

        JudgeInfo judgeInfoRes = judgeStrategyManager.doJudge(judgeContext);
        // 修改提交结果状态
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfoRes));
        boolean finalUpdate = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if(!finalUpdate){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        QuestionSubmit resQS = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        return resQS;
    }
}