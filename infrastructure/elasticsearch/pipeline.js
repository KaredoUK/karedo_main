var source = mongodb({
  "uri": "mongodb://admin:FNPQYDEFFCQDCGWX@sl-eu-lon-2-portal.2.dblayer.com:16096,sl-eu-lon-2-portal.3.dblayer.com:16096/admin",
  // "timeout": "30s",
  "tail": true,
  "ssl": true,
  // "cacerts": ["/path/to/cert.pem"],
  // "wc": 1,
  // "fsync": false,
  // "bulk": false,
  // "collection_filters": "{}"
  "namespace": "Karedo.KAR_APIMessage"
})

var sink = elasticsearch({
  "uri": "https://admin:NYVTVIDHWCRRCUAX@sl-eu-lon-2-portal3.dblayer.com:16981/"
  // "timeout": "10s", // defaults to 30s
  // "aws_access_key": "ABCDEF", // used for signing requests to AWS Elasticsearch service
  // "aws_access_secret": "ABCDEF" // used for signing requests to AWS Elasticsearch service
})

t.Source(source).Save(sink)
