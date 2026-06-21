package com.blank.app.service.impl;

import com.blank.app.entity.*;
import com.blank.app.exception.BusinessException;
import com.blank.app.mapper.*;
import com.blank.app.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired private CommentMapper commentMapper;
    @Autowired private CommentLikeMapper commentLikeMapper;
    @Autowired private PostcardMapper postcardMapper;
    @Autowired private UserMapper userMapper;

    @Override
    public List<Map<String, Object>> getComments(Integer postcardId, Integer userId) {
        List<Comment> comments = commentMapper.selectByPostcardId(postcardId);
        List<Integer> likedIds = userId != null ? commentLikeMapper.selectLikedIds(userId) : new ArrayList<>();
        List<Map<String, Object>> r = new ArrayList<>();
        for (Comment c : comments) {
            User author = userMapper.selectById(c.getUserId());
            if (author == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId()); item.put("text", c.getContent());
            item.put("pinned", c.getIsPinned() == 1); item.put("likes", c.getLikesCount());
            item.put("time", formatTime(c.getCreatedAt())); item.put("createdAt", c.getCreatedAt());
            item.put("author", author.getUsername()); item.put("authorAvatar", author.getAvatar());
            item.put("authorUid", author.getUid()); item.put("authorId", author.getId());
            item.put("liked", likedIds.contains(c.getId())); r.add(item);
        }
        return r;
    }

    @Override
    public Map<String, Object> addComment(Integer postcardId, Integer userId, String content) {
        content = content != null ? content.trim() : "";
        if (content.isEmpty()) throw new BusinessException("评论内容不能为空", 400);
        if (content.length() > 350) throw new BusinessException("评论最多350字", 400);
        if (postcardMapper.selectById(postcardId) == null) throw new BusinessException("明信片不存在", 404);
        Comment c = new Comment();
        c.setPostcardId(postcardId); c.setUserId(userId); c.setContent(content);
        c.setIsPinned(0); c.setLikesCount(0); commentMapper.insert(c);
        User author = userMapper.selectById(userId);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", c.getId()); r.put("text", c.getContent()); r.put("pinned", false);
        r.put("likes", 0); r.put("liked", false); r.put("time", "刚刚"); r.put("createdAt", c.getCreatedAt());
        r.put("author", author.getUsername()); r.put("authorAvatar", author.getAvatar());
        r.put("authorUid", author.getUid()); r.put("authorId", author.getId()); return r;
    }

    @Override @Transactional
    public void deleteComment(Integer commentId, Integer userId) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) throw new BusinessException("评论不存在", 404);
        Postcard p = postcardMapper.selectById(c.getPostcardId());
        if (!c.getUserId().equals(userId) && (p == null || !p.getUserId().equals(userId)))
            throw new BusinessException("无权删除", 403);
        commentLikeMapper.deleteByCommentId(commentId);
        commentMapper.deleteById(commentId);
    }

    @Override
    public Map<String, Object> togglePin(Integer commentId, Integer userId) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) throw new BusinessException("评论不存在", 404);
        Postcard p = postcardMapper.selectById(c.getPostcardId());
        if (p == null || !p.getUserId().equals(userId)) throw new BusinessException("只有明信片作者可以置顶", 403);
        c.setIsPinned(c.getIsPinned() == 1 ? 0 : 1); commentMapper.updateById(c);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("isPinned", c.getIsPinned() == 1); return r;
    }

    @Override
    public Map<String, Object> likeComment(Integer commentId, Integer userId) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) throw new BusinessException("评论不存在", 404);
        CommentLike existing = commentLikeMapper.selectByUserAndComment(userId, commentId);
        boolean liked;
        if (existing != null) { commentLikeMapper.deleteById(existing.getId()); c.setLikesCount(Math.max(0, c.getLikesCount() - 1)); liked = false; }
        else { CommentLike cl = new CommentLike(); cl.setUserId(userId); cl.setCommentId(commentId); commentLikeMapper.insert(cl); c.setLikesCount(c.getLikesCount() + 1); liked = true; }
        commentMapper.updateById(c);
        Map<String, Object> r = new LinkedHashMap<>(); r.put("liked", liked); r.put("likes", c.getLikesCount()); return r;
    }

    private String formatTime(LocalDateTime t) {
        if (t == null) return "";
        long m = Duration.between(t, LocalDateTime.now()).toMinutes();
        if (m < 1) return "刚刚"; if (m < 60) return m + "分钟前";
        long h = m / 60; if (h < 24) return h + "小时前";
        long d = h / 24; if (d < 30) return d + "天前";
        return t.getMonthValue() + "月" + t.getDayOfMonth() + "日";
    }
}
