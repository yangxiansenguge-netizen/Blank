package com.blank.app.dto.request;

import java.util.List;

public class BatchDeleteRequest {
    private List<Integer> ids;

    public List<Integer> getIds() { return ids; }
    public void setIds(List<Integer> ids) { this.ids = ids; }
}
