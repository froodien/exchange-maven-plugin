package org.mule.tools.maven.exchange.api;

public enum ExchangeObjectType {
    template {
        @Override
        public Integer id() {
            return EXCHANGE_OBJECT_TYPE_ID_TEMPLATE;
        }
    },
    connector {
        @Override
        public Integer id() {
            return EXCHANGE_OBJECT_TYPE_ID_CONNECTOR;
        }
    },
    example {
        @Override
        public Integer id() {
            return EXCHANGE_OBJECT_TYPE_ID_EXAMPLE;
        }
    };

    private static final Integer EXCHANGE_OBJECT_TYPE_ID_TEMPLATE = 1;
    private static final Integer EXCHANGE_OBJECT_TYPE_ID_CONNECTOR = 2;
    private static final Integer EXCHANGE_OBJECT_TYPE_ID_EXAMPLE = 3;

    public Integer id() {
        return 0;
    }
}
