package io.terminus.spot.plugin.log.error;

public class StackElement {
    private String className;

    private String methodName;

    private String fileName;

    private int line;

    private int index;

    public int getLine() {
        return line;
    }

    public String getClassName() {
        return className;
    }

    public int getIndex() {
        return index;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setMethodName(String methodName) {
        if (methodName.contains("$")) {
            methodName = methodName.split("\\$")[0];
        }
        this.methodName = methodName;
    }

    public void setIndex(int order) {
        this.index = order;
    }
}
