package com.fanap.podchat.requestobject;

import android.support.annotation.NonNull;

public class RequestPinThread extends GeneralRequestObject {
    private long threadId;

    RequestPinThread(@NonNull Builder builder) {
        super(builder);
        this.threadId = builder.threadId;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public static class Builder extends GeneralRequestObject.Builder<Builder> {
        private long threadId;

        public Builder(long threadId) {
            this.threadId = threadId;
        }

        @NonNull
        public RequestPinThread build() {
            return new RequestPinThread(this);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
