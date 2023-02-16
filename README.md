Architecture
------------
AkkaHttp (The actual Akka HTTP framework. It will include the ROutes form karedo_routes: @TODO: karedo_akka_http)
     |
     v
karedo_routes (Contains the Spray Routes to teh actual API calls.
     |
     v
karedo_actors (Contains intermediary Akka Actors to offload work.
     |
     v
karedo_persist (Access to MongoDB. Contains the DAO)

Sequence
--------
AkkaHttp adds routes from karedo_spray. (@TODO: karedo_akka_http)
When route us activated, it hits karedo_routes .exec().
karedo_routes offloads it to the actor layer karedo_actors
karedo_actors interfaces with the database using karedo_persist DAO objects.
