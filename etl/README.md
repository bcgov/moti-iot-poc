# Oahu ETL

This is a microservice for the *MOTI IOT Proof of concept Demo*
that is a subscriber to the Kapua broker.

This client listens for all events then publishes them into the PostgreSQL database.

Uses the OpenShift S2I to create an image.

## Environment variables for OpenShift
	        H2_DATABASE
            H2_USER
            H2_PASSWORD

            PG_DATABASE
            PG_USER
            PG_PASSWORD"

            BROKER_URL
            BROKER_TOPIC
            BROKER_USER
            BROKER_PASSWORD


## Running the code
`mvn compile`
`mvn exec:java`
