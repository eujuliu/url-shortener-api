/* (C)2025 */
package com.example.urlshortenerapi.shared.config;

import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.data.cassandra.core.cql.keyspace.SpecificationBuilder;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Value("${spring.cassandra.contact-points:localhost}")
    protected String contactPoints;

    @Value("${spring.cassandra.local-datacenter:DC1}")
    protected String localDatacenter;

    @Value("${spring.cassandra.keyspace:urlshortener}")
    protected String keyspace;

    @Override
    @NullMarked
    protected String getKeyspaceName() {
        return this.keyspace;
    }

    @Override
    protected @Nullable String getLocalDataCenter() {
        return localDatacenter;
    }

    @Override
    @NullMarked
    protected String getContactPoints() {
        return this.contactPoints;
    }

    @Override
    @NullMarked
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        CreateKeyspaceSpecification specification =
                SpecificationBuilder.createKeyspace(this.getKeyspaceName())
                        .ifNotExists()
                        .with(KeyspaceOption.DURABLE_WRITES, true)
                        .withSimpleReplication(1);

        return List.of(specification);
    }
}
