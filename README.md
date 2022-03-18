# es-ilm-demo

## Info

This demo app represents the ILM policy configuration for hot-warm-delete index lifecycle.

## Setup

You will need Elasticsearch 8.1.0 and Kibana docker images:

```
docker pull elasticsearch:8.1.0 
docker network create mynetwork
docker run -d --name elasticsearch --net mynetwork -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:8.1.0
docker pull docker.elastic.co/kibana/kibana:8.1.0                                          
docker run --name kib-01 --net mynetwork -p 5601:5601 docker.elastic.co/kibana/kibana:8.1.0
```

It might be necessary to open Elasticsearch container and copy/paste or manually generate/reset after the first run:
1. The enrollment token to use in Kibana. 
Can be generated with `bin/elasticsearch-create-enrollment-token --scope kibana`
2. Username/password to use in API calls
Can be generated with `bin/elasticsearch-reset-password -u username`


## Testing

1. Set the poll interval time to 5s for easier testing on low age configs. Default is 10 minutes which means even if max_age of hot phase is lower, it will often be at least 10 minutes before the rollover to the next phase

```
PUT _cluster/settings
{
"transient": {
    "indices.lifecycle.poll_interval": "5s"
  }
}
```

2. Start the project. The main method will just start the ILM cycle

3. To check the status of the indexes in ILM, use this query:

```
GET .ds-messages-*/_ilm/explain?human
```
