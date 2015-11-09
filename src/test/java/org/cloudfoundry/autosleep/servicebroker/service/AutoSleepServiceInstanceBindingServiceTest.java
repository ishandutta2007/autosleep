package org.cloudfoundry.autosleep.servicebroker.service;

import lombok.extern.slf4j.Slf4j;
import org.cloudfoundry.autosleep.config.RepositoryConfig;
import org.cloudfoundry.autosleep.repositories.BindingRepository;
import org.cloudfoundry.autosleep.repositories.ServiceRepository;
import org.cloudfoundry.autosleep.repositories.ram.RamServiceRepository;
import org.cloudfoundry.autosleep.scheduling.GlobalWatcher;
import org.cloudfoundry.autosleep.servicebroker.model.AutoSleepServiceInstance;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.service.CatalogService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
@ContextConfiguration(classes = {AutosleepCatalogService.class,
        RepositoryConfig.class})
public class AutoSleepServiceInstanceBindingServiceTest {

    private static final String APPUID = "DB1F7D54-7A6A-4F7C-A06E-43EF9B9E3144";


    private AutoSleepServiceInstanceBindingService bindingService;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private BindingRepository bindingRepo;


    private CreateServiceInstanceBindingRequest createRequestTemplate;
    private GlobalWatcher mockWatcher;
    private String planId;
    private String serviceDefinitionId;

    /**
     * Init request templates with calaog definition, prepare mocks.
     */
    @Before
    public void init() {

        //mocking serviceRepo, we will just test bindingRepo in this class.
        ServiceRepository serviceRepo = mock(RamServiceRepository.class);
        when(serviceRepo.findOne(any(String.class))).thenReturn(mock(AutoSleepServiceInstance.class));

        mockWatcher = mock(GlobalWatcher.class);

        bindingService = new AutoSleepServiceInstanceBindingService(bindingRepo,mockWatcher);

        ServiceDefinition serviceDefinition = catalogService.getCatalog().getServiceDefinitions().get(0);
        planId = serviceDefinition.getPlans().get(0).getId();
        serviceDefinitionId = serviceDefinition.getId();
        createRequestTemplate = new CreateServiceInstanceBindingRequest(serviceDefinitionId,
                planId,
                APPUID);
    }

    @Test
    public void testCreateServiceInstanceBinding() throws Exception {
        bindingService.createServiceInstanceBinding(createRequestTemplate.withServiceInstanceId("Sid").withBindingId(
                "Bid"));
        verify(mockWatcher,times(1)).watchApp(any());
    }

    @Test
    public void testDeleteServiceInstanceBinding() throws Exception {
        String bindingId = "delBindingId";
        String serviceId = "delServiceId";
        bindingService.createServiceInstanceBinding(createRequestTemplate.withServiceInstanceId(serviceId)
                .withBindingId(bindingId));
        DeleteServiceInstanceBindingRequest deleteRequest = new DeleteServiceInstanceBindingRequest(bindingId, null,
                serviceDefinitionId, planId);
        bindingService.deleteServiceInstanceBinding(deleteRequest);
        verify(mockWatcher,times(1)).cancelWatch(any());
    }

}