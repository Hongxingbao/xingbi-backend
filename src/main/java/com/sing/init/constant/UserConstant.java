package com.sing.init.constant;

/**
 * 用户常量
 *
 * @author xing

 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    /**
     * 默认名字
     */
    String DEFAULT_NAME = "无名氏";

    /**
     * 默认头像
     */
    String DEFAULT_AVATAR = "https://dn-qiniu-avatar.qbox.me/avatar/";

    // endregion
}
