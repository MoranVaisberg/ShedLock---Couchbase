package com.demo.configuration;

import com.couchbase.client.java.Bucket;
import com.demo.entity.LockRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.data.couchbase.repository.config.RepositoryOperationsMapping;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableCouchbaseRepositories
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {

    @Value("${spring.couchbase.bootstrap-hosts}")
    String bootstrapHosts;

    @Value("${spring.couchbase.bucket.name}")
    String bucketName;

    @Value("${spring.couchbase.bucket.password}")
    String bucketPassword;

    @Override
    protected List<String> getBootstrapHosts() {
        return Arrays.asList(bootstrapHosts);
    }

    @Override
    protected String getBucketName() { return bucketName; }

    @Override
    protected String getBucketPassword() { return bucketPassword;  }

    @Override
    public void configureRepositoryOperationsMapping(RepositoryOperationsMapping baseMapping) {
        try {
            CouchbaseTemplate couchbaseTemplate = getTemplate();
            baseMapping.mapEntity(LockRepository.class, couchbaseTemplate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bean
    public CouchbaseTemplate getTemplate() throws Exception {
        CouchbaseTemplate template = new CouchbaseTemplate(
                couchbaseClusterInfo(),
                getBucket(),
                mappingCouchbaseConverter(), translationService()
        );
        template.setDefaultConsistency(getDefaultConsistency());
        return template;
    }

    @Bean
    public Bucket getBucket() throws Exception {
        return couchbaseCluster().openBucket(getBucketName(), getBucketPassword());
    }

}
