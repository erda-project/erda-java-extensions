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
VERSION := $(shell head -n 1 ${PROJ_PATH}/agent-core/src/main/resources/java-agent.properties)
SKIP_TEST ?= false

# build info
BUILD_TIME := $(shell date "+%Y-%m-%d %H:%M:%S")
COMMIT_ID := $(shell git rev-parse HEAD 2>/dev/null)

.PHONY: all
all: print-info clean
	@cd $(PROJ_PATH) && mvn --batch-mode clean package -Dmaven.test.skip=$(SKIP_TEST)

.PHONY: clean
clean:
	@cd $(PROJ_PATH) && rm -rf ${AGENT_OUTPUT_PATH}

.PHONY: print-info
print-info:
	@echo ------------ Start Build INFO ------------
	@echo Agent Version: ${VERSION}
	@echo Build Time: ${BUILD_TIME}
	@echo Commit ID: ${COMMIT_ID}
	@echo Skip Tests: ${SKIP_TEST}
	@echo Agent Output Path: ${AGENT_OUTPUT_PATH}
	@echo ------------ End   Build INFO ------------