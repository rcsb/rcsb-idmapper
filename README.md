# rcsb-idmapper
ID Mapper is designed to facilitate the mapping of PDB and related identifiers. 

## Configuration

The environment variable with the database connection string (URI) expected by the application is `MONGODB_URI`.
The URI connection scheme: `mongodb://username:password@host[:port]/database?authSource=admin&[options]`
  - `mongodb://` Required. Prefix to identify that this is a string in the standard connection format 
  - `username:password` Required. The client will attempt to connect to the specific database using these credentials
  - `host` Required. Identifies a server address to connect to (hostname or IP address)
  - `port` Optional. The default value is __27017__ if not specified
  - `database` Required. The name of the database to connect to
  - `authSource=admin` Required. The name of the database to authenticate
  - `options` Optional. Connection specific options
