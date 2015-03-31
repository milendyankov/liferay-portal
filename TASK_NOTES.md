## Task

 1. move LDAP configuration from portal.properties to Confing 
 1. split configuration into 2 pieces 
	1. Portal instance (company) specific - 1 per company 
	1. Both portal instance (company) and LDAP instance specific - many per company
 1. replace current implementation to use the new configuration 
 1. provide migration/upgrade path 

## Assumptions

 1. UI will be taken care of separately 
 1. Configuration persistence will be taken care of separately 

## Design decisions

### Portal instance (company) specific configuration

 1. PID: `com.liferay.portal.ldap.configuration.LDAPIntegrationConfiguration`
 1. A OSGi service (let's say `LDAPIntegrationRegistry`) will keep track of all configurations (notified on create/modify/remove configuration)!
 1. All other components must access Company specific LDAP configuration properties exclusively through that service! 
 1. A configuration validation component (let's say `LDAPIntegrationConfigurationValidator`) will be able to validate the consistency and correctness of the configuration. In particular it will 
	1. report error if value for `companyId` property is missing in the configuration
	1. report warning if `companyId` refers to nonexistent instance 
	1. report warning if new configuration has the same `companyId` as existing one
 1. `LDAPIntegrationRegistry` will verify each configuration change it gets notified about against `LDAPIntegrationConfigurationValidator` and 
	1. log error/warning message for each error/warning
	1. ignore any new configuration for which validation reports error
	1. ignore existing configuration for which validation reports error after modification
 

### Portal instance (company) specific configuration

 1. PID: `com.liferay.portal.ldap.configuration.LDAPServerConfiguration`
 1. A OSGi service (let's say `LDAPServerRegistry`) will keep track of all configurations (notified on create/modify/remove configuration)!
 1. All other components must access LDAP server specific configuration properties exclusively through that service! 
 1. A configuration validation component (let's say `LDAPServerConfigurationValidator`) will be able to validate the consistency and correctness of the configuration. In particular it will 
	1. report error if value for `ldapServerId` property is missing in the configuration
	1. report warning if new configuration has the same `ldapServerId` as existing one
	1. report warning if any of the values (comma separated list) of `companyIds` refers to nonexistent instance 
 1. `LDAPServerRegistry` will verify each configuration change it gets notified about against `LDAPServerConfigurationValidator` and 
	1. log error/warning message for each error/warning
	1. ignore any new configuration for which validation reports error
	1. ignore existing configuration for which validation reports error after modification

## Known/dicovered issues

 1. Config Admin UI can not handle list types ('optionValues' in metatype)
 1. Deleting configuration from UI does not remove the `cfg` file (if such exists). Configuration will be restored when 
	1. Server is restarted with clean OSGi bundle cache
	1. `cfg` file is _touched_
 1. Config Admin UI can not handle array types (for example `isbns` in `AmazonRankingsConfiguration` or `hiddenVariables` in `IFrameConfiguration`) After first save from UI those are displayed as `[Ljava.lang.String;@3727c6c` and the configuration can not be saved anymore!

 
