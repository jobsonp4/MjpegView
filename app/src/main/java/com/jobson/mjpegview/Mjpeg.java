package com.jobson.mjpegview;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class Mjpeg {

    public enum Type {
        DEFAULT, NATIVE
    }

    private final Type type;
    
    private boolean sendConnectionCloseHeader = false;

    private Mjpeg(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("null type not allowed");
        }
        this.type = type;
    }

    public static Mjpeg newInstance() {
        return new Mjpeg(Type.DEFAULT);
    }


    public static Mjpeg newInstance(Type type) {
        return new Mjpeg(type);
    }

    public Mjpeg credential(String username, String password) {
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password.toCharArray());
                }
            });
        }
        return this;
    }

    public Mjpeg sendConnectionCloseHeader() {
        sendConnectionCloseHeader = true;
        return this;
    }

    @NonNull
    private Observable<MjpegInputStream> connect(String url) {
        return Observable.defer(() -> {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
                if (sendConnectionCloseHeader) {
                    urlConnection.setRequestProperty("Connection", "close");
                }
                InputStream inputStream = urlConnection.getInputStream();
                switch (type) {

                    case DEFAULT:
                        return Observable.just(new MjpegInputStreamDefault(inputStream));
                    case NATIVE:
                        return Observable.just(new MjpegInputStreamNative(inputStream));
                }
                throw new IllegalStateException("invalid type");
            } catch (IOException e) {
                return Observable.error(e);
            }
        });
    }

    public Observable<MjpegInputStream> open(String url) {
        return connect(url)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<MjpegInputStream> open(String url, int timeout) {
        return connect(url)
            .timeout(timeout, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

}
