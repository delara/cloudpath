import React, {Component} from 'react';
import {Application, ApplicationStatus, Deployment, Log, Server} from "./utilities/ApiDeclarations";
import ViewTopology from "./modules/topology/ViewTopology";
import NodeDeployment from "./modules/nodeDeployment/NodeDeployment";
import LiveTransitionVisual from "./modules/topology/LiveTransitionVisual";
import {DisplayAvailableApplications} from "./modules/installation/DisplayAvailableApplications";
import ApplicationCreation from "./modules/installation/ApplicationCreation";
import DeployApplication from "./modules/deployment/DeployApplication";

/**
 * State definition for {@link PathStoreControlPanel}
 */
interface PathStoreControlPanelState {
    /**
     * List of deployment objects from api
     */
    readonly deployment: Deployment[]

    /**
     * List of server objects from api
     */
    readonly servers: Server[]

    /**
     * List of application objects from api
     */
    readonly applications: Application[]

    /**
     * List of node application status from api
     */
    readonly applicationStatus: ApplicationStatus[]

    /**
     * List of logs for all node from api
     */
    readonly logs: Log[]
}

/**
 * This is the main component for the PathStore Control Panel it is the parent of multiple sub components and has the
 * main function of querying all main data for the website on a timer which refreshes ever 2 seconds. This can however
 * be over written by the force refresh function that can be passed to child components to call once they've committed
 * a state changing operation.
 *
 * The main component structure looks like:
 *
 * ViewTopology
 * NodeDeployment
 *  - NodeDeploymentModal
 *      - NodeDeploymentAdditionForm
 *      - AddServers
 *          - ServerCreationResponseModal
 *      - DisplayServers
 * LiveTransitionVisual
 *  - LiveTransitionVisualModal
 * DisplayAvailableApplications
 * ApplicationCreation
 *  - ApplicationCreationResponseModal
 * DeployApplication
 *  - DeployApplicationResponseModal
 *
 * The Common components used throughout the application are:
 * PathStoreTopology
 * ErrorResponseModal
 * LoadingModal
 * NodeInfoModal
 *
 * And all global function are in
 * Utils.tsx
 *
 * And all api responses and formats are in
 * ApiDeclarations.ts
 *
 * For more information about this code base and the project see the pathstore github page
 */
export default class PathStoreControlPanel extends Component<{}, PathStoreControlPanelState> {

    /**
     * Timer for auto refresh of data
     */
    private timer: any;

    /**
     * Denotes whether or not a specific request is still loading (Fixes issue with request stacking)
     */
    private loading: boolean[] = [false, false, false, false, false];

    /**
     * Initialize state and empty props
     *
     * @param props
     */
    constructor(props = {}) {
        super(props);

        this.state = {
            deployment: [],
            servers: [],
            applications: [],
            applicationStatus: [],
            logs: []
        }
    }

    /**
     * On mount of the component query all needed data to fill the state and set a timer to refresh this data every 2
     * seconds
     */
    componentDidMount(): void {
        this.queryAll();

        this.timer = setInterval(this.queryAll, 2000);
    }

    /**
     * Garbage collect interval thread
     */
    componentWillUnmount(): void {
        clearInterval(this.timer);
    }

    /**
     * Call fetch all to get all the responses and load them into the state
     */
    queryAll = (): void => {

        if (!this.loading[0])
            this.genericLoadFunction<Deployment>('/api/v1/deployment', 0)
                .then((response: Deployment[]) => this.setState({deployment: response}, () => this.loading[0] = false));

        if (!this.loading[1])
            this.genericLoadFunction<Server>('/api/v1/servers', 1)
                .then((response: Server[]) => this.setState({servers: response}, () => this.loading[1] = false));

        if (!this.loading[2])
            this.genericLoadFunction<Application>('/api/v1/applications', 2)
                .then((response: Application[]) => this.setState({applications: response}, () => this.loading[2] = false));

        if (!this.loading[3])
            this.genericLoadFunction<ApplicationStatus>('/api/v1/application_management', 3)
                .then((response: ApplicationStatus[]) => this.setState({applicationStatus: response}, () => this.loading[3] = false));

        if (!this.loading[4])
            this.genericLoadFunction<Log>('/api/v1/logs', 4)
                .then((response: Log[]) => this.setState({logs: response}, () => this.loading[4] = false));
    };

    /**
     * This function is used to return an array of parsed object data from the api.
     *
     * @param url url to query
     * @param index index of loading to set
     */
    genericLoadFunction = <T extends unknown>(url: string, index: number): Promise<T[]> => {
        this.loading[index] = true;
        return fetch(url)
            .then(response => response.json() as Promise<T[]>);
    };

    /**
     * Used by child component to force refresh this data. This is only given to child components who are capable
     * of making state changing operations (i.e effecting our state data within this class)
     */
    forceRefresh = (): void => {
        this.componentWillUnmount();
        this.componentDidMount();
    };

    /**
     * Render all child components with the following ordering policy
     *
     * Network structure related components
     *
     * Application related components
     *
     * @returns {*}
     */
    render = () => (
        <div>
            <h1>PathStore Control Panel</h1>
            <div>
                <div>
                    <h2>Network Topology</h2>
                    <ViewTopology deployment={this.state.deployment}
                                  servers={this.state.servers}
                                  applicationStatus={this.state.applicationStatus}
                                  logs={this.state.logs}
                                  forceRefresh={this.forceRefresh}/>
                </div>
                <br/>
                <div>
                    <h2>Network Expansion</h2>
                    <NodeDeployment deployment={this.state.deployment}
                                    servers={this.state.servers}
                                    forceRefresh={this.forceRefresh}/>
                </div>
                <div>
                    <div>
                        <h2>Live Installation Viewer</h2>
                        <LiveTransitionVisual applications={this.state.applications}
                                              applicationStatus={this.state.applicationStatus}
                                              deployment={this.state.deployment}/>
                    </div>
                    <br/>
                    <div>
                        <h2>Application Creation</h2>
                        <DisplayAvailableApplications applications={this.state.applications}/>
                        <br/>
                        <ApplicationCreation forceRefresh={this.forceRefresh}/>
                    </div>
                    <br/>
                    <div>
                        <h2>Application Deployment</h2>
                        <DeployApplication deployment={this.state.deployment}
                                           applications={this.state.applications}
                                           applicationStatus={this.state.applicationStatus}
                                           servers={this.state.servers}/>
                    </div>
                </div>
            </div>
        </div>
    );
}
