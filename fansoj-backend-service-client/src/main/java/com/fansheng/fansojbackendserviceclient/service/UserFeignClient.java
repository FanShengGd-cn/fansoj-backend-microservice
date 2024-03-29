package com.fansheng.fansojbackendserviceclient.service;


import com.fansheng.fansojbackendcommon.common.ErrorCode;
import com.fansheng.fansojbackendcommon.exception.BusinessException;
import com.fansheng.fansojbackendmodel.model.entity.User;
import com.fansheng.fansojbackendmodel.model.enums.UserRoleEnum;
import com.fansheng.fansojbackendmodel.model.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static com.fansheng.fansojbackendcommon.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务
 *
 * @author fansheng
 */
@FeignClient(name = "fansoj-backend-user-service", path = "/api/user/inner")
public interface UserFeignClient {




    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    default User getLoginUser(HttpServletRequest request){
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    };

    @GetMapping("/get/id")
    User getById(@RequestParam(value = "userId") Long userId);


    @GetMapping("/get/ids")
    List<User> listByIds(@RequestParam(value = "idList") Collection<Long> idList);
    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    default boolean isAdmin(HttpServletRequest request){
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    };

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    default boolean isAdmin(User user){
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    };





    default UserVO getUserVO(User user){
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    };



}
