# Security

H2O contains security features intended for deployment inside a secure data center.

## Security model

Below is a discussion of what the security assumptions are, and what the H2O software does and does not do.

### Terms

Term | Definition
---  | ---
**H2O cluster** | A collection of H2O nodes that work together.  In the H2O Flow Web UI, the cluster status menu item shows the list of nodes in an H2O cluster.
**H2O node** | One JVM instance running the H2O main class.  One H2O node corresponds to one OS-level process.  In the YARN case, one H2O node corresponds to one mapper instance and one YARN container.
**H2O embedded web port** | Each H2O node contains an embedded web port (by default port 54321).  This web port hosts H2O Flow as well as the H2O REST API.  The user interacts directly with this web port.
**H2O internal communication port** | Each H2O node also has an internal port (web port+1, so by default port 54322) for internal node-to-node communication.  This is a proprietary binary protocol.  An attacker using a tool like tcpdump or wireshark may be able to reverse engineer data captured on this communication path.

### Assumptions (threat model)

1.  H2O lives in a secure data center.

1.  Denial of service is not a concern.
    * H2O is not designed to withstand a DOS attack.

1.  HTTP traffic between the user client and H2O cluster needs to be encrypted.
    * This is true for both interactive sessions (e.g the H2O Flow Web UI) and programmatic sessions (e.g. an R program).

1.  Man-in-the-middle attacks are of low concern.
    * Certificate checking on the client side for R/python is not yet implemented.
  
1.  Internal binary H2O node-to-H2O node traffic does not need to be secured.
    * The customer is responsible for the H2O cluster's perimeter security if this is a concern.
    * An example would be putting the nodes for an H2O cluster in a VLAN and opening up one port so user clients can reach the H2O cluster on the embedded web port.

1.  You trust the person that starts H2O to start it correctly.
    * Enabling H2O security requires specifying the correct security options.

1.  User client sessions do not need to expire.  A session lives at most as long as the cluster lifetime.  H2O clusters are started and stopped "frequently enough".
    * All data is stored in-memory, so restarting the H2O cluster wipes all data from memory, and there is nothing to clean from disk.

1.  Once a user is authenticated for access to H2O, they have full access.
    * H2O supports authentication but not authorization or access control (ACLs).
    
1.  H2O clusters are meant to be accessed by only one user.
    * Each user starts their own H2O cluster.
    * H2O only allows access to the embedded web port to the person that started the cluster.

### Data chain-of-custody in a Hadoop data center environment

> Notes:   
> - This holds true for all versions of Hadoop (including YARN) supported by H2O.

Through this sequence, it is shown that a user is only able to access the same data from H2O that they could already access from normal Hadoop jobs.

1.  Data lives in HDFS
2.  The files in HDFS have permissions
3.  An HDFS user has permissions (capabilities) to access certain files
4.  Kerberos (kinit) can be used to authenticate a user in a Hadoop environment
5.  A user's Hadoop MapReduce job inherits the permissions (capabilities) of the user, as well as kinit metadata
6.  H2O is a Hadoop MapReduce job
7.  H2O can only access the files in HDFS that the user has permission to access
8.  Only the user that started the cluster is authenticated for access to the H2O cluster
9.  The authenticated user can access the same data in H2O that he could access via HDFS

### What is being secured today

1. Standard file permissions security is provided by the Operating System and by HDFS.

2. The embedded web port in each node of H2O can be secured in two ways:

Method | Description
--- | ---
HTTPS | Encrypted socket communication between the user client and the embedded H2O web port.
Authentication | An HTTP Basic Auth username and password from the user client.

> Note: Embedded web port HTTPS and authentication may be used separately or together.

### What is specifically not being secured today

*  Internal H2O node-to-H2O node communication.

## File security in H2O

H2O is a normal user program.  Nothing specifically needs to be done by the user to get file security for H2O.  Operating System and HDFS permissions "just work".

### Standalone H2O

Since H2O is a regular Java program, the files H2O can access are restricted by the user's Operating System permissions (capabilities).

### H2O on Hadoop

Since H2O is a regular Hadoop MapReduce program, the files H2O can access are restricted by the standard HDFS permissions of the user that starts H2O.

Since H2O is a regular Hadoop MapReduce program, Kerberos (kinit) works seamlessly.  (No code was added to H2O to support Kerberos file security.)

### Sparkling Water on YARN

Similar to H2O on Hadoop, this configuration is H2O on Spark on YARN.  The YARN job inherits the HDFS permissions of the user.

## Embedded web port (by default port 54321) security

For the client side, connection options exist.

For the server side, startup options exist to facilitate security.  These are detailed below.

### HTTPS

#### HTTPS client side

##### Flow Web UI client

When HTTPS is enabled on the server side, the user must provide the https URI scheme to the browser.  No http access will exist.

##### R client

The following code snippet demonstrates connecting to an H2O cluster with HTTPS:

```
h2o.init(ip = "a.b.c.d", port = 54321, https = TRUE, insecure = TRUE)
```

The underlying HTTPS implementation is provided by RCurl and by extension libcurl and OpenSSL.

> **Caution:**   
> Certificate checking has not been implemented yet.  The insecure flag tells the client to ignore certificate checking.  This means your client is exposed to a man-in-the-middle attack.  We assume for the time being that in a secure corporate network such attacks are of low concern.  Currently, the insecure flag must be set to TRUE so that in some future version of H2O you will confidently know when certificate checking has actually been implemented.

##### Python client

Not yet implemented.  Please contact H2O for an update.

#### HTTPS server side

A [Java Keystore](https://en.wikipedia.org/wiki/Keystore) must be provided on the server side to enable HTTPS.
Keystores can be manipulated on the command line with the [keytool](http://docs.oracle.com/javase/6/docs/technotes/tools/solaris/keytool.html) command.

The underlying HTTPS implementation is provided by Jetty 8 and the Java runtime.  (Note:  Jetty 8 was chosen to retain Java 6 compatibility.)

##### Standalone H2O

The following options are available:

```
-jks <filename>
     Java keystore file

-jks_pass <password>
     (Default is 'h2oh2o')
```

Example:

```
java -jar h2o.jar -jks h2o.jks
```

##### H2O on Hadoop

The following options are available:

```
-jks <filename>
     Java keystore file

-jks_pass <password>
     (Default is 'h2oh2o')
```

Example:

```
hadoop jar h2odriver.jar -n 3 -mapperXmx 10g -jks h2o.jks -output hdfsOutputDirectory
```

##### Sparkling Water

The following Spark conf properties exist for Java Keystore configuration:

Spark conf property | Description
--- | ---
spark.ext.h2o.jks | Path to Java Keystore
spark.ext.h2o.jks.pass | JKS password

Example:

```
$SPARK_HOME/bin/spark-submit --class water.SparklingWaterDriver --conf spark.ext.h2o.jks=/path/to/h2o.jks sparkling-water-assembly-0.2.17-SNAPSHOT-all.jar
```

##### Creating your own self-signed Java Keystore

Here is an example of how to create your own self-signed Java Keystore (mykeystore.jks) with a custom keystore password (mypass) and how to run standalone H2O using your Keystore:

```
# Be paranoid and delete any previously existing keystore.
rm -f mykeystore.jks

# Generate a new keystore.
keytool -genkey -keyalg RSA -keystore mykeystore.jks -storepass mypass -keysize 2048
What is your first and last name?
  [Unknown]:  
What is the name of your organizational unit?
  [Unknown]:  
What is the name of your organization?
  [Unknown]:  
What is the name of your City or Locality?
  [Unknown]:  
What is the name of your State or Province?
  [Unknown]:  
What is the two-letter country code for this unit?
  [Unknown]:  
Is CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown correct?
  [no]:  yes

Enter key password for <mykey>
	(RETURN if same as keystore password):  

# Run H2O using the newly generated self-signed keystore.
java -jar h2o.jar -jks mykeystore.jks -jks_pass mypass
```

## Authentication 
H2O client and server side configuration for LDAP and Kerberos is discussed below.  Authentication is implemented using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication).

### LDAP authentication



#### LDAP H2O-client side
##### Flow Web UI client

When authentication is enabled, the user will be presented with a username and password dialog box when attempting to reach Flow.

##### R client

The following code snippet demonstrates connecting to an H2O cluster with authentication:

```
h2o.init(ip = "a.b.c.d", port = 54321, username = "myusername", password = "mypassword")
```

##### Python client

Not yet implemented.  Please contact H2O for an update.

#### LDAP H2O-server side

An ldap.conf configuration file must be provided by the user.  As an example, this file works for H2O's internal LDAP server.  You will certainly need help from your IT security folks to adjust this configuration file for your environment.

Example **ldap.conf**:

```
ldaploginmodule {
    org.eclipse.jetty.plus.jaas.spi.LdapLoginModule required
    debug="true"
    useLdaps="false"
    contextFactory="com.sun.jndi.ldap.LdapCtxFactory"
    hostname="ldap.0xdata.loc"
    port="389"
    bindDn="cn=admin,dc=0xdata,dc=loc"
    bindPassword="0xdata"
    authenticationMethod="simple"
    forceBindingLogin="true"
    userBaseDn="ou=users,dc=0xdata,dc=loc"
    userRdnAttribute="uid"
    userIdAttribute="uid"
    userPasswordAttribute="userPassword"
    userObjectClass="inetOrgPerson"
    roleBaseDn="ou=groups,dc=0xdata,dc=loc"
    roleNameAttribute="cn"
    roleMemberAttribute="uniqueMember"
    roleObjectClass="groupOfUniqueNames";
};
```

See the [Jetty 8 LdapLoginModule documentation](http://wiki.eclipse.org/Jetty/Feature/JAAS#LdapLoginModule) for more information.

##### Standalone H2O

The following options are available:

```
-ldap_login
      Use Jetty LdapLoginService

-login_conf <filename>
      LoginService configuration file
     
-user_name <username>
      Override name of user for which access is allowed
```

Example:

```
java -jar h2o.jar -ldap_login -login_conf ldap.conf

java -jar h2o.jar -ldap_login -login_conf ldap.conf -user_name myLDAPusername
```

##### H2O on Hadoop

The following options are available:

```
-ldap_login
      Use Jetty LdapLoginService

-login_conf <filename>
      LoginService configuration file
     
-user_name <username>
      Override name of user for which access is allowed
```

Example:

```
hadoop jar h2odriver.jar -n 3 -mapperXmx 10g -ldap_login -login_conf ldap.conf -output hdfsOutputDirectory

hadoop jar h2odriver.jar -n 3 -mapperXmx 10g -ldap_login -login_conf ldap.conf -user_name myLDAPusername -output hdfsOutputDirectory
```

##### Sparkling Water

The following Spark conf properties exist for Java keystore configuration:

Spark conf property | Description
--- | ---
spark.ext.h2o.ldap.login | Use Jetty LdapLoginService
spark.ext.h2o.login.conf | LoginService configuration file
spark.ext.h2o.user.name | Override name of user for which access is allowed

Example:

```
$SPARK_HOME/bin/spark-submit --class water.SparklingWaterDriver --conf spark.ext.h2o.ldap.login=true --conf spark.ext.h2o.login.conf=/path/to/ldap.conf sparkling-water-assembly-0.2.17-SNAPSHOT-all.jar

$SPARK_HOME/bin/spark-submit --class water.SparklingWaterDriver --conf spark.ext.h2o.ldap.login=true --conf spark.ext.h2o.user.name=myLDAPusername --conf spark.ext.h2o.login.conf=/path/to/ldap.conf sparkling-water-assembly-0.2.17-SNAPSHOT-all.jar
```

### Kerberos authentication

#### Kerberos H2O-client side
##### Flow Web UI client

When authentication is enabled, the user will be presented with a username and password dialog box when attempting to reach Flow.

##### R client

The following code snippet demonstrates connecting to an H2O cluster with authentication:

```
h2o.init(ip = "a.b.c.d", port = 54321, username = "myusername", password = "mypassword")
```

##### Python client
For Python, connecting to H2O with authentication is similar:
```
h2o.init(ip = "a.b.c.d", port = 54321, username = "myusername", password = "mypassword")
```

#### Kerberos H2O-server side
You must provide a simple configuration file that specifies the Kerberos login module

Example **kerb.conf**:
```
krb5loginmodule {
     com.sun.security.auth.module.Krb5LoginModule required;
};
```
For more detail about Kerberos configuration: [Krb5LoginModule](https://docs.oracle.com/javase/7/docs/jre/api/security/jaas/spec/com/sun/security/auth/module/Krb5LoginModule.html), [Jaas note](http://docs.oracle.com/javase/7/docs/technotes/guides/security/jgss/tutorials/AcnOnly.html)

##### Standalone H2O

The following options are required for Kerberos authentication:

```
-kerberos_login
      Use Jetty LdapLoginService

-login_conf <filename>
      LoginService configuration file
     
-user_name <username>
      Override name of user for which access is allowed
```

The following JVM options must be set:
```
 -Djava.security.krb5.realm=kerberos_realm -Djava.security.krb5.kdc=kdc_server_hostname
```

Example:

```
java  -Djava.security.krb5.realm="0XDATA.LOC" -Djava.security.krb5.kdc="ldap.0xdata.loc" -jar h2o.jar -kerberos_login -login_conf kerb.conf -user_name kerb_principal

```

##### H2O on Hadoop

The following options are available:

```
-kerberos_login
      Use Jetty LdapLoginService

-login_conf <filename>
      LoginService configuration file
     
-user_name <username>
      Override name of user for which access is allowed
```

Use the HADOOP_OPTIONS environment variable to set the JVM options for the worker nodes
```
export HADOOP_OPTS="-Djava.security.krb5.realm=kerberos_realm -Djava.security.krb5.kdc=kdc_server_hostname"
```
Example:

```
export HADOOP_OPTS="-Djava.security.krb5.realm=0XDATA.LOC -Djava.security.krb5.kdc=ldap.0xdata.loc"
hadoop jar h2odriver.jar -n 3 -mapperXmx 10g -kerberos_login -login_conf kerb.conf -output hdfsOutputDirectory -user_name kerb_principal

```

##### Sparkling Water

The following Spark conf properties exist for Kerberos configuration:

Spark conf property | Description
--- | ---
spark.ext.h2o.kerberos.login | Use Jetty Krb5LoginModule
spark.ext.h2o.login.conf | LoginService configuration file
spark.ext.h2o.user.name | Name of user for which access is allowed

Also, spark.driver.extraJavaOptions must be used to configure the Kerberos authentication service:

```
--conf "spark.driver.extraJavaOptions=-Djava.security.krb5.realm=kerberos_realm -Djava.security.krb5.kdc=kdc_server_hostname" 
```

Example:

```
$SPARK_HOME/bin/spark-submit --class water.SparklingWaterDriver --conf spark.ext.h2o.kerberos.login=true --conf spark.ext.h2o.user.name=kerb_principal --conf spark.ext.h2o.login.conf=kerb.conf --conf "spark.driver.extraJavaOptions=-Djava.security.krb5.realm=0XDATA.LOC -Djava.security.krb5.kdc=ldap.0xdata.loc" sparkling-water-assembly-0.2.17-SNAPSHOT-all.jar
```

### Hash file authentication

H2O client and server side configuration for a hardcoded hash file is discussed below.  Authentication is implemented using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication).

#### Hash file H2O-client side

##### Flow Web UI client

When authentication is enabled, the user will be presented with a username and password dialog box when attempting to reach Flow.

##### R client

The following code snippet demonstrates connecting to an H2O cluster with authentication:

```
h2o.init(ip = "a.b.c.d", port = 54321, username = "myusername", password = "mypassword")
```

##### Python client

Not yet implemented.  Please contact H2O for an update.

#### Hash file H2O-server side

A realm.properties configuration file must be provided by the user.

Example **realm.properties**:

```
# See https://wiki.eclipse.org/Jetty/Howto/Secure_Passwords
#     java -cp h2o.jar org.eclipse.jetty.util.security.Password
username1: password1
username2: MD5:6cb75f652a9b52798eb6cf2201057c73
```

Generate secure passwords using the Jetty secure password generation tool:

```
java -cp h2o.jar org.eclipse.jetty.util.security.Password username password
```

See the [Jetty 8 HashLoginService documentation](http://wiki.eclipse.org/Jetty/Tutorial/Realms#HashLoginService) and [Jetty 8 Secure Password HOWTO](http://wiki.eclipse.org/Jetty/Howto/Secure_Passwords) for more information.

##### Standalone H2O

The following options are available:

```
-hash_login
      Use Jetty HashLoginService
          
-login_conf <filename>
      LoginService configuration file
```

Example:

```
java -jar h2o.jar -hash_login -login_conf realm.properties
```

##### H2O on Hadoop

The following options are available:

```
-hash_login
      Use Jetty HashLoginService
          
-login_conf <filename>
      LoginService configuration file
```

Example:

```
hadoop jar h2odriver.jar -n 3 -mapperXmx 10g -hash_login -login_conf realm.propertes -output hdfsOutputDirectory
```

##### Sparkling Water

The following Spark conf properties exist for hash login service configuration:

Spark conf property | Description
--- | ---
spark.ext.h2o.hash.login | Use Jetty HashLoginService
spark.ext.h2o.login.conf | LoginService configuration file

Example:

```
$SPARK_HOME/bin/spark-submit --class water.SparklingWaterDriver --conf spark.ext.h2o.hash.login=true --conf spark.ext.h2o.login.conf=/path/to/realm.properties sparkling-water-assembly-0.2.17-SNAPSHOT-all.jar
```
