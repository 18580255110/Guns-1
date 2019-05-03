package com.stylefeng.guns.core.message;

/**
 * Created by HH on 2017/2/20.
 */
public final class MessageConstant {
    private MessageConstant(){}

    public static final String DEFAULT_MESSAGE = "系统异常";

    public static class MessageCode{
        /**
         * 正常
         */
        public static final String SYS_OK = "000000";
        /**
         * 系统异常
         */
        public static final String SYS_EXCEPTION = "000099";
        /**
         * 校验码不正确
         */
        public static final String SYS_CAPTCHA_NOT_MATCH = "000001";
        /**
         * 校验码超时
         */
        public static final String SYS_CAPTCHA_TIME_OUT = "000002";
        /**
         * 对象不存在
         */
        public static final String SYS_SUBJECT_NOT_FOUND = "000003";
        /**
         * 对象重复
         */
        public static final String SYS_SUBJECT_DUPLICATE = "000004";
        /**
         * 缺少参数
         */
        public static final String SYS_MISSING_ARGUMENTS = "000005";
        /**
         * 状态异常
         */
        public static final String SYS_SUBJECT_STATE = "000006";
        /**
         * 无效字符
         */
        public static final String SYS_DATA_ILLEGAL = "000007";
        /**
         * 非法格式
         */
        public static final String SYS_ARGUMENTS_ILLEGAL_FORMAT = "000008";
        /**
         * 对象正在使用
         */
        public static final String SYS_SUBJECT_ONAIR = "000009";
        /**
         * 模板信息
         */
        public static final String SYS_TEMPLATE_MESSAGE = "000010";
        /**
         * 没有获取到有效的用户登录信息
         */
        public static final String SYS_CREDENTIAL_UNKNOW = "000101";
        /**
         * 用户令牌失效
         */
        public static final String SYS_CREDENTIAL_EXPIRED = "000102";
        /**
         * 数据超过限制
         */
        public static final String SYS_DATA_OVERTOP = "000103";
        /**
         * 签名验证失败
         */
        public static final String SYS_TOKEN_ERROR = "000111";
        /**
         * 上传失败
         */
        public static final String SYS_UPLOAD_ERROR = "000599";
        /**
         * 上传文件过大
         */
        public static final String SYS_UPLOAD_OUT_OF_SIZE = "000501";
        /**
         * 不支持的文件格式
         */
        public static final String SYS_UPLOAD_UNKNOW_FORMAT = "000502";
        /**
         * 账号不存在
         */
        public static final String LOGIN_ACCOUNT_NOT_FOUND = "010001";
        /**
         * 账号失效
         */
        public static final String LOGIN_ACCOUNT_EXPIRED = "010002";
        /**
         * 账号已锁定
         */
        public static final String LOGIN_ACCOUNT_LOCKED = "010003";
        /**
         * 登录失败
         */
        public static final String LOGIN_FAILED = "010099";

        /**
         * 不能重复报名
         */
        public static final String COURSE_SELECTED = "020001";

        /**
         * 报名已截止
         */
        public static final String COURSE_SELECT_OUTTIME = "020002";

        /**
         * 所报班级年级不符合
         */
        public static final String GRADE_NOT_MATCH = "020003";

        /**
         * 报名还未开始
         */
        public static final String COURSE_SELECT_NOTIME = "020004";

        /**
         * 支付方式不支持
         */
        public static final String PAY_METHOD_NOT_FOUND = "030001";
        /**
         * 支付下单失败
         */
        public static final String PAY_ORDER_EXCEPTION = "030002";
        /**
         * 已生成订单
         */
        public static final String ORDER_REQUEST_ORDERED = "040001";
        /**
         * 需要入学测试
         */
        public static final String ORDER_NEED_EXAMINE = "040002";
        /**
         * 班级已达到最大容量
         */
        public static final String ORDER_NO_CAPACITY = "040003";
        /**
         * 您已有相同的调课申请
         */
        public static final String ADJUST_COURSE_DUPLICATE = "050001";
        /**
         * 您已有相同的转班申请
         */
        public static final String CHANGE_CLASS_DUPLICATE = "050002";
        /**
         * 排班失败
         */
        public static final String SCHEDULE_CLASS_FAILED = "050003";
        /**
         * 调课失败： 调入班级不满足调课条件
         */
        public static final String ADJUST_TARGET_FAILED = "050004";

        /**
         * 没有设置期望答案
         */
        public static final String QUESTION_NO_EXPECT_ANSWER = "060001";
        /**
         * 不支持的试题类型
         */
        public static final String QUESTION_NO_SUPPORT = "060002";
        /**
         * 提交测试失败
         */
        public static final String EXAMINE_SUBMIT_FAILED = "060101";
        /**
         * 入学测试次数限制
         */
        public static final String EXAMINE_LIMIT_FAILED = "060103";

        /**
         * 批量导入失败
         */
        public static final String BATCH_IMPORT_FAILED = "070001";
    }
}
