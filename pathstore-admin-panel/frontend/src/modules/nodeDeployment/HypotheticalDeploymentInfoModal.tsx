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

import React, {
    FunctionComponent,
    useCallback,
    useContext,
    useEffect,
    useState
} from "react";
import {Button} from "react-bootstrap";
import Modal from "react-bootstrap/Modal";
import {HypotheticalDeploymentInfoModalContext} from "../../contexts/HypotheticalDeploymentInfoModalContext";
import {NodeDeploymentModalDataContext} from "../../contexts/NodeDeploymentModalContext";
import {getDeploymentObjects} from "./NodeDeploymentModal";
import {ServerInfo} from "../modalShared/ServerInfo";
import {SubmissionErrorModalContext} from "../../contexts/SubmissionErrorModalContext";
import {createMap, identity} from "../../utilities/Utils";
import {Deployment, DEPLOYMENT_STATE, DeploymentUpdate} from "../../utilities/ApiDeclarations";


/**
 * This component is used to render a modal on click of a node in the node deployment modal.
 * This component also handle all deletion logic for the hypothetical network
 * @param props
 * @constructor
 */
export const HypotheticalDeploymentInfoModal: FunctionComponent = (props) => {
    // submission error modal context
    const submissionErrorModal = useContext(SubmissionErrorModalContext);

    // load visible, data and close from the associated context
    const {visible, data, close} = useContext(HypotheticalDeploymentInfoModalContext);

    // reference all needed data from the node deployment modal data contex
    const {deployment, servers, additions, deletions, updateAdditions, updateDeletions} = useContext(NodeDeploymentModalDataContext);

    /** This state and effect are used to watch for updated deployment and additions objects to create the
     * same 'stitched' deployment object set.
     *
     * @see getDeploymentObjects
     */
    const [stitchedDeployment, updateStitchedDeployment] = useState<Deployment[]>([]);

    // update stitched deployment whenever deployment or additions change
    useEffect(() => updateStitchedDeployment(getDeploymentObjects(deployment, additions)), [deployment, additions]);

    /**
     * This function is used as a callback function for the hypothetical info modal. It will delete a subtree of the
     * topology based on which node was clicked to delete. It will also close the modal and
     *
     * @param event
     */
    const handleHypotheticalDelete = useCallback((event: any): void => {
        event.preventDefault();
        try {
            if (stitchedDeployment && additions && deletions && data && updateAdditions && updateDeletions && close) {
                const response = deleteSubTree(stitchedDeployment, additions, deletions, data.node);
                updateAdditions(response.additions);
                updateDeletions(response.deletions);
                close();
            }
        } catch (e) {
            if (submissionErrorModal.show)
                submissionErrorModal.show(e.message);
        }
    }, [additions, deletions, data, updateAdditions, updateDeletions, close, stitchedDeployment, submissionErrorModal]);

    /**
     * This function is used to determine if the delete button should be rendered.
     *
     * If will be rendered iff the node is deployed or hypothetical
     */
    const deleteButton = useCallback(() => {
        const map: Map<number, Deployment> = createMap<number, Deployment>(v => v.new_node_id, identity, stitchedDeployment);
        // Check ensures waiting nodes can't be deleted
        if (data)
            if ((data.node !== 1 && map.get(data.node)?.process_status === "DEPLOYED") || data.isHypothetical)
                return <Button onClick={handleHypotheticalDelete}>Delete</Button>;
        return undefined;
    }, [data, handleHypotheticalDelete, stitchedDeployment]);

    return (
        <Modal show={visible}
               size={"lg"}
               centered
        >
            <Modal.Header>
                <Modal.Title>Info Modal for node {data?.node}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <ServerInfo deployment={stitchedDeployment} servers={servers} node={data?.node}/>
            </Modal.Body>
            <Modal.Footer>
                {deleteButton()}
                <Button onClick={close}>Close</Button>
            </Modal.Footer>
        </Modal>
    );
};

/**
 * This function is used to handle the deletion of a sub tree from the network based on what node the info modal
 * was loaded for. This function handles all cases whether the sub tree is purley comprised of hypothetical additions,
 * all deployed nodes, or a mixture of both. To see the logic of how each value is modified see {@link handleDeletion}
 *
 * @param deployment deployment objects from the internal state
 * @param additions addition objects from the internal state
 * @param deletions deletion objects from the internal state
 * @param node_number what node was click on for the info modal
 */
const deleteSubTree = (deployment: Deployment[], additions: DeploymentUpdate[], deletions: DeploymentUpdate[], node_number: number):
    { additions: DeploymentUpdate[], deletions: DeploymentUpdate[] } => {

    const deploymentMap: Map<number, Deployment> = createMap<number, Deployment>(v => v.new_node_id, identity, deployment);
    const additionMap: Map<number, DeploymentUpdate> = createMap<number, DeploymentUpdate>(v => v.newNodeId, identity, additions);
    const deletionsMap: Map<number, DeploymentUpdate> = createMap<number, DeploymentUpdate>(v => v.newNodeId, identity, deletions);

    const nodeToListOfChildren: Map<number, Deployment[]> = new Map<number, Deployment[]>();

    deployment.forEach(v => {
        if (!nodeToListOfChildren.has(v.parent_node_id)) nodeToListOfChildren.set(v.parent_node_id, [v]);
        else nodeToListOfChildren.get(v.parent_node_id)?.push(v);
    });

    if (nodeToListOfChildren.has(node_number))
        deleteSubTreeHelper(deploymentMap, nodeToListOfChildren, additionMap, deletionsMap, node_number);

    handleDeletion(deploymentMap, additionMap, deletionsMap, node_number);

    const newUpdates = Array.from(additionMap.values());
    const newDeletions = Array.from(deletionsMap.values());

    return (
        {
            additions: newUpdates,
            deletions: newDeletions,
        }
    );

};

/**
 * This function will perform operations from a back to front order (furthest away from the root to the root node).
 * The operations performed are based on what type the node is see {@link handleDeletion}
 *
 * @param deploymentMap newNodeId to deployment object
 * @param nodeToListOfChildren newNodeId to list of children
 * @param additionMap newNodeId to update object
 * @param deletionsMap newNodeId to deletions object
 * @param node node currently inspecting
 */
const deleteSubTreeHelper = (deploymentMap: Map<number, Deployment>, nodeToListOfChildren: Map<number, Deployment[]>, additionMap: Map<number, DeploymentUpdate>, deletionsMap: Map<number, DeploymentUpdate>, node: number): void => {
    if (nodeToListOfChildren.has(node)) {

        const children: Deployment[] | undefined = nodeToListOfChildren.get(node);

        // Only here because Map.get may return undefined even though
        if (children)
            children.forEach(c => {
                deleteSubTreeHelper(deploymentMap, nodeToListOfChildren, additionMap, deletionsMap, c.new_node_id);

                handleDeletion(deploymentMap, additionMap, deletionsMap, c.new_node_id);
            });
    }
};

/**
 * If hypo then remove the updates record and remove the deployment record
 *
 * Else add deletion record
 *
 * @param deploymentMap map of newNodeId to deployment object
 * @param updatesMap map of newNodeId to update object
 * @param deletionsMap map of newNodeId to deletion object
 * @param node node to handle
 */
const handleDeletion = (deploymentMap: Map<number, Deployment>, updatesMap: Map<number, DeploymentUpdate>, deletionsMap: Map<number, DeploymentUpdate>, node: number): void => {
    if (updatesMap.has(node)) {
        updatesMap.delete(node);
    } else {

        switch (deploymentMap.get(node)?.process_status) {
            case DEPLOYMENT_STATE[DEPLOYMENT_STATE.DEPLOYED]:
                break;
            case DEPLOYMENT_STATE[DEPLOYMENT_STATE.PROCESSING_DEPLOYING]:
            case DEPLOYMENT_STATE[DEPLOYMENT_STATE.DEPLOYING]:
            case DEPLOYMENT_STATE[DEPLOYMENT_STATE.WAITING_DEPLOYMENT]:
                throw new Error("Cannot perform a delete operation on a sub-tree with a currently deploying node");
            default:
                return;
        }

        const value = conversion(deploymentMap.get(node));

        if (value)
            deletionsMap.set(node, value);
    }
};

/**
 * Strips a deployment object of certain information to produce an update object
 *
 * @param deployment deployment object to produce an update object
 */
const conversion = (deployment: Deployment | undefined): DeploymentUpdate | undefined => {
    return deployment ? {
        newNodeId: deployment.new_node_id,
        parentId: deployment.parent_node_id,
        serverUUID: deployment.server_uuid
    } : undefined;
};