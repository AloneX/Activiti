/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.activiti.services;

import org.activiti.client.model.ExtendedProcessInstance;
import org.activiti.client.model.ProcessInstance;
import org.activiti.client.model.resources.ProcessInstanceResource;
import org.activiti.client.model.resources.assembler.ProcessInstanceResourceAssembler;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.model.converter.ProcessInstanceConverter;
import org.activiti.services.audit.producer.app.MessageProducerActivitiEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**

 */
@RestController
@RequestMapping(value = "/api/runtime/process-instances", produces = "application/hal+json")
public class ProcessInstanceController {

    private final ProcessInstanceConverter processInstanceConverter;

    private final RuntimeService runtimeService;

    private final ProcessInstanceResourceAssembler resourceAssembler;

    private PageableProcessInstanceService pageableProcessInstanceService;

    private MessageProducerActivitiEventListener listener;

    @Autowired
    public ProcessInstanceController(ProcessInstanceConverter processInstanceConverter,
                                     RuntimeService runtimeService,
                                     ProcessInstanceResourceAssembler resourceAssembler,
                                     PageableProcessInstanceService pageableProcessInstanceService,
                                     MessageProducerActivitiEventListener listener) {
        this.processInstanceConverter = processInstanceConverter;
        this.runtimeService = runtimeService;
        this.resourceAssembler = resourceAssembler;
        this.pageableProcessInstanceService = pageableProcessInstanceService;
        this.listener = listener;
        this.runtimeService.addEventListener(listener);
    }

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<ProcessInstanceResource> getProcessInstances(Pageable pageable, PagedResourcesAssembler<ProcessInstance> pagedResourcesAssembler){
        Page<ProcessInstance> page = pageableProcessInstanceService.getProcessInstances(pageable);
        return pagedResourcesAssembler.toResource(page, resourceAssembler);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Resource<ProcessInstance> startProcess(@RequestBody ExtendedProcessInstance processInstance) {
        ProcessInstanceBuilder builder = runtimeService.createProcessInstanceBuilder();
        builder.processDefinitionKey(processInstance.getProcessDefinitionKey());
        builder.variables(processInstance.getVariables());

        return resourceAssembler.toResource(processInstanceConverter.from(builder.start()));
    }

    @RequestMapping(value = "/{processInstanceId}", method = RequestMethod.GET)
    public Resource<ProcessInstance> getProcessInstance(@PathVariable String processInstanceId){
        org.activiti.engine.runtime.ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        return resourceAssembler.toResource(processInstanceConverter.from(processInstance));
    }

}
