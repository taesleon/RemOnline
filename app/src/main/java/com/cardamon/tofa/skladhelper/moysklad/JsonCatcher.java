package com.cardamon.tofa.skladhelper.moysklad;

import org.json.JSONObject;
import java.net.URL;

/**
 * ловим json от сервера
 * Created by dima on 07.12.17.
 */

public interface JsonCatcher {
    void catchJson(final JSONObject json);
    void catchJson(final int code, final URL url);

}

