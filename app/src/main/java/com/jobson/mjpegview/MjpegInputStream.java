package com.jobson.mjpegview;

import java.io.DataInputStream;
import java.io.InputStream;

public abstract class MjpegInputStream extends DataInputStream {

    public MjpegInputStream(InputStream in) {
        super(in);
    }

}
