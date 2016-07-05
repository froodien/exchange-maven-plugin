package org.mule.tools.maven.exchange.core;

import org.apache.maven.plugin.MojoExecutionException;
import org.mule.tools.maven.exchange.VersioningStrategyType;
import org.mule.tools.maven.exchange.api.ExchangeApi;
import org.apache.maven.plugin.logging.Log;
import org.mule.tools.maven.exchange.api.ExchangeObject;

import java.io.IOException;

public class ProjectPublisher {

    // TODO Refactor method
    public static void upsertExchangeObject(
            ExchangeApi exchangeApi,
            ExchangeObject exchangeObject,
            VersioningStrategyType versioningStrategy,
            Log log) throws MojoExecutionException {

        ExchangeObject currentExchangeObject = null;
        try {
            log.info("");
            log.info("Checking if Project already exists in Exchange...");
            currentExchangeObject = exchangeApi.getExchangeObject(exchangeObject);
        } catch (IOException e) {
            log.error("Get object from Exchange failed with error " + e.getMessage());
        }
        if (currentExchangeObject != null) {
            log.info("");
            log.info("Project found in Exchange, updating versions...");
            ExchangeObject finalExchangeObject =
                    mergeExchangeObjectsForUpdate(currentExchangeObject, exchangeObject, versioningStrategy);
            try {
                ExchangeObject updatedExchangeObject = exchangeApi.updateExchangeObject(finalExchangeObject);
                // TODO Transition state to the previous one
                // WIP -> WIP
                // Request for approval -> Request for approval
                // Published -> Published / Request for approval
                // exchangeApi.requestForPublishing(updatedExchangeObject);
            } catch (IOException e) {
                log.error("Update object in Exchange failed with error " + e.getMessage());
            }
            log.info("Project successfully updated");
        } else {
            try {
                log.info("");
                log.info("Project not found in Exchange, creating new entry...");
                exchangeObject.setOrganizationId(exchangeApi.getOrgId());
                ExchangeObject createdExchangeObject = exchangeApi.createExchangeObject(exchangeObject);
            } catch (IOException e) {
                log.error("Create object in Exchange failed with error " + e.getMessage());
            }
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
