package com.fanap.podchat.requestobject;

public class RequestThreadInfo  extends GeneralRequestObject{
    private String image;
    private long threadId;
    private String name;
    private String description;
    private String metadata;

    RequestThreadInfo (Builder builder){
        super(builder);
        this.description = builder.description;
        this.image = builder.image;
        this.metadata = builder.metadata;
        this.name = builder.name;
        this.threadId = builder.threadId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public static class Builder extends GeneralRequestObject.Builder<Builder> {
        private String image;
        private String name;
        private String description;
        private String metadata;
        private long threadId;

        public Builder image(String image) {
            this.image = image;
            return this;
        }

        public Builder threadId(long threadId) {
            this.threadId = threadId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder metadat(String metadata) {
            this.metadata = metadata;
            return this;
        }

        public RequestThreadInfo build(){
            return new RequestThreadInfo(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
