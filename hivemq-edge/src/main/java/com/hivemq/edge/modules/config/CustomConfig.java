/*
 * Copyright 2019-present HiveMQ GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.edge.modules.config;

import com.hivemq.edge.HiveMQEdgeConstants;
import com.hivemq.extension.sdk.api.annotations.NotNull;

/** Marker interface **/

public interface CustomConfig {

    int PORT_MIN = 1;
    int PORT_MAX = HiveMQEdgeConstants.MAX_UINT16;
    int DEFAULT_PUBLISHING_INTERVAL = 1000;
    int DEFAULT_MAX_POLLING_ERROR_BEFORE_REMOVAL = 10;

    @NotNull String getId();
}
