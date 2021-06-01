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
DIST_PATH := ${PROJ_PATH}/dist
VERSION_FILE := agent-core/src/main/resources/java-agent.properties
VERSION := $(shell head -n 1 ${PROJ_PATH}/${VERSION_FILE})
SKIP_TEST ?= true
BUILD_TIME := $(shell date "+%Y-%m-%d %H:%M:%S")
COMMIT_ID := $(shell git rev-parse HEAD 2>/dev/null)
GIT_BRANCH := $(shell git symbolic-ref --short -q HEAD)

.PHONY: all
all: build

.PHONY: ci-build
ci-build: print-info clean set-maven
	./mvnw --batch-mode clean package -Dmaven.test.skip=$(SKIP_TEST)

.PHONY: build
build: print-info clean
	./scripts/build.sh

.PHONY: clean
clean:
	@if [[ -d "${DIST_PATH}" ]]; then \
		rm -rf ${DIST_PATH}; \
	fi
	mkdir -p ${DIST_PATH}/erda-java-agent

.PHONY: set-maven
set-maven:
	@sed -i 's^{{BP_NEXUS_URL}}^'"${BP_NEXUS_URL}"'^g' /root/.m2/settings.xml
	@sed -i 's^{{BP_NEXUS_USERNAME}}^'"${BP_NEXUS_USERNAME}"'^g' /root/.m2/settings.xml
	@sed -i 's^{{BP_NEXUS_PASSWORD}}^'"${BP_NEXUS_PASSWORD}"'^g' /root/.m2/settings.xml

.PHONY: print-info
print-info:
	@echo ------------ Start Build INFO ------------
	@echo Agent Version: ${VERSION}
	@echo Build Time: ${BUILD_TIME}
	@echo Commit ID: ${COMMIT_ID}
	@echo Skip Tests: ${SKIP_TEST}
	@echo Dist Path: ${DIST_PATH}
	@echo ------------ End   Build INFO ------------

.PHONY: pack-compatible
pack-compatible: build
	@cp -r ${DIST_PATH}/erda-java-agent ${DIST_PATH}/spot-agent
	@cp -f ${DIST_PATH}/spot-agent/erda-agent.jar ${DIST_PATH}/spot-agent/spot-agent.jar
	@rm -f ${DIST_PATH}/spot-agent/erda-agent.jar
	@cd ${DIST_PATH} && tar -zcvf spot-agent.tar.gz spot-agent