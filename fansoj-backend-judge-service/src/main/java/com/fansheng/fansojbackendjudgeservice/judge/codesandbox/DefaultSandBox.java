package com.fansheng.fansojbackendjudgeservice.judge.codesandbox;


import com.fansheng.fansojbackendmodel.model.dto.codesandbox.ExecuteQuestionRequest;
import com.fansheng.fansojbackendmodel.model.dto.codesandbox.ExecuteQuestionResponse;

public class DefaultSandBox implements CodeSandBox{
    @Override
    public ExecuteQuestionResponse doExecute(ExecuteQuestionRequest executeQuestionRequest) {
        System.out.println("默认代码沙箱");
        return null;
    }
}
