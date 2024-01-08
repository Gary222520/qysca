package nju.edu.cn.qysca.exception;

import lombok.Data;

@Data
public class PlatformException extends RuntimeException {
    private int code;

    public PlatformException(int code, String message) {
        super(message);
        this.code = code;
    }

    public PlatformException(String message, Throwable root) {
        super(message, root);
    }

}

