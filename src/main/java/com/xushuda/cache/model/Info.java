package com.xushuda.cache.model;

/**
 * hold thread local variable to provide info of signature and annotation
 * 
 * @author xushuda
 *
 */
public class Info {

    private static final ThreadLocal<SignatureInfo> SIGNATURE = new ThreadLocal<SignatureInfo>();
    private static final ThreadLocal<AnnotationInfo> ANNOTATION = new ThreadLocal<AnnotationInfo>();

    public static void init(SignatureInfo signature, AnnotationInfo annotation) {
        Info.SIGNATURE.set(signature);
        Info.ANNOTATION.set(annotation);
    }

    /**
     * 释放资源
     */
    public static void unset() {
        Info.SIGNATURE.set(null);
        Info.ANNOTATION.set(null);
    }

    /**
     * get signature
     * 
     * @return
     */
    public static SignatureInfo getSignature() {
        return Info.SIGNATURE.get();
    }

    /**
     * get annotation
     * 
     * @return
     */
    public static AnnotationInfo getAnnotation() {
        return Info.ANNOTATION.get();
    }
}
