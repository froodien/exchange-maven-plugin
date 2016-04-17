package org.mule.tools.maven.exchange;

import org.mule.tools.maven.exchange.api.Version;

import java.util.List;

public enum VersioningStrategyType {
    incremental {
        @Override
        public List<Version> mergeVersions(List<Version> existingVersions, List<Version> newVersions) {
            for (Version newVersion: newVersions) {
                existingVersions.add(newVersion);
            }
            return existingVersions;
        }
    },
    byMuleRuntime {
        @Override
        public List<Version> mergeVersions(List<Version> existingVersions, List<Version> newVersions) {
            for (Version newVersion: newVersions) {
                boolean added = false;
                for (Version existingVersion: existingVersions){
                    if (existingVersion.getMuleVersionId().equals(newVersion.getMuleVersionId())) {
                        existingVersions.set(
                                existingVersions.indexOf(existingVersion),
                                newVersion);
                        added = true;
                    }
                }
                if (!added) {
                    existingVersions.add(newVersion);
                }
            }
            return existingVersions;
        }
    },
    byVersionName {
        @Override
        public List<Version> mergeVersions(List<Version> existingVersions, List<Version> newVersions) {
            for (Version newVersion: newVersions) {
                boolean added = false;
                for (Version existingVersion: existingVersions){
                    if (existingVersion.getObjectVersion().equals(newVersion.getObjectVersion())) {
                        existingVersions.set(
                                existingVersions.indexOf(existingVersion),
                                newVersion);
                        added = true;
                    }
                }
                if (!added) {
                    existingVersions.add(newVersion);
                }
            }
            return existingVersions;
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
