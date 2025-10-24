#!/bin/bash

#
# Copyright (c) 2021 Terminus, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -o errexit -o nounset -o pipefail

DIST_DIR="$(echo dist/erda-java-agent)"

mkdir -p "${DIST_DIR}"

BUILD_VERSION="$(date '+%Y%m%d%H%M')-$(git rev-parse --short HEAD)"
echo "BUILD_VERSION: ${BUILD_VERSION}"

cat > "dist/erda-java-agent/build" <<EOF
${BUILD_VERSION}
EOF

# in darwin os, use this command choose jdk 8 version JAVA_HOME=`/usr/libexec/java_home -v 1.8.0_371`
mvn clean package -Dmaven.test.skip=true