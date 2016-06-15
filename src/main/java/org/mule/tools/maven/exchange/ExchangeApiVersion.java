package org.mule.tools.maven.exchange;

import org.mule.tools.maven.exchange.api.ExchangeObject;

public enum ExchangeApiVersion {
    v15("/exchange/api/%s/objects") {
        public String buildExchangeObjectsPath(ExchangeApi exchangeApi, ExchangeObject exchangeObject){
            if (exchangeObject == null) {
                return String.format(
                        exchangeObjectsPathTemplate,
                        exchangeApi.getUser().getAccount().getOrganization().getDomain()
                );
            } else {
                return String.format(
                        exchangeObjectsPathTemplate,
                        exchangeApi.getUser().getAccount().getOrganization().getDomain()
                ) + "/" + exchangeObject.getId();
            }
        }

        public String buildExchangeObjectsDomainPath(ExchangeApi exchangeApi){
            return String.format(
                    exchangeObjectsPathTemplate,
                    exchangeApi.getUser().getAccount().getOrganization().getDomain()
            );
        }
    },
    v16("/exchange/api/organizations/%s/objects"){
        public String buildExchangeObjectsPath(ExchangeApi exchangeApi, ExchangeObject exchangeObject){
            if (exchangeObject.getId() == null) {
                return String.format(
                        exchangeObjectsPathTemplate,
                        exchangeObject.getOrganizationId()
                );
            } else {
                return String.format(
                        exchangeObjectsPathTemplate,
                        exchangeObject.getOrganizationId()
                ) + "/" + exchangeObject.getId();
            }
        }

        public String buildExchangeObjectsDomainPath(ExchangeApi exchangeApi){
            return String.format(
                    exchangeObjectsPathTemplate,
                    exchangeApi.getUser().getAccount().getOrganization().getDomain()
            );
        }
    };

    public String exchangeObjectsPathTemplate;

    ExchangeApiVersion(String exchangeObjectsPathTemplate){
        this.exchangeObjectsPathTemplate = exchangeObjectsPathTemplate;
    };

    public String buildExchangeObjectsPath(ExchangeApi exchangeApi, ExchangeObject exchangeObject){
        return "";
    }

    public String buildExchangeObjectsDomainPath(ExchangeApi exchangeApi){
        return "";
    }

}
