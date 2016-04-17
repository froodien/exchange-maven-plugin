package org.mule.tools.maven.exchange;

public enum ExchangeApiVersion {
    v15("/exchange/api/%s/objects") {
        public String buildExchangeObjectsPath(ExchangeApi exchangeApi){
            return String.format(
                    exchangeObjectsPathTemplate,
                    exchangeApi.getUser().getAccount().getOrganization().getDomain()
            );
        }
    },
    v16("/exchange/api/organizations/%s/objects"){
        public String buildExchangeObjectsPath(ExchangeApi exchangeApi){
            return String.format(
                    exchangeObjectsPathTemplate,
                    exchangeApi.getOrgId()
            );
        }
    };

    public String exchangeObjectsPathTemplate;

    ExchangeApiVersion(String exchangeObjectsPathTemplate){
        this.exchangeObjectsPathTemplate = exchangeObjectsPathTemplate;
    };

    public String buildExchangeObjectsPath(ExchangeApi exchangeApi){
        return "";
    }
}
