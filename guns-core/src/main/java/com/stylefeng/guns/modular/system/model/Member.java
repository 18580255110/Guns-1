package com.stylefeng.guns.modular.system.model;

import java.util.Date;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.stylefeng.guns.modular.member.MemberStarEnum;
import com.stylefeng.guns.modular.member.MemberStateEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * <p>
 * 会员表
 * </p>
 *
 * @author simple
 * @since 2018-11-03
 */
@TableName("tb_member")
@ApiModel(value = "Member", description = "用户")
public class Member extends Model<Member> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键标示
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(name = "id", value = "用户名", position = 0, hidden = true)
    private Long id;

    /**
     * 用户名
     */
    @TableField("user_name")
    @ApiModelProperty(name = "username", value = "用户名", position = 0, example="sJ12181101U234g")
    @NotBlank(message = "用户名不能为空")
    private String userName;
    /**
     * 密码sha256加密
     */
    @ApiModelProperty(name = "password", value = "密码 sha256 加密", hidden = true)
    private String password;
    /**
     * 用户名称
     */
    @ApiModelProperty(name = "name", value = "姓名", position = 1, example="小明")
    private String name;

    /**
     * 昵称
     */
    @ApiModelProperty(name = "nickname", value = "昵称", position = 2, example="小明")
    private String nickname;
    /**
     * 性别: 1 男  2 女
     */
    @ApiModelProperty(name = "gender", value = "性别: 1 男  2 女", position = 3, example="1")
    private Integer gender;
    /**
     * 联系手机
     */
    @TableField("mobile_number")
    @ApiModelProperty(name = "mobileNumber", value = "联系手机号", position = 4, example="13399883333")
    private String mobileNumber;

    /**
     * 星级
     */
    @ApiModelProperty(name = "star", value = "用户星级", position = 5, example="1")
    private Integer star;
    /**
     * 联系地址
     */
    @ApiModelProperty(name = "address", value = "联系地址", position = 6, example="13399883333")
    private String address;
    /**
     * QQ号码
     */
    @ApiModelProperty(name = "qq", value = "QQ号码", position = 7, example="18181111")
    private String qq;
    /**
     * 微信号
     */
    @ApiModelProperty(name = "weixin", value = "微信号码", position = 8, example="weixin_123")
    private String weixin;
    /**
     * 电子邮箱
     */
    @ApiModelProperty(name = "email", value = "微信号码", position = 9, example="16334@qq.com")
    private String email;
    /**
     * 状态： 1 0 无效 11 有效 1 2 锁定 
     */
    @ApiModelProperty(name = "status", value = "状态：  10 无效 11 有效  12 锁定 ", position = 10, example="1")
    private Integer status;
    /**
     * 加入时间
     */
    @TableField("join_date")
    @ApiModelProperty(name = "joinDate", value = "状态：  0 无效 1 有效  2 锁定 ", position = 11, example="2018-11-01", hidden = true)
    private Date joinDate;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Integer getStar() {
        return star;
    }

    public void setStar(Integer star) {
        this.star = star;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getWeixin() {
        return weixin;
    }

    public void setWeixin(String weixin) {
        this.weixin = weixin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "Member{" +
        "id=" + id +
        ", userName=" + userName +
        ", password=" + password +
        ", name=" + name +
        ", gender=" + gender +
        ", mobileNumber=" + mobileNumber +
        ", address=" + address +
        ", qq=" + qq +
        ", weixin=" + weixin +
        ", email=" + email +
        ", status=" + status +
        ", joinDate=" + joinDate +
        "}";
    }


    public boolean isValid() {
        if (null == this.status)
            return false;

        return MemberStateEnum.Valid.code == this.status;
    }

    public boolean isTeacher() {
        if (null == this.star)
            return false;

        return MemberStarEnum.Star_99.code == this.star;
    }
}
