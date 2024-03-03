#### Add dependency
```groovy
implementation 'org.springframework.cloud:spring-cloud-vault-config:4.1.0'
```

#### Start server
```bash
vault server --dev
```

#### Add environment variables
```bash
export VAULT_ADDR="http://127.0.0.1:8200"
export VAULT_TOKEN="hvs.CBJkEIngBjsD5AIHM2sRSCcH"
```

#### Add Spring configuration in properties.yaml file

```yaml
spring:
  cloud:
    vault:
      uri: http://127.0.0.1:8200
      token: hvs.CBJkEIngBjsD5AIHM2sRSCcH
      kv:
        enabled: true
      application-name: pathfinder
  config:
    import: vault://
```

#### Basic commands

```bash
vault kv put secret/pathfinder DB_USER=root DB_PASS=root
vault kv patch secret/pathfinder DB_USER=admin
vault kv get secret/pathfiinder
vault kv destriy secret/pathfinder
```

#### Tutorial
```
https://developer.hashicorp.com/vault/tutorials/getting-started/getting-started-dev-server
```