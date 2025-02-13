/*
 * Copyright 2023-present HiveMQ GmbH
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
package com.hivemq.edge.adapters.opcua.client;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.hivemq.edge.adapters.opcua.OpcUaAdapterConfig;
import com.hivemq.edge.adapters.opcua.payload.OpcUaJsonPayloadConverter;
import com.hivemq.edge.adapters.opcua.payload.OpcUaStringPayloadConverter;
import com.hivemq.edge.modules.api.adapters.ProtocolAdapterPublishBuilder;
import com.hivemq.edge.modules.api.adapters.ProtocolAdapterPublishService;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.metrics.HiveMQMetrics;
import com.hivemq.mqtt.handler.publish.PublishReturnCode;
import com.hivemq.util.Bytes;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class OpcUaDataValueConsumer implements Consumer<DataValue> {
    private static final Logger log = LoggerFactory.getLogger(OpcUaDataValueConsumer.class);

    public static final byte[] EMTPY_BYTES = new byte[]{};

    private final @NotNull OpcUaAdapterConfig.Subscription subscription;
    private final @NotNull ProtocolAdapterPublishService adapterPublishService;
    private final @NotNull OpcUaClient opcUaClient;
    private final @NotNull NodeId nodeId;
    private final @NotNull Counter publishSuccessCounter;
    private final @NotNull Counter publishFailedCounter;
    private final @NotNull String adapterId;

    public OpcUaDataValueConsumer(
            final @NotNull OpcUaAdapterConfig.Subscription subscription,
            final @NotNull ProtocolAdapterPublishService adapterPublishService,
            final @NotNull OpcUaClient opcUaClient,
            final @NotNull NodeId nodeId,
            final @NotNull MetricRegistry metricRegistry,
            final @NotNull String adapterId) {
        this.subscription = subscription;
        this.adapterPublishService = adapterPublishService;
        this.opcUaClient = opcUaClient;
        this.nodeId = nodeId;
        this.adapterId = adapterId;

        final String adapterPrefix =
                HiveMQMetrics.PROTOCOL_ADAPTER_PREFIX + "opcua.client." + this.adapterId + ".read.publish.";
        publishSuccessCounter = metricRegistry.counter(adapterPrefix + "success.count");
        publishFailedCounter = metricRegistry.counter(adapterPrefix + "failed.count");
    }

    @Override
    public void accept(final @NotNull DataValue dataValue) {
        try {

            final ProtocolAdapterPublishBuilder publishBuilder = adapterPublishService.publish()
                    .withTopic(subscription.getMqttTopic())
                    .withPayload(convertPayload(dataValue, OpcUaAdapterConfig.PayloadMode.JSON))
                    .withQoS(subscription.getQos())
                    .withContextInformation("opcua-node-id", nodeId.toParseableString());

            if (subscription.getMessageExpiryInterval() != null) {
                publishBuilder.withMessageExpiryInterval(subscription.getMessageExpiryInterval());
            }

            try {

                final EndpointDescription endpoint = opcUaClient.getStackClient().getConfig().getEndpoint();
                if (endpoint != null) {
                    publishBuilder.withContextInformation("opcua-server-endpoint-url", endpoint.getEndpointUrl());
                    publishBuilder.withContextInformation("opcua-server-application-uri",
                            endpoint.getServer().getApplicationUri());
                }
            } catch (Exception e) {
                //ignore, but log
                log.debug("Not able to get dynamic context infos for OPC UA message for adapter {}", adapterId);
            }

            final CompletableFuture<PublishReturnCode> publishFuture = publishBuilder.send();

            publishFuture.thenAccept(publishReturnCode -> {
                publishSuccessCounter.inc();
            }).exceptionally(throwable -> {
                publishFailedCounter.inc();
                return null;
            });

        } catch (Exception e) {
            log.error("Error on creating MQTT publish from OPC-UA subscription for adapter {}", adapterId, e);
        }
    }

    private @NotNull byte[] convertPayload(
            DataValue dataValue, final @NotNull OpcUaAdapterConfig.PayloadMode payloadMode) {
        //null value, emtpy buffer
        if (dataValue.getValue().getValue() == null) {
            return EMTPY_BYTES;
        }

        if (payloadMode == null) {
            return Bytes.fromReadOnlyBuffer(OpcUaJsonPayloadConverter.convertPayload(opcUaClient, dataValue));
        }
        //option to choose different encoding types here -> string vs. json ...
        switch (payloadMode) {
            case STRING:
                return Bytes.fromReadOnlyBuffer(OpcUaStringPayloadConverter.convertPayload(dataValue));
            case JSON:
            default:
                return Bytes.fromReadOnlyBuffer(OpcUaJsonPayloadConverter.convertPayload(opcUaClient, dataValue));
        }
    }
}
