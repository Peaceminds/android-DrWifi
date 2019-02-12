package com.attsinghua.dwf;

/*
 * 本类定义黑名单、白名单（GoodAp）表内容结构 供构建解析用List之用 在DB数据入库操作中也会使用
 */

public class ModelGoodBadAPData {
	
	private Long timeStampVersion;
    private String gdBSSID;
    
    /**
     * ##################################
     * 
     * 01 - 构造方法
     * 02 - 同名赋值方法
     * 
     * ##################################
     */
    // 01
    public ModelGoodBadAPData() {
        super();
    }
    
    // 02
    public ModelGoodBadAPData(Long gdVercode, String gdBSSID) {  
        super();  
        this.setTimeStampVersion(gdVercode);
        this.setGdBSSID(gdBSSID);
    }
    
    /**
     * ##################################
     * 
     * getter setter
     * 
     * ##################################
     */
	public Long getTimeStampVersion() {
		return timeStampVersion;
	}

	public void setTimeStampVersion(Long timeStampVersion) {
		this.timeStampVersion = timeStampVersion;
	}

	public String getGdBSSID() {
		return gdBSSID;
	}

	public void setGdBSSID(String gdBSSID) {
		this.gdBSSID = gdBSSID;
	}
    
    
}
