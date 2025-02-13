/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { BridgeRuntimeInformation } from './BridgeRuntimeInformation';
import type { BridgeSubscription } from './BridgeSubscription';
import type { TlsConfiguration } from './TlsConfiguration';

export type Bridge = {
    bridgeRuntimeInformation?: BridgeRuntimeInformation;
    /**
     * The cleanStart value associated the the MQTT connection.
     */
    cleanStart: boolean;
    /**
     * The client identifier associated the the MQTT connection.
     */
    clientId?: string | null;
    /**
     * The host the bridge connects to - a well formed hostname, ipv4 or ipv6 value.
     */
    host: string;
    /**
     * The bridge id, must be unique and only contain alpha numeric characters with spaces and hyphens.
     */
    id: string;
    /**
     * The keepAlive associated the the MQTT connection.
     */
    keepAlive: number;
    /**
     * localSubscriptions associated with the bridge
     */
    localSubscriptions?: Array<BridgeSubscription>;
    /**
     * Is loop prevention enabled on the connection
     */
    loopPreventionEnabled?: boolean;
    /**
     * Loop prevention hop count
     */
    loopPreventionHopCount?: number;
    /**
     * The password value associated the the MQTT connection.
     */
    password?: string | null;
    /**
     * The port number to connect to
     */
    port: number;
    /**
     * remoteSubscriptions associated with the bridge
     */
    remoteSubscriptions?: Array<BridgeSubscription>;
    /**
     * The sessionExpiry associated the the MQTT connection.
     */
    sessionExpiry: number;
    tlsConfiguration?: TlsConfiguration;
    /**
     * The username value associated the the MQTT connection.
     */
    username?: string | null;
};

