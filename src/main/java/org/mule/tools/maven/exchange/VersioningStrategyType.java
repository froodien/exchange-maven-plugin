package org.mule.tools.maven.exchange;

import org.mule.tools.maven.exchange.api.Version;

import java.util.List;

public enum VersioningStrategyType {
    incremental {
        @Override
        public List<Version> mergeVersions(List<Version> existingVersions, List<Version> newVersions) {
            List<Version> returnVersions = existingVersions;
            for (Version version: newVersions) {
                returnVersions.add(version);
            }
            return returnVersions;
        }
    },
    byMuleRuntime {
        @Override
        public List<Version> mergeVersions(List<Version> existingVersions, List<Version> newVersions) {
            // TODO Implement
            return null;
        }
    },
    byVersionName {
        @Override
        public List<Version> mergeVersions(List<Version> existingVersions, List<Version> newVersions) {
            // TODO Implement
            return null;
        }
    },
    byDevKit {
        @Override
        public List<Version> mergeVersions(List<Version> existingVersions, List<Version> newVersions) {
            // TODO Implement
            return null;
        }
    };

    public List<Version> mergeVersions(List<Version> existingVersions, List<Version> newVersions) {
        return null;
    }
}
