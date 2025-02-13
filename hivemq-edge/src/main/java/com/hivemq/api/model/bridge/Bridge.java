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
package com.hivemq.api.model.bridge;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hivemq.api.model.connection.ConnectionStatus;
import com.hivemq.api.model.core.TlsConfiguration;
import com.hivemq.bridge.config.*;
import com.hivemq.edge.HiveMQEdgeConstants;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean to transport Bridge details across the API
 *
 * @author Simon L Johnson
 */
public class Bridge {

    @JsonProperty("id")
    @Schema(name = "id",
            description = "The bridge id, must be unique and only contain alpha numeric characters with spaces and hyphens.",
            format = "string",
            minLength = 1,
            required = true,
            maxLength = HiveMQEdgeConstants.MAX_ID_LEN,
            pattern = HiveMQEdgeConstants.ID_REGEX)
    private final @NotNull String id;

    @JsonProperty("host")
    @Schema(name = "host",
            description = "The host the bridge connects to - a well formed hostname, ipv4 or ipv6 value.",
            required = true,
            maxLength = 255)
    private final @NotNull String host;

    @JsonProperty("port")
    @Schema(name = "port",
            description = "The port number to connect to",
            required = true,
            minimum = "1",
            maximum = HiveMQEdgeConstants.MAX_UINT16_String)
    private final int port;

    @JsonProperty("clientId")
    @Schema(name = "clientId",
            description = "The client identifier associated the the MQTT connection.",
            format = "string",
            example = "my-example-client-id",
            nullable = true,
            maxLength = HiveMQEdgeConstants.MAX_UINT16)
    private final @NotNull String clientId;

    @JsonProperty("keepAlive")
    @Schema(name = "keepAlive",
            description = "The keepAlive associated the the MQTT connection.",
            required = true,
            defaultValue = "240",
            minimum = "0",
            maximum = HiveMQEdgeConstants.MAX_UINT16_String,
            format = "integer")
    private final int keepAlive;

    @JsonProperty("sessionExpiry")
    @Schema(name = "sessionExpiry",
            description = "The sessionExpiry associated the the MQTT connection.",
            required = true,
            defaultValue = "3600",
            minimum = "0",
            maximum = "4294967295",
            format = "integer")
    private final int sessionExpiry;

    @JsonProperty("cleanStart")
    @Schema(name = "cleanStart",
            description = "The cleanStart value associated the the MQTT connection.",
            required = true,
            defaultValue = "true",
            format = "boolean")
    private final boolean cleanStart;

    @JsonProperty("username")
    @Schema(name = "username",
            description = "The username value associated the the MQTT connection.",
            maxLength = HiveMQEdgeConstants.MAX_UINT16,
            format = "string",
            nullable = true)
    private final @Nullable String username;

    @JsonProperty("password")
    @Schema(name = "password",
            description = "The password value associated the the MQTT connection.",
            maxLength = HiveMQEdgeConstants.MAX_UINT16,
            format = "string",
            nullable = true)
    private final @Nullable String password;

    @JsonProperty("loopPreventionEnabled")
    @Schema(description = "Is loop prevention enabled on the connection",
            defaultValue = "true",
            format = "boolean")
    private final boolean loopPreventionEnabled;

    @JsonProperty("loopPreventionHopCount")
    @Schema(description = "Loop prevention hop count",
            defaultValue = "1",
            minimum = "0",
            maximum = "100",
            format = "integer")
    private final int loopPreventionHopCount;

    @JsonProperty("remoteSubscriptions")
    @Schema(description = "remoteSubscriptions associated with the bridge")
    private final @NotNull List<BridgeSubscription> remoteSubscriptions;

    @JsonProperty("localSubscriptions")
    @Schema(description = "localSubscriptions associated with the bridge")
    private final @NotNull List<BridgeSubscription> localSubscriptions;

    @JsonProperty("tlsConfiguration")
    @Schema(description = "tlsConfiguration associated with the bridge", nullable = true)
    private final @Nullable TlsConfiguration tlsConfiguration;

    @JsonProperty("bridgeRuntimeInformation")
    @Schema(description = "bridgeRuntimeInformation associated with the bridge", nullable = true)
    private final @Nullable BridgeRuntimeInformation bridgeRuntimeInformation;


    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Bridge(
            @NotNull @JsonProperty("id") final String id,
            @NotNull @JsonProperty("host") final String host,
            @NotNull @JsonProperty("port") final int port,
            @NotNull @JsonProperty("clientId") final String clientId,
            @NotNull @JsonProperty("keepAlive") final int keepAlive,
            @NotNull @JsonProperty("sessionExpiry") final int sessionExpiry,
            @NotNull @JsonProperty("cleanStart") final boolean cleanStart,
            @Nullable @JsonProperty("username") final String username,
            @Nullable @JsonProperty("password") final String password,
            @NotNull @JsonProperty("loopPreventionEnabled") final boolean loopPreventionEnabled,
            @NotNull @JsonProperty("loopPreventionHopCount") final int loopPreventionHopCount,
            @NotNull @JsonProperty("remoteSubscriptions") final List<BridgeSubscription> remoteSubscriptions,
            @NotNull @JsonProperty("localSubscriptions") final List<BridgeSubscription> localSubscriptions,
            @Nullable @JsonProperty("tlsConfiguration") final TlsConfiguration tlsConfiguration,
            @Nullable @JsonProperty("bridgeRuntimeInformation") final BridgeRuntimeInformation bridgeRuntimeInformation) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.clientId = clientId;
        this.keepAlive = keepAlive;
        this.sessionExpiry = sessionExpiry;
        this.cleanStart = cleanStart;
        this.username = username;
        this.password = password;
        this.loopPreventionEnabled = loopPreventionEnabled;
        this.loopPreventionHopCount = loopPreventionHopCount;
        this.remoteSubscriptions = remoteSubscriptions;
        this.localSubscriptions = localSubscriptions;
        this.tlsConfiguration = tlsConfiguration;
        this.bridgeRuntimeInformation = bridgeRuntimeInformation;
    }

    public BridgeRuntimeInformation getBridgeRuntimeInformation() {
        return bridgeRuntimeInformation;
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getClientId() {
        return clientId;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public int getSessionExpiry() {
        return sessionExpiry;
    }

    public boolean isCleanStart() {
        return cleanStart;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isLoopPreventionEnabled() {
        return loopPreventionEnabled;
    }

    public int getLoopPreventionHopCount() {
        return loopPreventionHopCount;
    }

    public List<BridgeSubscription> getRemoteSubscriptions() {
        return remoteSubscriptions;
    }

    public List<BridgeSubscription> getLocalSubscriptions() {
        return localSubscriptions;
    }

    public TlsConfiguration getTlsConfiguration() {
        return tlsConfiguration;
    }

    public static class BridgeSubscription {

        @JsonProperty("filters")
        @Schema(name = "filters",
                description = "The filters for this subscription.",
                required = true,
                example = "some/topic/value")
        private final @NotNull List<String> filters;

        @JsonProperty("destination")
        @Schema(name = "destination",
                description = "The destination topic for this filter set.",
                required = true,
                example = "some/topic/value")
        private final @NotNull String destination;

        @JsonProperty("excludes")
        @Schema(description = "The exclusion patterns", nullable = true)
        private final @Nullable List<String> excludes;

        @JsonProperty("customUserProperties")
        @Schema(description = "The customUserProperties for this subscription")
        private final @NotNull List<BridgeCustomUserProperty> customUserProperties;

        @JsonProperty("preserveRetain")
        @Schema(description = "The preserveRetain for this subscription")
        private final boolean preserveRetain;

        @JsonProperty("maxQoS")
        @Schema(name = "maxQoS",
                description = "The maxQoS for this subscription.",
                format = "number",
                required = true,
                defaultValue = "0",
                allowableValues = {"0", "1", "2"},
                minimum = "0",
                maximum = "2")
        private final int maxQoS;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public BridgeSubscription(
                @NotNull @JsonProperty("filters") final List<String> filters,
                @NotNull @JsonProperty("destination") final String destination,
                @Nullable @JsonProperty("excludes") final List<String> excludes,
                @NotNull @JsonProperty("customUserProperties") final List<BridgeCustomUserProperty> customUserProperties,
                @JsonProperty("preserveRetain") final boolean preserveRetain,
                @JsonProperty("maxQoS") final int maxQoS) {
            this.filters = filters;
            this.destination = destination;
            this.excludes = excludes;
            this.customUserProperties = customUserProperties;
            this.preserveRetain = preserveRetain;
            this.maxQoS = maxQoS;
        }

        public @NotNull List<String> getFilters() {
            return filters;
        }

        public @NotNull String getDestination() {
            return destination;
        }

        public @Nullable List<String> getExcludes() {
            return excludes;
        }

        public @NotNull List<BridgeCustomUserProperty> getCustomUserProperties() {
            return customUserProperties;
        }

        public boolean isPreserveRetain() {
            return preserveRetain;
        }

        public int getMaxQoS() {
            return maxQoS;
        }
    }

    public static class BridgeCustomUserProperty {

        @JsonProperty("key")
        @Schema(description = "The key the from the property", required = true, format = "string")
        private final @NotNull String key;

        @JsonProperty("value")
        @Schema(description = "The value the from the property", required = true, format = "string")
        private final @NotNull String value;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public BridgeCustomUserProperty(
                @NotNull @JsonProperty("key") final String key, @NotNull @JsonProperty("value") final String value) {
            this.key = key;
            this.value = value;
        }

        public @NotNull String getKey() {
            return key;
        }

        public @NotNull String getValue() {
            return value;
        }
    }

    public static Bridge convert(MqttBridge mqttBridge, ConnectionStatus status, String lastError) {

        BridgeRuntimeInformation runtimeInformation = new BridgeRuntimeInformation(status, lastError);
        Bridge bridge = new Bridge(mqttBridge.getId(),
                mqttBridge.getHost(),
                mqttBridge.getPort(),
                mqttBridge.getClientId(),
                mqttBridge.getKeepAlive(),
                mqttBridge.getSessionExpiry(),
                mqttBridge.isCleanStart(),
                mqttBridge.getUsername(),
                mqttBridge.getPassword(),
                mqttBridge.isLoopPreventionEnabled(),
                mqttBridge.getLoopPreventionHopCount() < 1 ? 0 : mqttBridge.getLoopPreventionHopCount(),
                mqttBridge.getRemoteSubscriptions()
                        .stream()
                        .map(m -> convertSubscription(m))
                        .collect(Collectors.toList()),
                mqttBridge.getLocalSubscriptions()
                        .stream()
                        .map(m -> convertSubscription(m))
                        .collect(Collectors.toList()),
                convertTls(mqttBridge.getBridgeTls()), runtimeInformation);
        return bridge;
    }

    public static BridgeSubscription convertSubscription(LocalSubscription localSubscription) {
        if (localSubscription == null) {
            return null;
        }
        BridgeSubscription subscription = new BridgeSubscription(localSubscription.getFilters(),
                localSubscription.getDestination(),
                localSubscription.getExcludes(),
                localSubscription.getCustomUserProperties()
                        .stream()
                        .map(f -> convertProperty(f))
                        .collect(Collectors.toList()),
                localSubscription.isPreserveRetain(),
                localSubscription.getMaxQoS());
        return subscription;
    }

    public static BridgeSubscription convertSubscription(RemoteSubscription remoteSubscription) {
        if (remoteSubscription == null) {
            return null;
        }
        BridgeSubscription subscription = new BridgeSubscription(remoteSubscription.getFilters(),
                remoteSubscription.getDestination(),
                null,
                remoteSubscription.getCustomUserProperties()
                        .stream()
                        .map(f -> convertProperty(f))
                        .collect(Collectors.toList()),
                remoteSubscription.isPreserveRetain(),
                remoteSubscription.getMaxQoS());
        return subscription;
    }

    public static BridgeCustomUserProperty convertProperty(CustomUserProperty customUserProperty) {
        if (customUserProperty == null) {
            return null;
        }
        BridgeCustomUserProperty property =
                new BridgeCustomUserProperty(customUserProperty.getKey(), customUserProperty.getValue());
        return property;
    }

    public static TlsConfiguration convertTls(BridgeTls tls) {
        if (tls == null) {
            return null;
        }
        TlsConfiguration tlsConfiguration = new TlsConfiguration(true,
                tls.getKeystorePath(),
                tls.getKeystorePassword(),
                tls.getPrivateKeyPassword(),
                tls.getTruststorePath(),
                tls.getTruststorePassword(),
                tls.getProtocols(),
                tls.getCipherSuites(),
                tls.getKeystoreType(),
                tls.getTruststoreType(),
                tls.isVerifyHostname(),
                tls.getHandshakeTimeout());
        return tlsConfiguration;
    }
}
