package org.mule.tools.maven.exchange.core;

import org.mule.tools.maven.exchange.VersioningStrategyType;
import org.mule.tools.maven.exchange.api.ExchangeApi;
import org.mule.tools.maven.exchange.api.ExchangeObject;
import org.mule.tools.maven.exchange.api.ExchangeObjectState;
import org.apache.maven.plugin.logging.Log;
import java.io.IOException;

public class ProjectPublisher {

    public static void upsertExchangeObject(
            ExchangeApi exchangeApi,
            ExchangeObject exchangeObject,
            VersioningStrategyType versioningStrategy,
            Log log) throws IOException {

        log.info("");
        log.info("Checking if Project already exists in Exchange...");
        ExchangeObject currentExchangeObject = exchangeApi.getExchangeObject(exchangeObject);

        if (currentExchangeObject != null) {
            log.info("");
            log.info("Project found in Exchange, updating versions...");
            ExchangeObject finalExchangeObject =
                    mergeExchangeObjectsForUpdate(currentExchangeObject, exchangeObject, versioningStrategy);
            ExchangeObject updatedExchangeObject = exchangeApi.updateExchangeObject(finalExchangeObject);

            // State transition scenarios:
            // WIP -> WIP
            // Request for approval -> Request for approval
            // Published -> Published / Request for approval
            if ((currentExchangeObject.getState().equals(ExchangeObjectState.published.id())) ||
                    (currentExchangeObject.getState().equals(ExchangeObjectState.waiting_for_approval.id()))){
                exchangeApi.requestForPublishing(updatedExchangeObject);
            }
            log.info("Project successfully updated");
        } else {
            log.info("");
            log.info("Project not found in Exchange, creating new entry...");
            exchangeObject.setOrganizationId(exchangeApi.getOrgId());
            // Objects are created in "Work In Progress" state
            exchangeApi.createExchangeObject(exchangeObject);
            log.info("Project successfully created");
        }
    }

    private static ExchangeObject mergeExchangeObjectsForUpdate(
            ExchangeObject currentExchangeObject,
            ExchangeObject newExchangeObject,
            VersioningStrategyType versioningStrategy) {

        currentExchangeObject.setVersions(
                versioningStrategy.mergeVersions(
                        currentExchangeObject.getVersions(),
                        newExchangeObject.getVersions()
                )
        );

        return currentExchangeObject;
    }

}
