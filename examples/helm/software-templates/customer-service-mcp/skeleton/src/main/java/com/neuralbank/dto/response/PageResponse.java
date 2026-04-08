package com.neuralbank.dto.response;

import java.util.List;

public class PageResponse<T> {

    public List<T> content;
    public int currentPage;
    public int totalPages;
    public long totalElements;
    public int numberOfElements;
    public int size;
    public boolean first;
    public boolean last;
    public boolean empty;
}
