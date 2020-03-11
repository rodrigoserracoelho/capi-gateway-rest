package at.rodrigo.api.gateway.rest.validator;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.rest.repository.ApiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class ApiValidator {

    @Autowired
    private ApiRepository apiRepository;

    public boolean isApiValid(Api newApi) {
        if(newApi.getId() != null) {
            Optional<Api> existingApi = apiRepository.findById(newApi.getId());
            if(existingApi.isPresent()) {
                return validateExistingApi(newApi, existingApi.get());
            } else {
                return false;
            }
        } else {
            return validateNewApi(newApi);
        }
    }

    private boolean validateExistingApi(Api newApi, Api existingApi) {
        if(!newApi.getName().equalsIgnoreCase(existingApi.getName())) {
            return false;
        }
        if(!newApi.getContext().equalsIgnoreCase(existingApi.getContext())) {
            return false;
        }
        return validateFields(newApi);
    }

    private boolean validateNewApi(Api newApi) {
        Api existingWithCondition = apiRepository.findByName(newApi.getName());
        if(existingWithCondition != null) {
            return false;
        }
        existingWithCondition = apiRepository.findByContext(newApi.getContext());
        if(existingWithCondition != null) {
            return false;
        }
        newApi.setId(UUID.randomUUID().toString());
        return validateFields(newApi);
    }

    private boolean validateFields(Api newApi) {
        if(newApi.getEndpoints().isEmpty()) {
            return false;
        }
        if(newApi.isSwagger() && newApi.getSwaggerEndpoint() == null) {
            return false;
        }
        if(newApi.isBlockIfInError() && newApi.getMaxAllowedFailedCalls() < 1) {
            return false;
        }
        return true;
    }
}
