package id.fahmikudo.persistence.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Page - Pagination result wrapper
 */
public record Page<T>(List<T> content, long totalElements, int pageNumber, int pageSize) {
    public Page(List<T> content, long totalElements, int pageNumber, int pageSize) {
        this.content = content != null ? content : new ArrayList<>();
        this.totalElements = totalElements;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
    }

    public boolean hasNext() {
        return pageNumber < getTotalPages() - 1;
    }

    public boolean hasPrevious() {
        return pageNumber > 0;
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public int getNumberOfElements() {
        return content.size();
    }
}

