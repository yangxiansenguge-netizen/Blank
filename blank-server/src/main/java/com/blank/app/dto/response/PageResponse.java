package com.blank.app.dto.response;

import java.util.List;

public class PageResponse<T> {
    private List<T> list;
    private Pagination pagination;

    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }
    public Pagination getPagination() { return pagination; }
    public void setPagination(Pagination pagination) { this.pagination = pagination; }

    public static class Pagination {
        private long total;
        private int page;
        private int pageSize;
        private int totalPages;

        public Pagination() {}

        public Pagination(long total, int page, int pageSize) {
            this.total = total;
            this.page = page;
            this.pageSize = pageSize;
            this.totalPages = (int) Math.ceil((double) total / pageSize);
        }

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }

    public static <T> ApiResponse<PageResponse<T>> paginate(List<T> list, long total, int page, int pageSize) {
        PageResponse<T> body = new PageResponse<>();
        body.setList(list);
        body.setPagination(new Pagination(total, page, pageSize));
        return ApiResponse.success(body);
    }
}
