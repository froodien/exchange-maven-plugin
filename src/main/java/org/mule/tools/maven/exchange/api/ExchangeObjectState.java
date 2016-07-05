package org.mule.tools.maven.exchange.api;

public enum ExchangeObjectState {
    work_in_progress {
        @Override
        public String id() {
            return EXCHANGE_OBJECT_STATE_WIP;
        }
    },
    waiting_for_approval {
        @Override
        public String id() {
            return EXCHANGE_OBJECT_STATE_WFA;
        }
    },
    published {
        @Override
        public String id() {
            return EXCHANGE_OBJECT_STATE_PUBLISHED;
        }
    },
    deleted {
        @Override
        public String id() {
            return EXCHANGE_OBJECT_STATE_DELETED;
        }
    };

    private static final String EXCHANGE_OBJECT_STATE_WIP = "wip";
    private static final String EXCHANGE_OBJECT_STATE_WFA = "wfa";
    private static final String EXCHANGE_OBJECT_STATE_PUBLISHED = "published";
    private static final String EXCHANGE_OBJECT_STATE_DELETED = "deleted";

    public String id() {
        return "";
    }
}
