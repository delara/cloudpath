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

import React, {FunctionComponent, ReactElement, useCallback, useContext, useState} from "react";
import {AvailableLogDates, Log} from "../../utilities/ApiDeclarations";
import {Button, Form} from "react-bootstrap";
import {NodeInfoModalContext} from "../../contexts/NodeInfoModalContext";
import {LoadingModalContext} from "../../contexts/LoadingModalContext";
import {ErrorModalContext} from "../../contexts/ErrorModalContext";
import {webHandler} from "../../utilities/Utils";
import {APIContext} from "../../contexts/APIContext";

/**
 * This component is used to display logs to the user based on their request
 * @constructor
 */
export const LogViewer: FunctionComponent = () => {

    // load available log dates from api context
    const {availableLogDates} = useContext(APIContext);

    // Load the node id from the node info modal context
    const {data} = useContext(NodeInfoModalContext);

    // Get loadingModal
    const loadingModal = useContext(LoadingModalContext);

    // Get errorModal
    const errorModal = useContext(ErrorModalContext);

    // Internal state for the log (set when the user submits the form)
    const [log, setLog] = useState<Log | null>(null);

    // Handle the request of the form
    const onFormSubmit = useCallback((event: any): void => {
        if (loadingModal && loadingModal.show && loadingModal.close) {

            event.preventDefault();

            const date = event.target.elements.date.value.trim();

            const log_level = event.target.elements.log_level.value.trim();

            const url = '/api/v1/logs?node_id=' + data + "&date=" + date + "&log_level=" + log_level;

            // show loading
            loadingModal.show();
            fetch(url, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            })
                .then(webHandler)
                .then(setLog)
                .catch(errorModal.show)
                .finally(loadingModal.close);
        }
    }, [data, loadingModal, errorModal]);

    return (
        <>
            {formatForm(availableLogDates, data, onFormSubmit)}
            <br/>
            {formatLog(log)}
        </>
    )
};

/**
 * If the user has submitted a request for log info then this will make the response displayable.
 *
 * There is a chance that a certain log level is empty for their request and this will inform the user if that occurs
 */
const formatForm = (availableLogDates: AvailableLogDates[] | undefined, node: number | undefined, formSubmit: (event: any) => void): ReactElement | null => {
    if (!availableLogDates || !node) return null;
    else {

        const nodeSpecificAvailableDates = availableLogDates.filter(i => i.node_id === node)[0];

        if (!nodeSpecificAvailableDates) return null;

        const dates = [];

        for (let [index, date] of nodeSpecificAvailableDates.date.entries())
            dates.push(
                <option key={index}>{date}</option>
            );

        return (
            <div>
                <hr/>
                <h2>Log Selector</h2>
                <Form onSubmit={formSubmit}>
                    <Form.Group controlId="date">
                        <Form.Label>Select Date</Form.Label>
                        <Form.Control as="select">
                            {dates}
                        </Form.Control>
                    </Form.Group>
                    <Form.Group controlId="log_level">
                        <Form.Label>Select Log Level</Form.Label>
                        <Form.Control as="select">
                            <option>ERROR</option>
                            <option>INFO</option>
                            <option>DEBUG</option>
                            <option>FINEST</option>
                        </Form.Control>
                    </Form.Group>
                    <Button variant="primary" type="submit">
                        Submit
                    </Button>
                </Form>
            </div>
        );
    }

};

/**
 * If the user has submitted a request for log info then this will make the response displayable.
 *
 * There is a chance that a certain log level is empty for their request and this will inform the user if that occurs
 */
const formatLog = (log: Log | null): ReactElement | null => {
    if (!log) return null;
    else if (log?.logs.length === 0) {
        return (
            <div>
                <h2>Log Viewer</h2>
                <p>The log you've selected is empty</p>
            </div>
        );
    } else {
        return (
            <div>
                <h2>Log Viewer</h2>
                <div className={"logViewer"}>
                    <code id={"log"}>{log?.logs.map(i => i + "\n")}</code>
                </div>
            </div>
        );
    }
};