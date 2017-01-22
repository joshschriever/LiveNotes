package com.joshschriever.livenotes.task;

import android.os.AsyncTask;

public abstract class SingleParamResultlessTask<T> extends AsyncTask<T, Void, Void> {

    protected abstract void doInBackground(T param);

    @Override
    protected final Void doInBackground(T... params) {
        doInBackground(params[0]);
        return null;
    }

    public final void execute(T param) {
        super.execute(param);
    }

}
