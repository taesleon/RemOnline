package com.cardamon.tofa.skladhelper.moysklad;

/**
 * если запрос прошел с какой то ошибкой, в активности вызывается метод 
 * Created by dima on 24.12.17.
 */

public interface BrokenRequest {
    void breakRequest();
}
