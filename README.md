Marin Eduard-Constantin, 321CA <br />
November 2021

<h1>VideosDB</h1>

<h2>Structure:</h2>

In order to store the entities in a database, every entity has a custom
class, and all the entities are gathered in a Repository, which is
implemented as a singleton and contains a list for every entity type.

* Entities:
  * Actor
  * User
  * Movie and Serial, which extends the Video class


For every operation that can be done on the database, there is a class
that extends an abstract class Action, and overrides its method that allow
to apply a general operation on the database.

* Action
  * Command
  * Query
  * Recommendation

Keeping and executing the actions is done by an ActionCenter class, which
stores them in a list and have a method that handles the operations, calling
(at runtime) the corresponding implementation for a general solving method
from Action for each received command.

To sort and filter the data, when the operation specifies this, static helper
methods were defined in the CustomFilter and CustomSort classes. The sorting is
done by creating new comparators, according to the possible sorting criteria.

<h2>Flow:</h2>
Entities that form the database are loaded into memory in a Repository
type object, and the actions that will be performed on the database are stored
in an ActionCenter instance, based on their type.<br />
After gathering them, the actions are applied in ActionCenter, calling
a method that is overridden in every subclass of Action, and the output of
every operation is added to a JSONArray .

Depending on the action type, the execution can be split on three branches:

- command - in a Command instance, the command will be executed as it follows:
  - check if the user associated with the command exists in the database
  - modify the database accordingly to Command variables, which describes
          the command behaviour
  - return the output message
- query - uses a Query instance
  - interrogates the database
  - filters the result, using a static custom method that filters the data
    using a list of filters
  - sorts the result, using a static custom method that sorts a list
    of entities using a given property as the first criteria
    (the properties are stored in a map in which every entity is associated
    to its property value), and the alphabetical order as the second criteria
- recommendation - uses a Recommendation instance
  - check if the user exists in the database and have the required
    subscription type
  - process the data from database accordingly to the recommendation type
    that have to be performed
  - generate a result

When a new database have to be loaded, the old data from Repository is removed
and the new data is stored into the database.
