# change tracking service

# requirements

- MongoDB 3.0.0 or above running on localhost

# Spotlight

- `chr.service` namespace

Clojure native services that work with maps

- `chr.web.service` namespace

Compojure base web service that handles JSON

# Testing

## Via REPL

```
boot repl
```

### Registering changes

```clojure
(require '[chr.service :as s])

(s/save! {:name "Bruce Norries" :address {:street "Some street" :zipcode {:prefix "123" :code "XYZ"}}})
# => {:_id #object[org.bson.types.ObjectId 0x3cb8670e "588e9a5d77c8a65c3357dd13"], :address {:zipcode {:code "XYZ", :prefix "123"}, :street "Some street"}, :name "Bruce Norries"}

(s/save! {:_id "588e9a5d77c8a65c3357dd13", :address {:zipcode {:code "000", :prefix "000"}, :street "Nakatomi Plaza"}, :name "Bruce Willis"})
# => {:name "Bruce Willis", :address {:street "Nakatomi Plaza", :zipcode {:prefix "000", :code "000"}}, :_id #object[org.bson.types.ObjectId 0x52da2943 "588e9a5d77c8a65c3357dd13"]}

(s/save! {:_id "588e9a5d77c8a65c3357dd13", :address {:zipcode {:code "123", :prefix "333"}, :street "Nakatomi Plaza"}, :name "Mr. Bruce Willis"})
# => {:address {:street "Nakatomi Plaza", :zipcode {:prefix "333", :code "123"}}, :name "Mr. Bruce Willis", :_id #object[org.bson.types.ObjectId 0x68692e1f "588e9a5d77c8a65c3357dd13"]}
```
### Listing changes

```clojure
(s/changes "588e9a5d77c8a65c3357dd13" (s/parse-date "2017-01-30T00:00:00Z") (s/parse-date "2017-01-31T00:00:00Z"))
# => [{:field "name", :old "Bruce Norries", :new "Mr. Bruce Willis"}
      {:field "address.zipcode.code", :old "XYZ", :new "123"}
      {:field "address.zipcode.prefix", :old "123", :new "333"}
      {:field "address.street", :old "Some street", :new "Nakatomi Plaza"}]
```

# Via web service

```
boot run-server
```

### Registering changes

```bash
curl "http://localhost:3360/api/save" -v -H "Content-Type:application/json" -d '{"name": "Bruce Norries", "address": {"street": "Some street", "zipcode": {"prefix": "123", "code": "XYZ"}}}'

{"_id":"588e9ee377c867bde1ded61c","address":{"zipcode":{"code":"XYZ","prefix":"123"},"street":"Some street"},"name":"Bruce Norries"}

curl "http://localhost:3360/api/save" -v -H "Content-Type:application/json" -d '{"_id":"588e9ee377c867bde1ded61c", "name": "Bruce Willis", "address": {"street": "Nakatomi Plaza", "zipcode": {"prefix": "000", "code": "000"}}}'

{"name":"Bruce Willis","address":{"zipcode":{"code":"000","prefix":"000"},"street":"Nakatomi Plaza","_id":"588e9ee377c867bde1ded61c"}}

curl "http://localhost:3360/api/save" -v -H "Content-Type:application/json" -d '{"_id":"588e9ee377c867bde1ded61c", "name": "Bruce Willis", "address": {"street": "Nakatomi
Plaza", "zipcode": {"prefix": "1000", "code": "100"}}}'

{"address":{"zipcode":{"code":"100","prefix":"1000"},"street":"Nakatomi Plaza"},"name":"Bruce Willis","_id":"588e9ee377c867bde1ded61c"}
```

### Listing changes

```bash
curl -v "http://localhost:3360/api/changes/588e9ee377c867bde1ded61c/2017-01-30T00:00:00Z/2017-01-31T00:00:00Z"

[{"field":"address.street", "old":"Some street", "new":"Nakatomi Plaza"},
{"field":"address.zipcode.prefix", "old":"123", "new":"1000"},
{"field":"address.zipcode.code", "old":"XYZ", "new":"100"},
{"field":"name", "old":"Bruce Norries", "new":"Bruce Willis"}]
```

# Unhandled issues

- Some operations invert the key order
- Hide BSON object, show only the id
- Working with plain IDs (non BSON)
