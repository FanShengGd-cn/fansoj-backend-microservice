package com.fansheng.fansojbackendquestionservice.service.impl;


import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fansheng.fansojbackendcommon.common.ErrorCode;
import com.fansheng.fansojbackendcommon.exception.BusinessException;
import com.fansheng.fansojbackendcommon.exception.ThrowUtils;
import com.fansheng.fansojbackendmodel.model.dto.question.QuestionQueryRequest;
import com.fansheng.fansojbackendmodel.model.entity.Question;
import com.fansheng.fansojbackendmodel.model.entity.User;
import com.fansheng.fansojbackendmodel.model.vo.QuestionVO;
import com.fansheng.fansojbackendmodel.model.vo.UserVO;
import com.fansheng.fansojbackendquestionservice.mapper.QuestionMapper;
import com.fansheng.fansojbackendquestionservice.service.QuestionService;

import com.fansheng.fansojbackendserviceclient.service.UserFeignClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author fansheng
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2023-12-22 09:17:51
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService {

    @Resource
    private UserFeignClient userFeignClient;



    @Override
    public void validQuestion(Question question, boolean add) {
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String answer = question.getAnswer();
        String judgeCase = question.getJudgeCase();
        String judgeConfig = question.getJudgeConfig();

        if( add){
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title,content,tags.toString()),ErrorCode.PARAMS_ERROR);
        }
        if(StringUtils.isNotBlank(title) && title.length() > 80 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标题过长");
        }
        if(StringUtils.isNotBlank(content) && content.length() > 8192 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"内容过长");
        }
        if(StringUtils.isNotBlank(answer) && answer.length() > 8192 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"答案过长");
        }
        if(StringUtils.isNotBlank(judgeCase) && judgeCase.length() > 8192 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"判题用例过长");
        }
        if(StringUtils.isNotBlank(judgeConfig) && judgeConfig.length() > 8192 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"判题配置过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        String searchText = questionQueryRequest.getSearchText();
        List<String> tagList = questionQueryRequest.getTags();
        Long userId = questionQueryRequest.getUserId();




        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.like("title", searchText).or().like("content", searchText);
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollectionUtils.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }

        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", 0);
        return queryWrapper;
    }


    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        QuestionVO questionVO = QuestionVO.objToVo(question);
        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userFeignClient.getById(userId);
        }
        UserVO userVO = userFeignClient.getUserVO(user);
        questionVO.setUserVO(userVO);
        return questionVO;
    }

    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollectionUtils.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userFeignClient.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            QuestionVO questionVO = QuestionVO.objToVo(question);
            Long userId = question.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUserVO(userFeignClient.getUserVO(user));
            return questionVO;
        }).collect(Collectors.toList());
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }
}




