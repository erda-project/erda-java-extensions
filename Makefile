#!/bin/bash

# Copyright (c) 2021 Terminus, Inc.
#
# This program is free software: you can use, redistribute, and/or modify
# it under the terms of the GNU Affero General Public License, version 3
# or later ("AGPL"), as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.

SHELL := /bin/bash -o pipefail
PROJ_PATH := $(shell dirname $(abspath $(lastword $(MAKEFILE_LIST))))
AGENT_OUTPUT_PATH := ${PROJ_PATH}/dist
VERSION_FILE := agent-core/src/main/resources/java-agent.properties
VERSION := $(shell head -n 1 ${PROJ_PATH}/${VERSION_FILE})
BINARY_PACKAGE_NAME := erda-java-agent.tar.gz
SKIP_TEST ?= false
BUILD_TIME := $(shell date "+%Y-%m-%d %H:%M:%S")
COMMIT_ID := $(shell git rev-parse HEAD 2>/dev/null)
GIT_BRANCH := $(shell git symbolic-ref --short -q HEAD)

.PHONY: all
all: build version push-oss

.PHONY: build
build: print-info clean set-maven
	./mvnw --batch-mode clean package -Dmaven.test.skip=$(SKIP_TEST)

.PHONY: clean
clean:
	@if [[ -d "${AGENT_OUTPUT_PATH}" ]]; then \
		rm -rf ${AGENT_OUTPUT_PATH}; \
	fi

.PHONY: version
version:
	cp -rv ${PROJ_PATH}/${VERSION_FILE} ${AGENT_OUTPUT_PATH}/agent_version

.PHONY: set-maven
set-maven:
	sed -i 's^{{BP_NEXUS_URL}}^'"${BP_NEXUS_URL}"'^g' /root/.m2/settings.xml
	sed -i 's^{{BP_NEXUS_USERNAME}}^'"${BP_NEXUS_USERNAME}"'^g' /root/.m2/settings.xml
	sed -i 's^{{BP_NEXUS_PASSWORD}}^'"${BP_NEXUS_PASSWORD}"'^g' /root/.m2/settings.xml

.PHONY: push-oss
push-oss:
	ossutil config -e ${OSS_DOMAIN} -i ${OSS_ACCESS_KEY} -k ${OSS_SECRET_KEY}
	ossutil cp -f ${AGENT_OUTPUT_PATH}/${BINARY_PACKAGE_NAME} oss://${OSS_PATH}/java-agent/${GIT_BRANCH}/${BINARY_PACKAGE_NAME}
	@echo "https://${OSS_PATH}.${OSS_DOMAIN}/java-agent/${GIT_BRANCH}/${BINARY_PACKAGE_NAME}"

.PHONY: print-info
print-info:
	@echo ------------ Start Build INFO ------------
	@echo Agent Version: ${VERSION}
	@echo Build Time: ${BUILD_TIME}
	@echo Commit ID: ${COMMIT_ID}
	@echo Skip Tests: ${SKIP_TEST}
	@echo Agent Output Path: ${AGENT_OUTPUT_PATH}
	@echo ------------ End   Build INFO ------------