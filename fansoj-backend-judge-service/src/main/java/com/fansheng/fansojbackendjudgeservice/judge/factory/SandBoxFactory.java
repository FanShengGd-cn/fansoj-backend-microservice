package com.fansheng.fansojbackendjudgeservice.judge.factory;


import com.fansheng.fansojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import com.fansheng.fansojbackendjudgeservice.judge.codesandbox.DefaultSandBox;
import com.fansheng.fansojbackendjudgeservice.judge.codesandbox.RemoteSandBox;
import com.fansheng.fansojbackendjudgeservice.judge.codesandbox.ThirdPartySandBox;

public class SandBoxFactory {

    public static CodeSandBox getCodeSandBox(String type){
        switch (type){
            case "remote":
                return new RemoteSandBox();
            case "third":
                return new ThirdPartySandBox();
            default:
                return new DefaultSandBox();
        }
    }
}
