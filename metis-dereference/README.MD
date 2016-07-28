# Metis Dereference Service
This module includes dereferencing functionality for Metis services.
The module includes the following submodels:

Module | Functionality
---|--- 
dereference-client | REST client for Dereferencing REST API
dereference-common | Object model for dereferencing functioanlity
dereference-service| Main functionality of dereferencing service
dereference-rest | REST API for dereferencing service

Dereferencing service allows the inclusion of external resources representing contextual classes in a Europeana record.
 This is achieved by means of providing XSLs that can convert the external resources to EDM contextual classes and the endpoint 
 where the contextual resources can be accessible. The service allows for advanced functionality such as extracting different 
 contextual classes from the same remote controlled vocabulary. The service functionality is exposed via a REST API.