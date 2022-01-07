package com.ht.orm;

/**
 * 分页参数
 * @author wkkyo
 * @date 2018/9/14
 */
public class Page {

    /**
     * 总记录数
     */
    private long totalCount = 0;

    private long pageSize = 20;

    /**
     * 当前页
     */
    private long pageNum = 1;

    /**
     * 总页数
     * @return
     */
    public long getTotalPage() {
        if(totalCount%pageSize == 0){
            return totalCount/pageSize;
        }else{
            return totalCount/pageSize+1;
        }
    }

    public long getPrePage() {
        return pageNum-1;
    }

    public long getNextPage() {
        return pageNum+1;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        if(pageNum < 1){
            this.pageNum = 1;
        }else {
            this.pageNum = pageNum;
        }
    }

    public long getOffset(){
        return (pageNum-1)*pageSize;
    }

    public void reset(){
        totalCount = 0;
        pageNum = 1;
    }
}
