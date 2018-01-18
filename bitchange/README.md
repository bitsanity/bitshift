
# REST endpoints

## Watched Address Management

| URL | VERB | SUCCESS | FAILURE| RESPONSE |
| --- | :---: | :---: | :---: | --- |
| /bitcoin/address/{address} | POST | 201 (Created) | 409 (Conflict) - if already watched|`{"@class":"com.bitsanity.bitchange.canonical.RestMessage","command":"/bitcoin/address/moqX6S9knQUBFQbnwWRPsybMsuZ1d9ZqcP","result":0,"message":"Ok"}`|
||||500 (Internal Server Error) - for any failures such as invalid address|`{"@class":"com.bitsanity.bitchange.canonical.RestMessage","command":"/bitcoin/address/abc123","result":1000,"message":"Trying to add invalid address to watch: abc123"}`|
| /bitcoin/address/{address} | DELETE | 200 (Ok) ||`{"@class":"com.bitsanity.bitchange.canonical.RestMessage","command":"/bitcoin/address/moqX6S9knQUBFQbnwWRPsybMsuZ1d9ZqcP","result":0,"message":"Ok"}`|
|||| 404 (Not Found) - if not already watched|`{"@class":"com.bitsanity.bitchange.canonical.RestMessage","command":"/bitcoin/address/moqX6S9knQUBFQbnwWRPsybMsuZ1d9ZqcP","result":404,"message":"Ok"}`|
||||500 (Internal Server Error) - for any failures such as invalid address|`{"@class":"com.bitsanity.bitchange.canonical.RestMessage","command":"/bitcoin/address/abc123","result":1000,"message":"Trying to remove invalid watch address: abc123"}`|

 