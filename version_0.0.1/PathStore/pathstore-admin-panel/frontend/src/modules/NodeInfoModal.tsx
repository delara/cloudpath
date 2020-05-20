import {ApplicationStatus, AvailableLogDates, Deployment, Server, Update, Error} from "../utilities/ApiDeclarations";
import Modal from "react-modal";
import Button from "react-bootstrap/Button";
import React, {Component} from "react";
import {webHandler} from "../utilities/Utils";
import LogViewer from "./LogViewer";
import {ServerInfo} from "./nodeDeployment/servers/ServerInfo";
import {ApplicationStatusViewer} from "./ApplicationStatusViewer";
import {ErrorResponseModal} from "./ErrorResponseModal";

/**
 * Properties definition for {@link NodeInfoModal}
 */
interface NodeInfoModalProperties {
    /**
     * Node id of node to show info of
     */
    readonly node: number

    /**
     * Whether to display the modal or not
     */
    readonly show: boolean

    /**
     * List of deployment objects from api
     */
    readonly deployment: Deployment[]

    /**
     * List of node application status from api
     */
    readonly applicationStatus: ApplicationStatus[]

    /**
     * List of server objects from api
     */
    readonly servers: Server[]

    /**
     * List of available dates for each log
     */
    readonly availableLogDates?: AvailableLogDates[]

    /**
     * Force refresh props on other components
     */
    readonly forceRefresh?: () => void

    /**
     * Callback function to close modal on completion
     */
    readonly callback: () => void
}

/**
 * State definition for {@link NodeInfoModal}
 */
interface NodeInfoModalState {
    /**
     * Whether to show the error modal or not
     */
    readonly errorModalShow: boolean

    /**
     * What to give the error modal
     */
    readonly errorModalData: Error[]
}

/**
 * This component is used when a user clicks on a node in a pathstore topology to give the user some information
 * about the node.
 */
export default class NodeInfoModal extends Component<NodeInfoModalProperties, NodeInfoModalState> {

    /**
     * Initialize state and props
     *
     * @param props
     */
    constructor(props: NodeInfoModalProperties) {
        super(props);

        this.state = {
            errorModalShow: false,
            errorModalData: []
        }
    }

    /**
     * Returns a button or null iff the node is eligible for re-trying deployment (the node has failed deployment)
     *
     * @param deployment
     * @returns {null|*}
     */
    retryButton = (deployment: Deployment[]): {} | null => {
        const deployObject = deployment.filter(i => i.new_node_id === this.props.node);

        if (deployObject[0].process_status === "FAILED")
            return <Button onClick={this.retryOnClick}>Retry</Button>;
        else return null;
    };

    /**
     * Get data for retry
     *
     * @param deployment
     * @returns {{newNodeId: number, serverUUID, parentId: number}}
     */
    retryData = (deployment: Deployment[]): Update => {
        const deployObject = deployment.filter(i => i.new_node_id === this.props.node);

        return {
            parentId: deployObject[0].parent_node_id,
            newNodeId: deployObject[0].new_node_id,
            serverUUID: deployObject[0].server_uuid
        }
    };

    /**
     * PUT request to api with retryData as the body to inform the root node
     * that this node should be re-tried for deployment
     */
    retryOnClick = (): void => {
        fetch('/api/v1/deployment', {
            method: 'PUT',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({record: this.retryData(this.props.deployment)})
        })
            .then(webHandler)
            .then(() => {
                // Optional query iff the prop exists
                this.props.forceRefresh?.();
                this.props.callback();
            })
            .catch((response: Error[]) => this.setState({errorModalShow: true, errorModalData: response}));

    };

    /**
     * Callback used to close the error modal
     */
    closeModal = () => this.setState({errorModalShow: false, errorModalData: []});

    /**
     * Render server information and application status table and optionally the retry button
     *
     * @returns {*}
     */
    render() {

        const errorModal =
            this.state.errorModalShow ?
                <ErrorResponseModal show={this.state.errorModalShow}
                                    data={this.state.errorModalData}
                                    callback={this.closeModal}/>
                : null;

        return (
            <Modal isOpen={this.props.show}
                   style={{overlay: {zIndex: 1}}}
                   ariaHideApp={false}>
                {errorModal}
                <ServerInfo deployment={this.props.deployment} servers={this.props.servers} node={this.props.node}/>
                {this.retryButton(this.props.deployment)}
                <br/>
                <ApplicationStatusViewer
                    applicationStatus={this.props.applicationStatus.filter(i => i.nodeid === this.props.node)}/>
                <br/>
                <LogViewer node={this.props.node} availableLogDates={this.props.availableLogDates}/>
                <br/>
                <Button onClick={this.props.callback}>close</Button>
            </Modal>
        );
    }
};