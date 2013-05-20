package com.race604.http;

public class MultipartFile {

    /**
     * Multipart name 接收参数名
     */
    protected String name;

    protected String fileName;

    protected String fileType;

    protected String filePath;

    protected long fileSize;

    public MultipartFile(String filePath) {
        super();
        this.filePath = filePath;
    }

    public MultipartFile(String name, String type, String filePath) {
        super();
        this.name = name;
        this.fileType = type;
        this.filePath = filePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return fileType;
    }

    public void setType(String type) {
        this.fileType = type;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getSize() {
        return fileSize;
    }

    public void setSize(long size) {
        this.fileSize = size;
    }
}
