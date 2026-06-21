package com.blank.app.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.blank.app.entity.*;
import com.blank.app.exception.BusinessException;
import com.blank.app.mapper.*;
import com.blank.app.service.VipService;
import com.blank.app.util.CodeGenerator;
import com.blank.app.util.ZPaySignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VipServiceImpl implements VipService {

    @Autowired private VipPlanMapper vipPlanMapper;
    @Autowired private VipOrderMapper vipOrderMapper;
    @Autowired private VipActivationCodeMapper vacMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ZPaySignUtil zPaySignUtil;
    @Autowired private CodeGenerator codeGenerator;
    @Value("${app.zpay.base-url:https://zpayz.cn}") private String zpayBaseUrl;
    @Value("${app.zpay.pid:}") private String zpayPid;
    @Value("${app.zpay.pkey:}") private String zpayPkey;
    @Value("${app.zpay.default-type:alipay}") private String zpayDefaultType;
    @Value("${app.zpay.cid:}") private String zpayCid;

    @Override
    public List<Map<String, Object>> getVipPlans() {
        List<Map<String, Object>> r = new ArrayList<>();
        for (VipPlan p : vipPlanMapper.selectActiveAll()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("planKey", p.getPlanKey()); item.put("title", p.getTitle());
            item.put("billingUnit", p.getBillingUnit()); item.put("priceCents", p.getPriceCents());
            item.put("currency", p.getCurrency()); r.add(item);
        }
        return r;
    }

    @Override
    public Map<String, Object> createVipPayment(Integer userId, String planKey, String payType, HttpServletRequest req) {
        if (zpayPid == null || zpayPid.isEmpty()) throw new BusinessException("支付系统未配置", 500);
        VipPlan plan = vipPlanMapper.selectByPlanKey(planKey);
        if (plan == null) throw new BusinessException("套餐不存在", 404);
        User user = userMapper.selectById(userId);
        if ("lifetime".equals(user.getVipPlanKey())) throw new BusinessException("已是终身会员", 400);
        String orderNo = codeGenerator.generateOrderNo();
        String ptype = payType != null ? payType : zpayDefaultType;

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("pid", zpayPid); params.put("type", ptype); params.put("out_trade_no", orderNo);
        params.put("notify_url", "https://example.com/api/vip/payments/notify");
        params.put("return_url", "https://example.com/payment/result");
        params.put("name", plan.getTitle() + " - VIP会员");
        params.put("money", String.format("%.2f", plan.getPriceCents() / 100.0));
        if (zpayCid != null && !zpayCid.isEmpty()) params.put("cid", zpayCid);
        String sign = zPaySignUtil.buildSign(params, zpayPkey);
        params.put("sign", sign); params.put("sign_type", "MD5");
        StringBuilder url = new StringBuilder(zpayBaseUrl + "/submit.php?");
        for (Map.Entry<String, Object> e : params.entrySet()) url.append(e.getKey()).append("=").append(e.getValue()).append("&");
        String gateway = url.substring(0, url.length() - 1);

        VipOrder order = new VipOrder();
        order.setOrderNo(orderNo); order.setUserId(userId); order.setPlanKey(planKey);
        order.setPlanTitle(plan.getTitle()); order.setAmountCents(plan.getPriceCents());
        order.setPaymentType(ptype); order.setStatus("pending"); order.setPayUrl(gateway);
        order.setRequestIp(req.getHeader("x-forwarded-for") != null ? req.getHeader("x-forwarded-for") : req.getRemoteAddr());
        order.setDevice("pc"); vipOrderMapper.insert(order);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("orderNo", orderNo); r.put("planKey", planKey); r.put("planTitle", plan.getTitle());
        r.put("amount", plan.getPriceCents()); r.put("gatewayUrl", gateway); r.put("payUrl", gateway); return r;
    }

    @Override @Transactional
    public void handlePaymentNotify(HttpServletRequest req, HttpServletResponse resp) {
        try {
            Map<String, Object> params = new HashMap<>();
            Enumeration<String> names = req.getParameterNames();
            while (names.hasMoreElements()) { String n = names.nextElement(); params.put(n, req.getParameter(n)); }
            if (!zPaySignUtil.buildSign((Map) params, zpayPkey).equals(params.get("sign"))) { resp.getWriter().write("sign error"); return; }
            if (!"TRADE_SUCCESS".equals(params.get("trade_status"))) { resp.getWriter().write("success"); return; }
            String orderNo = (String) params.get("out_trade_no");
            VipOrder order = vipOrderMapper.selectByOrderNo(orderNo);
            if (order == null) { resp.getWriter().write("order not found"); return; }
            if ("paid".equals(order.getStatus())) { resp.getWriter().write("success"); return; }
            order.setStatus("paid"); order.setZpayTradeNo((String) params.get("trade_no"));
            order.setNotifyRaw(JSON.toJSONString(params)); order.setPaidAt(LocalDateTime.now());
            vipOrderMapper.updateById(order);
            grantVipPlan(order.getUserId(), order.getPlanKey());
            resp.getWriter().write("success");
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    @Override
    public Map<String, Object> getLatestVipPayment(Integer userId) {
        VipOrder order = vipOrderMapper.selectLatestByUserId(userId);
        User user = userMapper.selectById(userId); syncVipStatus(user);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("order", order != null ? buildOrderMap(order) : null);
        r.put("vipSnapshot", buildVipSnapshot(userMapper.selectById(userId))); return r;
    }

    @Override
    public Map<String, Object> cancelVipPayment(Integer userId, String orderNo) {
        VipOrder order = vipOrderMapper.selectByOrderNoAndUser(orderNo, userId);
        if (order == null) throw new BusinessException("订单不存在", 404);
        if (!"pending".equals(order.getStatus())) throw new BusinessException("订单不可取消", 400);
        order.setStatus("closed"); vipOrderMapper.updateById(order);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("order", buildOrderMap(order)); r.put("vipSnapshot", buildVipSnapshot(userMapper.selectById(userId))); return r;
    }

    @Override
    public Map<String, Object> getVipPaymentStatus(Integer userId, String orderNo) {
        VipOrder order = vipOrderMapper.selectByOrderNoAndUser(orderNo, userId);
        if (order == null) throw new BusinessException("订单不存在", 404);
        return buildOrderMap(order);
    }

    @Override @Transactional
    public Map<String, Object> redeemActivationCode(Integer userId, String code) {
        VipActivationCode vac = vacMapper.selectByCode(code);
        if (vac == null) throw new BusinessException("激活码不存在", 404);
        if ("used".equals(vac.getStatus())) throw new BusinessException("该激活码已被使用", 400);
        if ("disabled".equals(vac.getStatus())) throw new BusinessException("该激活码已被禁用", 400);
        User user = userMapper.selectById(userId); syncVipStatus(user);
        user = userMapper.selectById(userId);
        if ("lifetime".equals(user.getVipPlanKey())) throw new BusinessException("已是终身会员", 400);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = user.getVipExpiresAt() != null && user.getVipExpiresAt().isAfter(now) ? user.getVipExpiresAt() : now;
        user.setVipLevel(vac.getVipLevel());
        user.setVipPlanKey(user.getVipPlanKey() != null ? user.getVipPlanKey() : "activation");
        user.setVipExpiresAt(base.plusDays(vac.getValidDays())); userMapper.updateById(user);
        vac.setStatus("used"); vac.setUsedBy(userId); vac.setUsedAt(now); vacMapper.updateById(vac);
        Map<String, Object> r = new LinkedHashMap<>(); r.put("vipSnapshot", buildVipSnapshot(userMapper.selectById(userId))); return r;
    }

    private void grantVipPlan(Integer userId, String planKey) {
        User user = userMapper.selectById(userId); syncVipStatus(user);
        user = userMapper.selectById(userId);
        Map<String, Integer> planMonths = Map.of("monthly", 1, "quarterly", 3, "yearly", 12);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = user.getVipExpiresAt() != null && user.getVipExpiresAt().isAfter(now) ? user.getVipExpiresAt() : now;
        if ("lifetime".equals(planKey)) { user.setVipLevel("\u7EC8\u8EAB\u4F1A\u5458"); user.setVipPlanKey("lifetime"); user.setVipExpiresAt(null); }
        else { user.setVipLevel("VIP 1"); user.setVipPlanKey(planKey); user.setVipExpiresAt(base.plusMonths(planMonths.getOrDefault(planKey, 1))); }
        userMapper.updateById(user);
    }

    private void syncVipStatus(User user) {
        if (!"VIP 0".equals(user.getVipLevel()) && !"lifetime".equals(user.getVipPlanKey())
                && user.getVipExpiresAt() != null && user.getVipExpiresAt().isBefore(LocalDateTime.now())) {
            user.setVipLevel("VIP 0"); user.setVipPlanKey(null); user.setVipExpiresAt(null); userMapper.updateById(user);
        }
    }

    private Map<String, Object> buildOrderMap(VipOrder o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("orderNo", o.getOrderNo()); m.put("planKey", o.getPlanKey()); m.put("planTitle", o.getPlanTitle());
        m.put("amountCents", o.getAmountCents()); m.put("paymentType", o.getPaymentType());
        m.put("status", o.getStatus()); m.put("payUrl", o.getPayUrl());
        m.put("paidAt", o.getPaidAt()); m.put("createdAt", o.getCreatedAt()); return m;
    }

    private Map<String, Object> buildVipSnapshot(User u) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("vipLevel", u.getVipLevel()); s.put("vipPlanKey", u.getVipPlanKey());
        s.put("vipExpiresAt", u.getVipExpiresAt()); s.put("isLifetime", "lifetime".equals(u.getVipPlanKey())); return s;
    }
}
