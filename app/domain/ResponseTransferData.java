package domain;


public class ResponseTransferData {

    private Object responseData;
    private boolean success;
    private String responseCode;
    private String responseMessage;
    private String currentLoginName;
    private String currentUserName;

    public static ResponseTransferData build(Throwable e) {

        ResponseTransferData rtd = new ResponseTransferData();

        rtd.setSuccess(false);
        e.printStackTrace();
        return rtd;
    }

    public Object getResponseData() {
        return responseData;
    }

    public void setResponseData(Object responseData) {
        this.responseData = responseData;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getCurrentLoginName() {
        return currentLoginName;
    }

    public void setCurrentLoginName(String currentLoginName) {
        this.currentLoginName = currentLoginName;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
    }

}
