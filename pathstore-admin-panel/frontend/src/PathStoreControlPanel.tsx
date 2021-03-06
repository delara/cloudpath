/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React, {FunctionComponent, useContext, useEffect, useState} from "react";
import {useInterval} from "./hooks/useInterval";
import {APIContext} from "./contexts/APIContext";
import {ApplicationStatus, AvailableLogDates, Deployment} from "./utilities/ApiDeclarations";
import {ViewTopology} from "./modules/topology/ViewTopology";
import {Center} from "./utilities/AlignedDivs";
import {NodeInfoModalProvider} from "./contexts/NodeInfoModalContext";
import {NodeDeployment} from "./modules/nodeDeployment/NodeDeployment";
import {NodeDeploymentModalProvider} from "./contexts/NodeDeploymentModalContext";
import {ApplicationManagement} from "./modules/appInstallation/ApplicationManagement";
import {useLockedApiRequest} from "./hooks/useLockedApiRequest";

/**
 * This is the main component for the pathstore control panel website. It must have access to the {@link APIContext}
 * as this component will manage the setting of the data within that context on a timer.
 * @constructor
 */
export const PathStoreControlPanel: FunctionComponent = () => {
    // Grab needed values from the context
    const {setDeployment, setApplicationStatus, setAvailableLogDates, forceRefresh} = useContext(APIContext);

    // function to query deployment objects
    const queryDeployment = useLockedApiRequest<Deployment>('/api/v1/deployment', setDeployment);

    // function to query application status objects
    const queryApplicationStatus = useLockedApiRequest<ApplicationStatus>('/api/v1/application_management', setApplicationStatus);

    // function to query available log dates
    const queryAvailableLogDates = useLockedApiRequest<AvailableLogDates>('/api/v1/available_log_dates', setAvailableLogDates);

    // state to force the use effect to only be called on startup
    const [called, setCalled] = useState<boolean>(false);

    // Load all data on startup
    useEffect(() => {
        if (forceRefresh && !called) {
            forceRefresh();
            setCalled(true);
        }
    }, [forceRefresh, called]);

    // every 2 seconds reload the below endpoint
    useInterval(() => {
        queryDeployment();
        queryApplicationStatus();
        queryAvailableLogDates();
    }, 2000);

    return (
        <>
            <Center>
                <h1 style={{fontWeight: 700, textDecoration: 'underline'}}>PathStore Control Panel</h1>
            </Center>
            <NodeInfoModalProvider>
                <ViewTopology/>
            </NodeInfoModalProvider>
            <NodeDeploymentModalProvider>
                <NodeDeployment/>
            </NodeDeploymentModalProvider>
            <hr/>
            <ApplicationManagement/>
        </>
    );
};