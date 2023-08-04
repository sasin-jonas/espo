import os
from typing import Dict, Any

# Schema for the Elastic index for Crowdhelix data
es_schema: Dict[str, Any] = {
    "index": "crowdhelix_data",
    "lang": "en",
    "es": {
        "settings": {
            "index": {
                "number_of_shards": "1",
                "number_of_replicas": "0",
            }
        },
        "mappings": {
            "properties": {
                "helix": {
                    "type": "keyword"
                },
                "role": {
                    "type": "keyword"
                },
                "expertise": {
                    "type": "keyword"
                },
                "title": {
                    "type": "text",
                    "term_vector": "yes",
                    "analyzer": "english"
                },
                "url": {
                    "type": "keyword"
                },
                "author": {
                    "type": "keyword"
                },
                "institutionName": {
                    "type": "keyword"
                },
                "institutionUrl": {
                    "type": "keyword"
                },
                "description": {
                    "type": "text",
                    "term_vector": "yes",
                    "analyzer": "english"
                },
                "appendixUrl": {
                    "type": "keyword"
                },
                "ID": {
                    "type": "integer"
                }
            }
        }
    }
}

# connection variables, defaults for localhost used
elastic_host: str = os.environ.get('APP_ELASTIC_HOSTNAME', 'localhost')
elastic_port: int = int(os.environ.get('APP_ELASTIC_PORT', '9200'))

# configuration for Elastic connections
elastic_config: Dict[str, Any] = {
    "host": elastic_host,
    "port": elastic_port,
    "scheme": "http"
}
