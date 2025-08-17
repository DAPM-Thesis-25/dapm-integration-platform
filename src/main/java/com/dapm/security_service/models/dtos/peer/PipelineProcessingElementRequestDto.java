// ** TODO: replace the pipelineId with something concrete, so that the OrgB can get enough knowledge of the pipeline without OrgA revealing the pipeline
package com.dapm.security_service.models.dtos.peer;

import com.dapm.security_service.models.RequesterInfo;
import lombok.Data;

import java.util.UUID;

@Data
public class PipelineProcessingElementRequestDto {
    private UUID requestId;
    private UUID pipelineId;
    private UUID pipelineNodeId;
    private RequesterInfo requester;
}
