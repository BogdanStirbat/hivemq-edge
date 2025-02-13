/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { JsonNode } from './JsonNode';

/**
 * List of result items that are returned by this endpoint
 */
export type ProtocolAdapter = {
    /**
     * The author of the adapter
     */
    author?: string;
    /**
     * The category of the adapter
     */
    category?: string;
    configSchema?: JsonNode;
    /**
     * The description
     */
    description?: string;
    /**
     * The id assigned to the protocol adapter type
     */
    id?: string;
    /**
     * Is the adapter installed?
     */
    installed?: boolean;
    /**
     * The logo of the adapter
     */
    logoUrl?: string;
    /**
     * The name of the adapter
     */
    name?: string;
    /**
     * The supported protocol
     */
    protocol?: string;
    /**
     * The search tags associated with this adapter
     */
    tags?: Array<string>;
    /**
     * The url of the adapter
     */
    url?: string;
    /**
     * The installed version of the adapter
     */
    version?: string;
};

