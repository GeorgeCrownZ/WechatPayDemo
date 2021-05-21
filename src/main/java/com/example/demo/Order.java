package com.example.demo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author md
 * @since 2021-04-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Alias("Order")
@TableName("fa_order")
public class Order implements Serializable {

    private static final long serialVersionUID=1L;

      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String orderId;

    /**
     * userid
     */
    private Long userId;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userNick;

    /**
     * 真实姓名
     */
    private String trueName;

    /**
     * 用户手机号
     */
    private String mobileNumber;

    /**
     * 渠道ID-如菜篮子店、地利鲜店等
     */
    private Integer channelId;

    /**
     * 商品数量
     */
    private Integer productCount;

    /**
     * 收件人姓名
     */
    private String receiveName;

    /**
     * 收货人电话
     */
    private String receiveTelphone;

    /**
     * 收获地址
     */
    private String address;

    /**
     * 用户备注
     */
    private String comments;

    /**
     * 系统备注
     */
    private String systemComments;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 是否评价 0未评价，1已评价
     */
    private Integer isComments;

    /**
     * 是否售后 0未售后，1已售后
     */
    private Integer isSales;

    /**
     * 商品金额
     */
    private BigDecimal productAmount;

    /**
     * 优惠金额，通过优惠券，满减，积分等
     */
    private BigDecimal discountAmount;

    /**
     * 订单状态  1: 已取消 2待发货  3待收货 4已签收
     */
    private Integer tradeStatus;

    /**
     * 发货时间
     */
    private Date sendTime;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 支付类型：1 微信支付 2 支付宝支付 3储值卡 4购物积分
     */
    private Integer payWay;

    /**
     * 支付状态：0 未付款 1 已付款
     */
    private Integer payStatus;

    /**
     * 第三方支付单号
     */
    private String payNo;

    /**
     * 订单状态：1 生效中 2 已删除 999 超时订单
     */
    private Integer orderStatus;

    /**
     * 总重量
     */
    private BigDecimal totalWeight;

    /**
     * 总积分
     */
    private BigDecimal totalScore;

    /**
     * 网页防刷code
     */
    private String code;

    /**
     * 快递单号
     */
    private String expressNo;

    /**
     * 快递公司id
     */
    private Long expressId;

    /**
     * 快递公司
     */
    private String expressName;

    /**
     * 省份
     */
    private Long province;

    /**
     * 快递费：每公斤单价
     */
    private BigDecimal expressFee;

    /**
     * 是否退款
     */
    private Integer isBackpay;

    /**
     * 退款金额
     */
    private BigDecimal backpayAmount;

    /**
     * 门店id
     */
    private Long shopId;

    /**
     * 门店名称
     */
    private String shopName;

    /**
     * 下单时间
     */
    private Date createTime;


}
