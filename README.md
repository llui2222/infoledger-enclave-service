# README #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for? ###

* Quick summary
* Version
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### How do I get set up? ###

* Summary of set up
* Configuration
* Dependencies
* Database configuration
* How to run tests
* Deployment instructions

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact

### How to build and run Enclave App

First need to install java 11:
```
sudo amazon-linux-extras install java-openjdk11
java -version
sudo /usr/sbin/alternatives --config java
```

Then, build enclave image and start. While building application docker image, 
need to specify target operations which enclave will handle - currently supported 
are `aggregation` and `validation`. This is done by specifying the operations in Dockerfile via
`sed -i 's/ENCLAVE_OPERATIONS/aggregation validation/g' crypto-service-enclave-app/Dockerfile`
```
sudo nitro-cli terminate-enclave --all
mvn clean install
mvn -f crypto-service-enclave-app/pom.xml compile package jib:dockerBuild
sed -i 's/ENCLAVE_REGION/us-west-2/g' crypto-service-enclave-app/Dockerfile
sed -i 's/ENCLAVE_PORT/5000/g' crypto-service-enclave-app/Dockerfile
sed -i 's/PROXY_PORT/8443/g' crypto-service-enclave-app/Dockerfile
sed -i 's/ENCLAVE_OPERATIONS/aggregate validate/g' crypto-service-enclave-app/Dockerfile
docker build crypto-service-enclave-app -t crypto-service-enclave-app
sudo nitro-cli build-enclave --docker-uri crypto-service-enclave-app:latest --output-file crypto-service-enclave-app.eif
sudo nitro-cli run-enclave --cpu-count 2 --memory 3072 --eif-path crypto-service-enclave-app.eif --enclave-cid 10 --debug-mode
sudo nitro-cli console --enclave-id $(nitro-cli describe-enclaves | jq -r ".[0].EnclaveID")
```

### Demo Host application to send a request and get back and print a response

```
KMS_KEY_ARN=$(aws kms list-keys --region us-west-2 | jq -r ".Keys[3].KeyId")
echo $KMS_KEY_ARN

MESSAGE="Hello, KMS"
CIPHERTEXT=$(aws kms encrypt --key-id "$KMS_KEY_ARN" --plaintext "$MESSAGE" --query CiphertextBlob --output text --region us-west-2)
echo $CIPHERTEXT

mvn -f crypto-service-host-demo/pom.xml compile exec:exec -Denclave.cid=$(nitro-cli describe-enclaves | jq -r ".[0].EnclaveCID") -Dencrypted.text=$CIPHERTEXT -Dkey.id=$KMS_KEY_ARN
```

Additionally, one can provide optional access/secret keys and session token -
in this case, the provided credentials will be used for authentication. Otherwise - IAM credentials:
```
mvn -f crypto-service-host-demo/pom.xml compile exec:exec -Denclave.cid=$(nitro-cli describe-enclaves | jq -r ".[0].EnclaveCID") -Dencrypted.text=$CIPHERTEXT -Dkey.id=$KMS_KEY_ARN -Daccess.key=$AWS_ACCESS_KEY -Dsecret.key=$AWS_SECRET_KEY -Dsession.token=$AWS_SESSION_TOKEN
```
Session token is also an optional argument - it's possible to provide only access/secret keys.

### Publish artifacts to maven repository (s3)

#### Deploying a snapshot
mvn --settings settings.xml clean deploy -DIAM_INFOLEDGER_MAVEN_REPO_USER_ACCESS_KEY_ID=XXXX -DIAM_INFOLEDGER_MAVEN_REPO_USER_SECRET_KEY=XXXX

#### Deploying a release
mvn clean versions:set -DnewVersion=1.0.1
mvn --settings settings.xml clean deploy -DIAM_INFOLEDGER_MAVEN_REPO_USER_ACCESS_KEY_ID=XXXX -DIAM_INFOLEDGER_MAVEN_REPO_USER_SECRET_KEY=XXXX

### Quickstart

Quickstart script `setup/aws-quickstart.sh` allows to launch the application in aws.

### Note 1: Enclave Operations

Enclave Application now supports pluggable operations that it can handle
and which can be optionally turned on or off.

The examples are `aggregation` - see `crypto-aggregation-service` maven module,
and `validation` - see `crypto-validation-service` maven module.

It's even possible to develop these modules in different repositories and only
include dependencies on the new plugins to `enclave-app` pom.

Such a plugin should implement `OperationHandler` interface from `crypto-service-enclave-api`
and define api `request` and `response`, which should extend `CryptoRequest` and `CryptoResponse` correspondingly.

Once attached, the enclave application can be started with new operations by specifying 
`--operations <operations_list>` command line argument. Right now this is done
with building the proper docker image via 
```
sed -i "s/ENCLAVE_OPERATIONS/$ENCLAVE_OPERATIONS/g" crypto-service-enclave-app/Dockerfile &&
```
instruction which passes to Dockerfile proper list of operations to launch on this enclave instance.

So, it's possible to have different independent enclave instances each running its
own set of services(operations) and therefore each scaled separately.

Services discovery is done automatically via java Service Loader so no changes 
are required in enclave application.

The proper pluggable operation handle plugin must implement `OperationHandler` interface from `crypto-service-enclave-api`
and properly export the implementation - via a `META-INF/services/com.infoledger.crypto.handler.OperationHandler` file.
The content of the file is the fully qualified class name of the handler implementation - for example,
```
com.infoledger.crypto.validation.ValidationHandler
```