package com.julio.urlshortenerapi.config;

import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.data.cassandra.core.cql.keyspace.SpecificationBuilder;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "com.julio.urlshortenerapi")
public class CassandraConfig extends AbstractCassandraConfiguration {

  @Value("${spring.cassandra.contact-points}")
  protected String contactPoints;

  @Value("${spring.cassandra.local-datacenter}")
  protected String localDatacenter;

  @Value("${spring.cassandra.keyspace}")
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
  public SchemaAction getSchemaAction() {
    return SchemaAction.CREATE_IF_NOT_EXISTS;
  }

  @Override
  @NullMarked
  public String[] getEntityBasePackages() {
    return new String[] { "com.julio.urlshortenerapi" };
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
