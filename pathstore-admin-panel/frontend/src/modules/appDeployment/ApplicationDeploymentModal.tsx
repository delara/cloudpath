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

import React, {FunctionComponent, useCallback, useContext} from "react";
import Modal from "react-bootstrap/Modal";
import {Button} from "react-bootstrap";
import {
    ApplicationDeploymentModalContext,
    ApplicationDeploymentModalDataContext
} from "../../contexts/ApplicationDeploymentModalContext";
import {ApplicationDeploymentForm} from "./ApplicationDeploymentForm";
import {HypotheticalApplicationTopology} from "./HypotheticalApplicationTopology";
import {SubmissionErrorModalContext} from "../../contexts/SubmissionErrorModalContext";
import {APIContext} from "../../contexts/APIContext";
import {LoadingModalContext} from "../../contexts/LoadingModalContext";
import {ErrorModalContext} from "../../contexts/ErrorModalContext";
import {webHandler} from "../../utilities/Utils";
import {ConfirmationButton} from "../confirmation/ConfirmationButton";

/**
 * This component is used to visualize the transition of the network through the deployment and undeployment proccesses
 * of an application. It is also used as a 'workbench' to deploy and undeploy an application from a section of the network.
 * After the user is okay with their choice they can submit their changes to the api.
 *
 * @constructor
 */
export const ApplicationDeploymentModal: FunctionComponent = () => {

    // load force refresh
    const {forceRefresh} = useContext(APIContext);

    // loading modal
    const loadingModal = useContext(LoadingModalContext);

    // error modal
    const errorModal = useContext(ErrorModalContext);

    // submission error modal
    const submissionErrorModal = useContext(SubmissionErrorModalContext);

    // Load information from the data context to properly display the topology
    const {application, additions, updateAdditions, deletions, updateDeletions} = useContext(ApplicationDeploymentModalDataContext);

    // Callback function to reset the changes to the network
    const reset = useCallback(() => {
        if (updateAdditions && updateDeletions) {
            updateAdditions([]);
            updateDeletions([]);
        }
    }, [updateAdditions, updateDeletions]);

    /**
     * This function is used to submit the changes added by the user to the api.
     *
     * Either the additions or the deletions must have atleast one entry and it will be submitted.
     */
    const submitChanges = useCallback(() => {
        if (additions && deletions && loadingModal.show && loadingModal.close && errorModal.show && submissionErrorModal.show && forceRefresh) {

            if (additions.length <= 0 && deletions.length <= 0) {
                submissionErrorModal.show("You have not made any changes to the network");
                return;
            }

            loadingModal.show();
            if (additions.length > 0)
                fetch('/api/v1/application_management', {
                    method: 'POST',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        records: additions
                    })
                })
                    .then(webHandler)
                    .then(() => {
                        if (deletions?.length === 0) {
                            forceRefresh();
                            reset();
                        }
                    })
                    .catch(errorModal.show)
                    .finally(loadingModal.close);


            if (deletions.length > 0)
                fetch('/api/v1/application_management', {
                    method: 'DELETE',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        records: deletions
                    })
                })
                    .then(webHandler)
                    .then(() => {
                        forceRefresh();
                        reset();
                    })
                    .catch(errorModal.show)
                    .finally(loadingModal.close);
        }
    }, [additions, deletions, reset, loadingModal, errorModal, submissionErrorModal, forceRefresh]);

    // load the modal information from the modal context
    const {visible, close} = useContext(ApplicationDeploymentModalContext);

    /**
     * This callback is used to delete the given application from the network.
     */
    const deleteApplication = useCallback(() => {
        if (loadingModal.show && loadingModal.close && errorModal.show && forceRefresh && close && application) {
            loadingModal.show();
            fetch('/api/v1/applications', {
                method: 'DELETE',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    applicationName: application.keyspace_name
                })
            })
                .then(webHandler)
                .then(() => {
                    forceRefresh();
                    reset();
                    close();
                })
                .catch(errorModal.show)
                .finally(loadingModal.close)
        }
    }, [loadingModal, errorModal, forceRefresh, reset, close, application]);

    return (
        <Modal show={visible}
               size={"xl"}
               centered
        >
            <Modal.Header>
                <Modal.Title>Application Deployment for the keyspace {application?.keyspace_name}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <HypotheticalApplicationTopology>
                    <Button onClick={reset}>Reset Changes</Button>
                </HypotheticalApplicationTopology>
                <ApplicationDeploymentForm/>
            </Modal.Body>
            <Modal.Footer>
                <ConfirmationButton message={"Are you sure you want to submit your application deployment changes?"}
                                    onClick={submitChanges}>
                    Submit Changes
                </ConfirmationButton>
                <ConfirmationButton message={"Are you sure you want to delete this application from the network?"}
                                    onClick={deleteApplication}>
                    Delete Applications
                </ConfirmationButton>
                <Button onClick={close}>close</Button>
            </Modal.Footer>
        </Modal>
    );
};