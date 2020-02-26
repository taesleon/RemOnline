package com.cardamon.tofa.skladhelper;

/**
 * Created by dima on 20.07.17.
 */

public interface DateSetObservable {
    void registerObserver(DateSetObserver o);
    void removeObserver(DateSetObserver o);
    void notifyObservers();
}
