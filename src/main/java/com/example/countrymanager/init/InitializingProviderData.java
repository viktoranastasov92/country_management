package com.example.countrymanager.init;

import com.example.countrymanager.model.Provider;
import com.example.countrymanager.repository.ProviderRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class InitializingProviderData implements InitializingBean  {

    private static final String PROVIDERS_SEPARATOR = ";";
    private static final String PROVIDER_FIELDS_SEPARATOR = ",";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";

    @Autowired
    private Environment environment;

    @Autowired
    private ProviderRepository providerRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        String prop = environment.getProperty("providers");
        if (StringUtils.hasLength(prop)) {
            String[] envProviders = prop.split(PROVIDERS_SEPARATOR);
            List<Provider> providers = new ArrayList<>();
            for (String provider: envProviders) {
                String[] fields = provider.split(PROVIDER_FIELDS_SEPARATOR);
                int id = 0;
                String name = "";
                for (String field: fields) {
                    if (field.indexOf(KEY_ID) >= 0) {
                        id = Integer.parseInt(field.substring(field.indexOf(KEY_ID) + KEY_ID.length() + 1, field.length()));
                    } else if (field.indexOf(KEY_NAME) >= 0) {
                        name = field.substring(field.indexOf(KEY_NAME) + KEY_NAME.length() + 1, field.length());
                    }
                }
                providers.add(new Provider(id, name));
            }
            providerRepository.saveAll(providers);
        }
    }

}
