package com.fansheng.fansojbackendjudgeservice.judge.codesandbox;


import com.fansheng.fansojbackendmodel.model.dto.codesandbox.ExecuteQuestionRequest;
import com.fansheng.fansojbackendmodel.model.dto.codesandbox.ExecuteQuestionResponse;

public class ThirdPartySandBox implements CodeSandBox{
    @Override
    public ExecuteQuestionResponse doExecute(ExecuteQuestionRequest executeQuestionRequest) {
        System.out.println("第三方代码沙箱");
        return null;
    }
}
