package com.blank.app.service.impl;

import com.alibaba.fastjson2.JSON;
import com.blank.app.entity.*;
import com.blank.app.exception.BusinessException;
import com.blank.app.mapper.*;
import com.blank.app.service.AdminService;
import com.blank.app.service.FileStorageService;
import com.blank.app.util.CodeGenerator;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired private UserMapper userMapper;
    @Autowired private PostcardMapper postcardMapper;
    @Autowired private StampMapper stampMapper;
    @Autowired private StampSeriesMapper stampSeriesMapper;
    @Autowired private VipActivationCodeMapper vacMapper;
    @Autowired private NotificationMapper notificationMapper;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private CodeGenerator codeGenerator;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("totalUsers", userMapper.countAll());
        o.put("totalAdmins", userMapper.countByIdentity("admin"));
        o.put("totalPostcards", postcardMapper.countAll());
        o.put("driftingPostcards", postcardMapper.countDrifting());
        o.put("totalStamps", stampMapper.countAll());
        o.put("totalSeries", stampSeriesMapper.selectAll().size());
        o.put("totalActivationCodes", vacMapper.countAll());
        o.put("unusedActivationCodes", vacMapper.countUnused());
        o.put("pendingManualReview", 0); return o;
    }

    @Override
    public Map<String, Object> getPostcards(int page, int pageSize, String keyword, String pt, String st) {
        PageHelper.startPage(page, pageSize);
        List<Postcard> records = postcardMapper.selectAdminPage(keyword, pt, st);
        long total = new com.github.pagehelper.PageInfo<>(records).getTotal();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Postcard p : records) {
            User author = userMapper.selectById(p.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId()); item.put("userId", p.getUserId()); item.put("title", p.getTitle());
            item.put("imageUrl", p.getImageUrl()); item.put("recipientInput", p.getRecipientInput());
            item.put("isPublic", p.getIsPublic() == 1); item.put("postcardType", p.getPostcardType());
            item.put("status", p.getStatus()); item.put("aspectRatio", p.getAspectRatio());
            item.put("canvasWidth", p.getCanvasWidth()); item.put("canvasHeight", p.getCanvasHeight());
            item.put("stampId", p.getStampId()); item.put("elementsRaw", p.getElements());
            item.put("authorUid", author != null ? author.getUid() : "");
            item.put("authorName", author != null ? author.getUsername() : "");
            item.put("senderDeleted", p.getSenderDeleted() == 1);
            item.put("recipientDeleted", p.getRecipientDeleted() == 1);
            item.put("createdAt", p.getCreatedAt());
            Stamp s = p.getStampId() != null ? stampMapper.selectById(p.getStampId()) : null;
            item.put("stampTitle", s != null ? s.getTitle() : "未使用"); list.add(item);
        }
        Map<String, Object> result = new LinkedHashMap<>(); result.put("list", list);
        Map<String, Object> pag = new LinkedHashMap<>();
        pag.put("total", total); pag.put("page", page); pag.put("pageSize", pageSize);
        pag.put("totalPages", (int)Math.ceil((double)total/pageSize));
        result.put("pagination", pag); return result;
    }

    @Override
    public Map<String, Object> createPostcard(Map<String, Object> body) {
        Integer uid = (Integer) body.get("userId");
        String iu = (String) body.get("imageUrl");
        if (uid == null || iu == null || iu.isEmpty()) throw new BusinessException("请填写用户ID与图片地址", 400);
        if (userMapper.selectById(uid) == null) throw new BusinessException("目标用户不存在", 404);
        String pt = (String) body.getOrDefault("postcardType", "normal");
        if (!"normal".equals(pt) && !"drifting".equals(pt)) throw new BusinessException("无效的明信片类型", 400);
        Postcard p = new Postcard();
        p.setUserId(uid); p.setTitle((String) body.getOrDefault("title", "")); p.setImageUrl(iu);
        p.setAspectRatio((String) body.getOrDefault("aspectRatio", "3/2"));
        p.setCanvasWidth(body.get("canvasWidth")!=null?(Integer)body.get("canvasWidth"):600);
        p.setCanvasHeight(body.get("canvasHeight")!=null?(Integer)body.get("canvasHeight"):400);
        p.setStampId(body.get("stampId")!=null?(Integer)body.get("stampId"):null);
        p.setRecipientInput((String) body.getOrDefault("recipientInput", ""));
        p.setIsPublic(Boolean.TRUE.equals(body.get("isPublic"))?1:0); p.setPostcardType(pt);
        p.setElements(body.get("elements")!=null?JSON.toJSONString(body.get("elements")):"[]");
        p.setStatus((String) body.getOrDefault("status", "sent"));
        p.setSenderDeleted(0); p.setRecipientDeleted(0); postcardMapper.insert(p);
        Map<String, Object> r = new LinkedHashMap<>(); r.put("id", p.getId()); return r;
    }

    @Override
    public void updatePostcard(Integer id, Map<String, Object> body) {
        Postcard p = postcardMapper.selectById(id);
        if (p == null) throw new BusinessException("明信片不存在", 404);
        if (body.containsKey("userId")) p.setUserId((Integer) body.get("userId"));
        if (body.containsKey("title")) p.setTitle((String) body.get("title"));
        if (body.containsKey("imageUrl")) p.setImageUrl((String) body.get("imageUrl"));
        if (body.containsKey("recipientInput")) p.setRecipientInput((String) body.get("recipientInput"));
        if (body.containsKey("isPublic")) p.setIsPublic(Boolean.TRUE.equals(body.get("isPublic"))?1:0);
        if (body.containsKey("postcardType")) p.setPostcardType((String) body.get("postcardType"));
        if (body.containsKey("status")) p.setStatus((String) body.get("status"));
        if (body.containsKey("stampId")) p.setStampId((Integer) body.get("stampId"));
        if (body.containsKey("elements")) p.setElements(JSON.toJSONString(body.get("elements")));
        if (body.containsKey("senderDeleted")) p.setSenderDeleted(Boolean.TRUE.equals(body.get("senderDeleted"))?1:0);
        if (body.containsKey("recipientDeleted")) p.setRecipientDeleted(Boolean.TRUE.equals(body.get("recipientDeleted"))?1:0);
        postcardMapper.updateById(p);
    }

    @Override public void deletePostcard(Integer id) { postcardMapper.deleteById(id); }

    @Override
    public List<Map<String, Object>> getStampSeries() {
        List<Map<String, Object>> r = new ArrayList<>();
        for (StampSeries s : stampSeriesMapper.selectAll()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId()); item.put("name", s.getName()); item.put("folderName", s.getFolderName());
            item.put("description", s.getDescription()); item.put("sortOrder", s.getSortOrder());
            item.put("stampCount", (int) stampMapper.countBySeriesId(s.getName()));
            item.put("createdAt", s.getCreatedAt()); r.add(item);
        }
        return r;
    }

    @Override public void createStampSeries(Map<String, Object> body) {
        String name = (String) body.get("name");
        if (name == null || name.trim().isEmpty()) throw new BusinessException("请输入系列名称", 400);
        if (stampSeriesMapper.selectByName(name.trim()) != null) throw new BusinessException("系列名称已存在", 409);
        StampSeries s = new StampSeries();
        s.setName(name.trim()); s.setFolderName((String) body.getOrDefault("folderName", null));
        s.setDescription((String) body.getOrDefault("description", ""));
        s.setSortOrder(body.get("sortOrder")!=null?(Integer)body.get("sortOrder"):0);
        stampSeriesMapper.insert(s);
    }

    @Override public void updateStampSeries(Integer id, Map<String, Object> body) {
        StampSeries s = stampSeriesMapper.selectById(id);
        if (s == null) throw new BusinessException("系列不存在", 404);
        String oldName = s.getName();
        if (body.containsKey("name")) s.setName(((String) body.get("name")).trim());
        if (body.containsKey("description")) s.setDescription((String) body.get("description"));
        if (body.containsKey("sortOrder")) s.setSortOrder((Integer) body.get("sortOrder"));
        if (!oldName.equals(s.getName())) stampMapper.updateStampsSeriesName(oldName, s.getName());
        stampSeriesMapper.updateById(s);
    }

    @Override public void deleteStampSeries(Integer id) {
        StampSeries s = stampSeriesMapper.selectById(id);
        if (s == null) throw new BusinessException("系列不存在", 404);
        if (stampMapper.countBySeriesId(s.getName()) > 0) throw new BusinessException("请先删除该系列下的邮票", 400);
        stampSeriesMapper.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> getStamps(String keyword, String seriesId) {
        List<Map<String, Object>> r = new ArrayList<>();
        for (Stamp s : stampMapper.selectByKeyword(keyword, seriesId)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId()); item.put("seriesId", s.getSeriesId()); item.put("title", s.getTitle());
            item.put("description", s.getDescription()); item.put("price", s.getPrice());
            item.put("imagePath", s.getImagePath()); item.put("createdAt", s.getCreatedAt()); r.add(item);
        }
        return r;
    }

    @Override public void createStamp(Map<String, Object> body) {
        String sid = (String) body.get("seriesId"), title = (String) body.get("title"), ip = (String) body.get("imagePath");
        if (sid == null || title == null || ip == null) throw new BusinessException("请填写完整信息", 400);
        Stamp s = new Stamp(); s.setSeriesId(sid); s.setTitle(title);
        s.setDescription((String) body.getOrDefault("description", ""));
        s.setPrice(body.get("price")!=null?(Integer)body.get("price"):5); s.setImagePath(ip); stampMapper.insert(s);
    }

    @Override public void updateStamp(Integer id, Map<String, Object> body) {
        Stamp s = stampMapper.selectById(id);
        if (s == null) throw new BusinessException("邮票不存在", 404);
        if (body.containsKey("seriesId")) s.setSeriesId((String) body.get("seriesId"));
        if (body.containsKey("title")) s.setTitle((String) body.get("title"));
        if (body.containsKey("description")) s.setDescription((String) body.get("description"));
        if (body.containsKey("price")) s.setPrice((Integer) body.get("price"));
        if (body.containsKey("imagePath")) s.setImagePath((String) body.get("imagePath"));
        stampMapper.updateById(s);
    }

    @Override public void deleteStamp(Integer id) {
        Stamp s = stampMapper.selectById(id);
        if (s == null) throw new BusinessException("邮票不存在", 404);
        stampMapper.deleteById(id);
        if (s.getImagePath() != null && (s.getImagePath().startsWith("/uploads/") || s.getImagePath().startsWith("/res/"))) {
            File f = new File(s.getImagePath().substring(1)); if (f.exists()) f.delete();
        }
    }

    @Override
    public Map<String, String> uploadStampImage(MultipartFile file, String seriesName) {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("imagePath", seriesName != null && !seriesName.isEmpty() ? fileStorageService.storeStampImageWithSeries(file, seriesName) : fileStorageService.storeStamp(file)); return r;
    }

    @Override
    public List<Map<String, Object>> getActivationCodes() {
        List<Map<String, Object>> r = new ArrayList<>();
        for (VipActivationCode c : vacMapper.selectAll()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId()); item.put("code", c.getCode()); item.put("vipLevel", c.getVipLevel());
            item.put("validDays", c.getValidDays()); item.put("status", c.getStatus());
            item.put("note", c.getNote()); item.put("usedAt", c.getUsedAt()); item.put("createdAt", c.getCreatedAt());
            if (c.getCreatedBy() != null) { User creator = userMapper.selectById(c.getCreatedBy()); item.put("createdByName", creator != null ? creator.getUsername() : "系统"); }
            if (c.getUsedBy() != null) { User ub = userMapper.selectById(c.getUsedBy()); item.put("usedByName", ub != null ? ub.getUsername() : ""); }
            r.add(item);
        }
        return r;
    }

    @Override @Transactional
    public Map<String, Object> generateActivationCodes(Integer userId, Map<String, Object> body) {
        int qty = Math.min(body.get("quantity")!=null?(Integer)body.get("quantity"):10, 50);
        String vl = (String) body.getOrDefault("vipLevel", "VIP 1");
        int vd = body.get("validDays")!=null?(Integer)body.get("validDays"):30;
        String note = (String) body.getOrDefault("note", "");
        for (int i = 0; i < qty; i++) {
            VipActivationCode vac = new VipActivationCode();
            vac.setCode(codeGenerator.generateActivationCode()); vac.setVipLevel(vl);
            vac.setValidDays(vd); vac.setStatus("unused"); vac.setNote(note); vac.setCreatedBy(userId);
            vacMapper.insert(vac);
        }
        Map<String, Object> r = new LinkedHashMap<>(); r.put("count", qty); return r;
    }

    @Override public void deleteActivationCode(Integer id) {
        vacMapper.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> getPendingPostcards() {
        List<Map<String, Object>> r = new ArrayList<>();
        for (Postcard p : postcardMapper.selectPendingList()) {
            User author = userMapper.selectById(p.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId()); item.put("userId", p.getUserId()); item.put("title", p.getTitle());
            item.put("image", p.getImageUrl()); item.put("aspectRatio", p.getAspectRatio());
            item.put("postcardType", p.getPostcardType());
            item.put("elements", p.getElements()!=null?JSON.parseArray(p.getElements()):new ArrayList<>());
            item.put("canvasWidth", p.getCanvasWidth()); item.put("canvasHeight", p.getCanvasHeight());
            item.put("imageOffset", Map.of("x", p.getImageOffsetX(), "y", p.getImageOffsetY()));
            item.put("imageScale", p.getImageScale()); item.put("imageRotation", p.getImageRotation());
            Stamp s = p.getStampId()!=null?stampMapper.selectById(p.getStampId()):null;
            item.put("stamp", s!=null?Map.of("title",s.getTitle(),"image",s.getImagePath()):null);
            item.put("recipientInput", p.getRecipientInput()); item.put("isPublic", p.getIsPublic()==1);
            item.put("status", p.getStatus()); item.put("reviewReason", p.getReviewReason());
            item.put("authorName", author!=null?author.getUsername():""); item.put("authorUid", author!=null?author.getUid():"");
            item.put("createdAt", p.getCreatedAt()); r.add(item);
        }
        return r;
    }

    @Override public void approvePostcard(Integer id) {
        Postcard p = postcardMapper.selectById(id);
        if (p == null) throw new BusinessException("明信片不存在", 404);
        if (!"pending".equals(p.getStatus())) throw new BusinessException("该明信片不在待审核状态", 400);
        p.setStatus("sent"); p.setReviewReason(null); postcardMapper.updateById(p);
    }

    @Override public void rejectPostcard(Integer id, String reason) {
        Postcard p = postcardMapper.selectById(id);
        if (p == null) throw new BusinessException("明信片不存在", 404);
        if (!"pending".equals(p.getStatus())) throw new BusinessException("该明信片不在待审核状态", 400);
        p.setStatus("rejected"); p.setReviewReason(reason); postcardMapper.updateById(p);
        Notification n = new Notification();
        n.setUserId(p.getUserId()); n.setType("review"); n.setTitle("明信片审核未通过");
        n.setContent("您的明信片\""+(p.getTitle()!=null?p.getTitle():"未命名")+"\"审核未通过，原因："+reason);
        n.setPostcardId(id); n.setIsRead(0); notificationMapper.insert(n);
    }
}
