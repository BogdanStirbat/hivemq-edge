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
package com.hivemq.api.resources.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.google.common.collect.ImmutableList;
import com.hivemq.api.AbstractApi;
import com.hivemq.api.model.ApiConstants;
import com.hivemq.api.model.ApiErrorMessages;
import com.hivemq.api.model.adapters.Adapter;
import com.hivemq.api.model.adapters.AdapterRuntimeInformation;
import com.hivemq.api.model.adapters.AdaptersList;
import com.hivemq.api.model.adapters.ProtocolAdapter;
import com.hivemq.api.model.adapters.ProtocolAdaptersList;
import com.hivemq.api.model.adapters.ValuesTree;
import com.hivemq.api.model.connection.ConnectionStatus;
import com.hivemq.api.model.connection.ConnectionStatusList;
import com.hivemq.api.model.connection.ConnectionStatusTransitionCommand;
import com.hivemq.api.resources.ProtocolAdaptersApi;
import com.hivemq.api.utils.ApiErrorUtils;
import com.hivemq.api.utils.ApiUtils;
import com.hivemq.configuration.service.ConfigurationService;
import com.hivemq.edge.HiveMQEdgeConstants;
import com.hivemq.edge.modules.adapters.impl.ProtocolAdapterDiscoveryOutputImpl;
import com.hivemq.edge.modules.adapters.params.ProtocolAdapterDiscoveryInput;
import com.hivemq.edge.modules.api.adapters.ProtocolAdapterInformation;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.protocols.AdapterInstance;
import com.hivemq.protocols.ProtocolAdapterManager;
import com.hivemq.protocols.ProtocolAdapterUtils;
import com.hivemq.protocols.params.NodeTreeImpl;
import com.networknt.schema.ValidationMessage;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProtocolAdaptersResourceImpl extends AbstractApi implements ProtocolAdaptersApi {

    private final @NotNull ConfigurationService configurationService;
    private final @NotNull ProtocolAdapterManager protocolAdapterManager;
    private final @NotNull ObjectMapper objectMapper;

    @Inject
    public ProtocolAdaptersResourceImpl(
            final @NotNull ConfigurationService configurationService,
            final @NotNull ProtocolAdapterManager protocolAdapterManager,
            final @NotNull ObjectMapper objectMapper) {
        this.configurationService = configurationService;
        this.protocolAdapterManager = protocolAdapterManager;
        this.objectMapper = ProtocolAdapterUtils.createProtocolAdapterMapper(objectMapper);
    }

    @Override
    public @NotNull Response getAdapterTypes() {
        final ImmutableList.Builder<ProtocolAdapter> adapters = ImmutableList.builder();
        for (ProtocolAdapterInformation info : protocolAdapterManager.getAllAvailableAdapterTypes().values()) {
            String logoUrl = info.getLogoUrl();
            if(Boolean.getBoolean(HiveMQEdgeConstants.DEVELOPMENT_MODE)){
                //-- when we're in developer mode, ensure we make the logo urls fully qualified
                //-- as the FE maybe being run from a different development server.
              logoUrl = ApiUtils.getWebContextRoot(configurationService.apiConfiguration(), false) + logoUrl;
            }
            adapters.add(new ProtocolAdapter(info.getProtocolId(),
                    info.getProtocolName(),
                    info.getName(),
                    info.getDescription(),
                    info.getUrl(),
                    info.getVersion(),
                    logoUrl,
                    info.getAuthor(),
                    true,
                    info == null ? null : info.getCategory().toString().toLowerCase(),
                    info.getTags() == null ? null : info.getTags().stream().
                            map(Enum::toString).collect(Collectors.toList()),
                    protocolAdapterManager.getSchemaManager(info).generateSchemaNode()));
        }
        return Response.status(200).entity(new ProtocolAdaptersList(adapters.build())).build();
    }

    @Override
    public @NotNull Response getAdapters() {

        final List<Adapter> adapters = protocolAdapterManager.getProtocolAdapters()
                .values()
                .stream()
                .map(this::convertToAdapter)
                .collect(Collectors.toUnmodifiableList());

        return Response.status(200).entity(new AdaptersList(adapters)).build();
    }


    @Override
    public @NotNull Response getAdaptersForType(@NotNull final String adapterType) {

        Optional<ProtocolAdapterInformation> protocolAdapterType =
                protocolAdapterManager.getAdapterTypeById(adapterType);
        if (protocolAdapterType.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final List<Adapter> adapters = protocolAdapterManager.getProtocolAdapters()
                .values()
                .stream()
                .filter(adapterInstance -> adapterInstance.getAdapterInformation().getProtocolId().equals(adapterType))
                .map(this::convertToAdapter)
                .collect(Collectors.toUnmodifiableList());

        return Response.status(200).entity(new AdaptersList(adapters)).build();
    }

    @Override
    public @NotNull Response getAdapter(final @NotNull String adapterId) {
        Optional<AdapterInstance> instance = protocolAdapterManager.getAdapterById(adapterId);
        if (!instance.isPresent()) {
            return ApiErrorUtils.notFound("Adapter not found");
        }
        return Response.status(200).entity(convertToAdapter(instance.get())).build();
    }


    private @NotNull Adapter convertToAdapter(final @NotNull AdapterInstance value) {
        AdapterRuntimeInformation runtimeInformation =
                new AdapterRuntimeInformation(value.getAdapter().getTimeOfLastStartAttempt() == null ?
                        null :
                        value.getAdapter().getTimeOfLastStartAttempt(),
                        value.getAdapter().getNumberOfDaemonProcessed(),
                        value.getAdapter().getLastErrorMessage(),
                        getConnectionStatusInternal(value.getAdapter().getId()));
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Map<String, Object> configObject;
        try {
            Thread.currentThread().setContextClassLoader(value.getAdapterFactory().getClass().getClassLoader());
            configObject = value.getAdapterFactory().unconvertConfigObject(objectMapper, value.getConfigObject());
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        return new Adapter(value.getAdapter().getId(),
                value.getAdapterInformation().getProtocolId(),
                configObject,
                runtimeInformation);
    }

    @Override
    public @NotNull Response discoverValues(
            @NotNull final String adapterId, final @Nullable String rootNode, final @Nullable Integer depth) {

        Optional<AdapterInstance> instance = protocolAdapterManager.getAdapterById(adapterId);
        if (!instance.isPresent()) {
            return ApiErrorUtils.notFound("Adapter not found");
        }

        final ProtocolAdapterDiscoveryOutputImpl output = new ProtocolAdapterDiscoveryOutputImpl();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            AdapterInstance adapterInstance = instance.get();
            Thread.currentThread()
                    .setContextClassLoader(adapterInstance.getAdapterFactory().getClass().getClassLoader());
            adapterInstance.getAdapter().discoverValues(new ProtocolAdapterDiscoveryInput() {
                @Override
                public @Nullable String getRootNode() {
                    return rootNode;
                }

                @Override
                public int getDepth() {
                    return (depth != null && depth > 0) ? depth : 1;
                }

            }, output);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        final NodeTreeImpl nodeTree = output.getNodeTree();
        final List<NodeTreeImpl.ObjectNode> children = nodeTree.getRootNode().getChildren();
        return Response.status(200).entity(new ValuesTree(children)).build();
    }

    @Override
    public Response addAdapter(final String adapterType, final Adapter adapter) {
        Optional<ProtocolAdapterInformation> protocolAdapterType =
                protocolAdapterManager.getAdapterTypeById(adapterType);
        if (protocolAdapterType.isEmpty()) {
            return ApiErrorUtils.notFound("Adapter Type not found by adapterType");
        }

        ApiErrorMessages errorMessages = ApiErrorUtils.createErrorContainer();
        Optional<AdapterInstance> instance = protocolAdapterManager.getAdapterById(adapter.getId());
        if (instance.isPresent()) {
            ApiErrorUtils.addValidationError(errorMessages, "id", "Adapter ID must be unique in system");
            return ApiErrorUtils.badRequest(errorMessages);
        }

        validateAdapterSchema(errorMessages, adapter);
        if (ApiErrorUtils.hasRequestErrors(errorMessages)) {
            return ApiErrorUtils.badRequest(errorMessages);
        }
        try {
            protocolAdapterManager.addAdapter(adapterType, adapter.getId(), adapter.getConfig(), true);
        } catch(IllegalArgumentException e){
            if(e.getCause() instanceof UnrecognizedPropertyException){
                ApiErrorUtils.addValidationError(errorMessages,
                        ((UnrecognizedPropertyException)e.getCause()).getPropertyName(), "Unknown field on adapter configuration");
            }

            return ApiErrorUtils.badRequest(errorMessages);
        }

        logger.info("Added protocol adapter of type {} with id {}", adapterType, adapter.getId());
        return Response.ok().build();
    }

    @Override
    public Response updateAdapter(final String adapterId, final Adapter adapter) {
        Optional<AdapterInstance> instance = protocolAdapterManager.getAdapterById(adapterId);
        if (!instance.isPresent()) {
            return ApiErrorUtils.notFound("Cannot update an adapter that does not exist");
        }
        protocolAdapterManager.updateAdapter(adapterId, adapter.getConfig());
        return Response.ok().build();
    }

    @Override
    public Response deleteAdapter(final String adapterId) {
        Optional<AdapterInstance> instance = protocolAdapterManager.getAdapterById(adapterId);
        if (!instance.isPresent()) {
            return ApiErrorUtils.notFound("Adapter not found");
        }
        logger.info("deleting adapter {}", adapterId);
        protocolAdapterManager.deleteAdapter(adapterId);
        return Response.ok().build();
    }

    @Override
    public Response changeConnectionStatus(final String adapterId, final ConnectionStatusTransitionCommand command) {
        ApiErrorMessages errorMessages = ApiErrorUtils.createErrorContainer();
        ApiErrorUtils.validateRequiredField(errorMessages, "id", adapterId, false);
        ApiErrorUtils.validateRequiredFieldRegex(errorMessages, "id", adapterId, HiveMQEdgeConstants.ID_REGEX);
        ApiErrorUtils.validateRequiredEntity(errorMessages, "command", command);
        if (!protocolAdapterManager.getAdapterById(adapterId).isPresent()) {
            return ApiErrorUtils.notFound(String.format("Adapter not found by id '%s'", adapterId));
        }
        if (ApiErrorUtils.hasRequestErrors(errorMessages)) {
            return ApiErrorUtils.badRequest(errorMessages);
        } else {
            switch (command.getCommand()) {
                case CONNECT:
                    break;
                case DISCONNECT:
                    break;
                case RESTART:
                    break;
            }
            return Response.status(200).build();
        }
    }

    @Override
    public Response getConnectionStatus(final @NotNull String adapterId) {

        ApiErrorMessages errorMessages = ApiErrorUtils.createErrorContainer();
        ApiErrorUtils.validateRequiredField(errorMessages, "id", adapterId, false);
        ApiErrorUtils.validateRequiredFieldRegex(errorMessages, "id", adapterId, HiveMQEdgeConstants.ID_REGEX);
        if (!protocolAdapterManager.getAdapterById(adapterId).isPresent()) {
            return ApiErrorUtils.notFound(String.format("Adapter not found by id '%s'", adapterId));
        }
        if (ApiErrorUtils.hasRequestErrors(errorMessages)) {
            return ApiErrorUtils.badRequest(errorMessages);
        } else {
            return Response.status(200).entity(getConnectionStatusInternal(adapterId)).build();
        }
    }

    protected ConnectionStatus getConnectionStatusInternal(final @NotNull String adapterId) {
        Optional<AdapterInstance> optional = protocolAdapterManager.getAdapterById(adapterId);
        boolean connected = false;
        if (optional.isPresent()) {
            connected = optional.get().getAdapter().status() ==
                    com.hivemq.edge.modules.api.adapters.ProtocolAdapter.Status.CONNECTED;
        }
        ConnectionStatus status = new ConnectionStatus(connected ?
                ConnectionStatus.STATUS.CONNECTED :
                ConnectionStatus.STATUS.DISCONNECTED, adapterId, ApiConstants.ADAPTER_TYPE);
        return status;
    }

    protected void validateAdapterSchema(
            final @NotNull ApiErrorMessages apiErrorMessages, final @NotNull Adapter adapter) {
        ProtocolAdapterInformation information =
                protocolAdapterManager.getAllAvailableAdapterTypes().get(adapter.getProtocolAdapterType());
        if (information == null) {
            ApiErrorUtils.addValidationError(apiErrorMessages,
                    "config",
                    "Unable to find adapter type by supplied adapterTypeId");
            return;
        }

        if (adapter.getConfig() == null) {
            ApiErrorUtils.addValidationError(apiErrorMessages, "config", "Config must be supplied on the adapter");
            return;
        }

        Set<ValidationMessage> errors =
                protocolAdapterManager.getSchemaManager(information).validateObject(adapter.getConfig());
        errors.stream()
                .forEach(e -> ApiErrorUtils.addValidationError(apiErrorMessages,
                        e.getPath(),
                        e.getMessage() + ", args=" + Arrays.toString(e.getArguments()) + ", type=" + e.getType()));
    }

    @Override
    public Response status() {
        //-- Bridges
        ImmutableList.Builder<ConnectionStatus> builder = new ImmutableList.Builder<>();
        Map<String, AdapterInstance> adapters = protocolAdapterManager.getProtocolAdapters();
        for (AdapterInstance instance : adapters.values()) {
            boolean connected = instance.getAdapter().status() ==
                    com.hivemq.edge.modules.api.adapters.ProtocolAdapter.Status.CONNECTED;
            ConnectionStatus status = new ConnectionStatus(connected ?
                    ConnectionStatus.STATUS.CONNECTED :
                    ConnectionStatus.STATUS.DISCONNECTED, instance.getAdapter().getId(), ApiConstants.ADAPTER_TYPE);
            builder.add(status);
        }
        return Response.status(200).entity(new ConnectionStatusList(builder.build())).build();
    }
}
