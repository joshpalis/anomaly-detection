/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.ad.transport;

import java.util.Collections;
import java.util.List;

import org.opensearch.Version;
import org.opensearch.action.ActionListener;
import org.opensearch.action.FailedNodeException;
import org.opensearch.action.support.ActionFilters;
import org.opensearch.action.support.TransportAction;
import org.opensearch.ad.model.ADTaskProfile;
import org.opensearch.ad.task.ADTaskManager;
import org.opensearch.sdk.SDKClusterService;
import org.opensearch.tasks.Task;
import org.opensearch.tasks.TaskManager;
import org.opensearch.threadpool.ThreadPool;

import com.google.inject.Inject;

public class ADTaskProfileTransportAction extends TransportAction<ADTaskProfileRequest, ADTaskProfileResponse> {

    private ADTaskManager adTaskManager;
    private SDKClusterService clusterService;
    // private HashRing hashRing;

    @Inject
    public ADTaskProfileTransportAction(
        ThreadPool threadPool,
        SDKClusterService clusterService,
        ActionFilters actionFilters,
        TaskManager taskManager,
        ADTaskManager adTaskManager
        // HashRing hashRing
    ) {
        super(ADTaskProfileAction.NAME, actionFilters, taskManager);
        this.adTaskManager = adTaskManager;
        this.clusterService = clusterService;
        // this.hashRing = hashRing;
    }

    protected ADTaskProfileResponse newResponse(
        ADTaskProfileRequest request,
        List<ADTaskProfileNodeResponse> responses,
        List<FailedNodeException> failures
    ) {
        return new ADTaskProfileResponse(clusterService.state().getClusterName(), responses, failures);
    }

    @Override
    protected void doExecute(Task task, ADTaskProfileRequest request, ActionListener<ADTaskProfileResponse> actionListener) {
        /* @anomaly.detection Commented until we have extension support for hashring : https://github.com/opensearch-project/opensearch-sdk-java/issues/200 
        String remoteNodeId = request.getParentTask().getNodeId();
        Version remoteAdVersion = hashRing.getAdVersion(remoteNodeId);
        */
        Version remoteAdVersion = Version.CURRENT;
        ADTaskProfile adTaskProfile = adTaskManager.getLocalADTaskProfilesByDetectorId(request.getDetectorId());
        ADTaskProfileNodeResponse adTaskProfileNodeResponse = new ADTaskProfileNodeResponse(
            clusterService.localNode(),
            adTaskProfile,
            remoteAdVersion
        );
        actionListener.onResponse(newResponse(request, List.of(adTaskProfileNodeResponse), Collections.emptyList()));
    }
}
