package com.yealink.ims.fileshare.event;

/**
 * http服务类型 根据uri路径
 * author:pengzhiyuan
 * Created on:2016/7/20.
 */
public enum PathType {
    AVATAR("/avatar"),
    MONITOR("/monitor"),
    IMAGE("/image");

    private String path;
    private PathType(String path) {
        this.path = path;
    }

    public static PathType valuesOf(String path) {
        PathType[] pathTypes = PathType.values();
        if (pathTypes == null || pathTypes.length == 0) {
            return null;
        }
        for (PathType pathType : pathTypes) {
            if (pathType.path.equals(path)) {
                return pathType;
            }
        }
        return null;
    }

}
