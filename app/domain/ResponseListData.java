package domain;


public class ResponseListData {

    private Object datas;
    private Long totalRecordNums;
    private Integer page;

    public Object getDatas() {
        return datas;
    }

    public void setDatas(Object datas) {
        this.datas = datas;
    }

    public Long getTotalRecordNums() {
        return totalRecordNums;
    }

    public void setTotalRecordNums(Long totalRecordNums) {
        this.totalRecordNums = totalRecordNums;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}
