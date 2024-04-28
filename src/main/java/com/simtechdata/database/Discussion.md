# Here is the discussion I originally wrote in the `SQLite` class just above the `createDatabase()` method until I realized that both of these extensive comments needed to be in their own MD file

This method createDatabase needs some explanation that is not obvious just by looking at the code because of the way
that SQLite does what it does. So first we look for the existence of the database file and if it is not there, then we
make the parent folders for it. This paves the space for the next command to execute without throwing an error.

It is the very next command that actually creates the database file itself.

	DriverManager.getConnection(connString);

This is also the same command that we use to get a Connection object so that we can interact with the database at all,
but we usually assign that command to a variable (see the getConn() method). But in this case, I'm only using it to make
sure that the database file itself has been created.

Then, once the database file has been created, we have to create the schema (tables and relationships) and that is
handled in the iteration of the String split because you cannot run both table creation statements as a single command,
I learned that I had to split them up and execute each one on its own otherwise it would only create the first table.

The command that creates the tables is

	conn.createStatement().executeUpdate(table);

conn is the connection that the library establishes with the database file. createStatement is a request letting the
connection know that we wish to execute a SQL statement against some object in the database, and executeUpdate tells it
that the SQL statement that we want to run will be invasive - meaning it will change the stuff in the database file (as
opposed to just reading stuff from the file).

If you were going to execute a read query, you would use conn.createStatement.executeQuery(SQL) with your SQL statement
passed in as argument.

You will see examples of this in addition to one other way of doing this in the DB class and I will put notes in there
too.

# This was originally written as a block comment in the DB class

I don't really annotate these methods at all. But as you examine them, you will see a pattern emerging... This pattern
is typical of Java interaction with pretty much any relational database (NO SQL is very different)

For me, I have only ever needed to use one of three different ways of accessing a database and this is the same for
MySQL as it is for SQLite ... it would also be the same for Microsoft SQL or Oracle or any database engine that can be
talked to through JDBMS (Java Database Management System) of which a bunch of different libraries exist, but the ones in
this pom file are the ones I use exclusively.

The first two methods of interacting with the database is

	Statement statement = conn.createStatement();
	statement.executeUpdate(SQL)

Which as you saw in the SQLite class, could also be written as:

	conn.createStatement().executeUpdate(SQL); (this is a table modifying call)

	OR

	statement.executeQuery(SQL);
	(This does not modify a table it only reads from a table and it is important to use the right call - update vs just query)

With those two methods, you **cannot** pass into the SQL statement, any dynamic content. These execute STATIC SQL
statements that you write, and you should never use SQL statements that you alter from method arguments because the
JDBMS libraries have specific methods for dynamic SQL statements... and that looks like this:

    String SQL = "SELECT * FROM MyTable WHERE SomeBooleanColumn = ?;"; 
    (that question mark is everything in this context)
    
    PreparedStatement pst = conn.prepareStatement(SQL)

Next, what we do is we fill the question mark by using the methods in the library:

    pst.setBoolean(1, true);

The number 1 is indicating that we are referencing the first question mark in the SQL statement and the value after it
is of course the value we need to assign to that question mark. If you look at the methods iun the DB class, you will
see several examples where a SQL statement has multiple question marks with varying data types.

The reason why it is imperative to do it this way, is because each database engine has different syntax requirements
that define SQL arguments as different data types (Strings vs boolean vs Double etc. etc.) and so doing it this way
GUARANTEES that the String sent to the server is always formatted correctly. The mis-formatting of Strings being sent to
database servers was such a problem, that this method for sending dynamic content emerged, and it is wonderful because
it's one less thing we need to be concerned about - even though it is thick with code ... but I mitigate this by using
Live Templates in IntelliJ ... I can tap out one of these methods in less than 30 seconds most of the time, because of
*Live Templates* in IJ.

The last thing that we need to cover is how we get the data back from the database. And that is through the class...

	ResultSet rs = pst.executeQuery();
	ResultSet rs = conn.createStatement.executeQuery(SQL);

	(remember with a PreparedStatement `pst`, we include the SQL String with the
	instantiation of the pst object so no need to pass it in as an argument here)

ResultSet will contain the response back from the database server and that response has to be iterated over because we
can only access one record at a time (which should make sense). And just as when we filled in the question marks in the
SQL statement when we use the `pst.setBoolean(index, value);` method. We access the data returned using a similar
syntax.

And there is a direct connection between retrieving the data to the way you create your SQL statement.

For example, if you write your statement so that it returns ALL the columns in a table, like this:

	SELECT * FROM Employees;

Then you will need to know the exact names of the columns that you wish to pull from the results of the SQL statement.
For example, lets say that we only want to get the FirstName and LastName from the results of the above SQL query:

	(We defined the `rs` object above)

	while(rs.next()) {
		String firstName = rs.getString("FirstName");
		String lastName  = rs.getString("LastName");
    }

You would then most likely have some kind of List object that you would be adding data into. OR, you would have defined
a custom class that would hold the data you needed to access then you would have your List object be a List of that
custom class etc. etc.

NOW, if you defined your SQL statement like this:

	String SQL = "Select FirstName, LastName FROM Employees;";

Then you COULD pull the data like this:

	while(rs.next()) {
		String firstName = rs.getString(1);
		String lastName  = rs.getString(2);
	}

Where the index number is connected to the position of the specified columns in the SQL statement.

What is also true, is that if you knew where the FirstName and LastName columns actually were - positionally - within
the entire table ... then the first example where we selected the wild card could still be accessed using the position
number of the columns inside the table:

	String SQL = "SELECT * FROM Employees;";
	ResultSet rs = conn.createStatement.executeQuery(SQL);
	while(rs.next()) {
		String firstName = rs.getString(3);
		String lastName  = rs.getString(4);
	}

This can only be done, of course, if you know exactly which column those fields exist in - and ...

##### WHEN IT COMES TO SQL SERVERS AND JDBMS - THERE IS NO 0 REFERENCE - EVERYTHING STARTS AT 1 ... There is NO column 0 ... there is NO record 0 ... ZERO REFERENCES TO DO NOT EXIST IN SQL SERVERS - EXCLAMATION POINT!!!

The next thing I think you need to learn after digesting all of the above ... would be to understand how to write SQL
statements. There is a TON of info on that online, so I will only cover the very basics ...

* SELECT - is how you get data from a table.
* INSERT - is how you add data to a table.
* UPDATE - is how you change existing data in a table.
* DELETE - is how you remove records from a table.

And the language is perhaps the most natural and intuitive of any language on the planet.... lets see how that works ...

#### "I need to see a list of all employee first and last names from the EmployeeNames table" ...

	SELECT FirstName, LastName FROM EmployeeNames;

#### "I need to know the first and last name of every employee who was born in the month of March":

	SELECT FirstName, LastName FROM EmployeeNames WHERE Birthdate LIKE "%March%";

And so it goes with SQL statements ... it is very natural ... it flows very well, and they are relatively easy to read
until you start getting into crazy things like inner and outer joins which I never have delved deep into.

But let's look at another example where we need to select data from a table where it has a relationship with another
table:

#### "I need to see employees who live on streets named Rosco Ave., 3rd Street and Arcadia Blvd. because their intersecting roads are going to be under construction next Monday, so I need to let them know that they can work from home next week if they desire."

	SELECT FirstName, LastName, EmailAddress FROM Employees WHERE EmployeeId IN
	  (  	SELECT EmployeeId FROM EmployeeAddress WHERE
				Street LIKE '%Rosco%' OR
				Street LIKE '%3rd%' OR
				Street LIKE '%Arcadia%'
	  );

So we match the `EmployeeId` based on a completely isolated query, but you can see how simple it is to get at the data
we need. That query would produce a single table in RAM containing the related fields from the two different tables. The
database server presents the data back to us in the form of a RAM based table object that in Java we iterate over using
the ResultSet.

Obviously as the structure of your tables becomes more complicated ... everything I just said concerning simplicity goes
straight out the window to where a person must have some deep knowledge about SQL ... because what I have shown you here
does not even remotely scratch the surface ... though what I have written here would be darn close to containing
everything you need to know in order to use a database with a program like Drifty.

We have not covered the actual designing of tables ... but lets save that for a little later. You can see an example in
the SQLite class getSchema() method ... but that will hardly explain to you how to create tables from scratch ... even
though it's a perfectly fine example ... the various data types and ways of defining the columns are not explained at
all.

I prefer to use a GUI tool to create and design my tables because they allow us to visually define the relationships
between table fields (discussed next) without needing to be concerned with the commands and syntax that are required for
defining those relationships.

## RELATIONSHIPS

When it comes to deleting data ... it can get sticky when you have your tables properly normalized because this means
that you will have to have defined relationships between fields across different tables.

So lets say that you have a table that contains records about an employees address, and another table that contains
records about an employees annual reviews (and it is imperative that those two completely disconnected pieces of
information exist in isolated tables because nothing about employee performance reviews has anything at all to do with
where they live ... make sense?

And yet you have a third table that contains the employee first, middle, and last name along with their employee ID
number ... you have to relate that employee to the other two tables somehow ... and we do this through something called
a FOREIGN KEY (and this is where the "Natural and Intuitive" concerning SQL - exists the entire SQL server paradigm ...
though it is very logical and simple to comprehend the fact that we need to define the connections between fields across
tables where that data is in fact related ... the way in which those connections are created is anything but intuitive
and natural.

Personally, I leverage GUI tools where I can drag and drop fields between tables and let the program create the commands
for me that create those relationships. But the relationships themselves have common sense terms:

> ONE-TO-ONE relationships mean that there is only one record from the source table that is related to only one record
> in the other table.

> ONE-TO-MANY relationships mean that there is only ONE record in the source table that is related to ONE OR MORE
> records in the other table.

So in our employee example, the `EmployeeID` inside the `EmployeeNames` table would have a ONE-TO-ONE relationship with
the `EmployeeID` field in the `EmployeeAddress` table BUT it would have a ONE-TO-MANY relationship with the `EmployeeId`
in the `EmployeeReviews` table, since we will be adding an employee review to that table every year.

When you have a ONE-TO-ONE relationship and that relationship connects two fields such as the `EmployeeId`, the database
server allows you to create exactly ONE record in the related table with the value of the relationship from the parent
table ... this means that in our example, a ONE-TO-ONE relationship between `EmployeeNames` and `EmployeeAddresses`
means that we can create ONE RECORD in `EmployeeAddresses` that has a matching value in the `EmployeeId` field in
the `EmployeeNames` table.

If you then try to create a second record with that same `EmployeeId` inside the `EmployeeAddresses` table, the database
server will throw an error and not create the second record.

But in the case of the `EmployeeNames` table in relation to the `EmployeeReviews` table, we can create as many records
in the `EmployeeReview` table that we like that contains a related `EmployeeId` in the `EmployeeNames` table because the
relationship is defined as a ONE-TO-MANY relationship.

#### Make Sense?

WHY do relationships matter? And WHY does the database engine need to know about them? I'm glad you asked ...

BECAUSE when we make a change to the data at the top of those relationships, we need that change to propagate
AUTOMATICALLY to the related tables.

ALSO, when we run SQL statements against the data to pull or put data into the table, we must make sure that the data is
pulling and putting in a manner that is consistent with the real world connections that exists between the various
elements within the data.

That is to say that it is possible to define relationships between tables in such a way that if you try to add data to
the tables where you do that in a manner that it is not consistent with the natural relationship between the fields and
tables, you will generate an error in the response because you violated the rules that you instilled into the tables via
the relationships.

And this touches on a specific discipline within IT that is deep and highly specialized ... there are people who exist
that do nothing more than specialize in accurately and efficiently defining those relationships and specialize in
writing SQL statements so that the results will be correct 100% of the time. I've seen SQL databases with literally
thousands of tables and even more defined relationships ... and maintaining clarity in that kind of potentially
disastrous expression of real world data ... is an art! It takes someone who is devoted to that discipline to engineer
the structure properly and define the back end relationships properly so that the interaction with the data works
properly... And in that context, I am a complete nothing - I understand the nature of those relationships, but I do not
pretend to understand how to relate the data in SQL at those complex levels ... but when a person does have that
comprehension on an intuitive level, they are capable of creating SQL statements and other SQL objects (Stored
Procedures, OLE objects and others) that will make the interface to the data insanely efficient ... and when the
database server is the source of a website that serves millions of hits per second, this kind of ability becomes more
valuable than gold.

But for our purposes ... I use relationships to ensure that when I delete the parent connection in the relationship, all
of the related records are automatically deleted with it so that I don't have orphaned records out there with no
connection to the root data. Without the foreign key relationships defined, I would have to write the methods so that
they deleted data out of multiple tables and that can get cumbersome even in a database with as little as five tables
because the relationships can be subtle and difficult to remember long after you've designed the tables and you just
need to code for them without having to re-engineer the tables in your head every time you need to do something
simple ... relationships allow us to write SQL statements with as little concern for those details as possible.

If you look at the `getSchema()` method in the `SQLite` class, you will see that the `LinkActions` table has a foreign
key relationship to the `Links` table. I have been actually kind of sloppy with this table design structure because I
was in a hurry. This design is not technically normalized properly. To normalize this database PROPERLY, I would have
multiple tables in addition to LinkActions ... I technically should have the database looking like this:

* Links
* Failures
* Deletions
* Downloads

Here is a what the database SHOULD look like when I design it using a GUI tool...

![Tables](../../../../../../OriginalArt/images/Normalized.png)

This MIGHT even be considered FOURTH normal form (which is difficult to attain in massively multi-table database
schemas, but THIRD NORMAL FORM should ALWAYS be the level that you strive to attain, no matter how insignificant the
database schema is ... its mandatory that your brain think in THIRD NORMAL FORM when it comes to database schemas
because that will be the minimum expectation of you if a company is going to hire you where your database skills are
also being considered. so THIRD NORMAL FORM is your buddy!

Lets go over some of this example for some clarity ...

Notice that I have a `TimeStamp` field in every table. I have established the DEFAULT VALUE of those fields (which
cannot be seen in this graphic but if I double clicked on one of the tables, you would see it) to be equal
to `CURRENT_TIMESTAMP` - which is unique to SQLite. In MySQL, you would set that default value to `now()` ... but
because I have a default value defined inside the table, I can insert records into the table without needing to include
the `TimeStamp` field in my `INSERT` statement, NOR do I need to create a Time object in Java and then make sure that
the time object is properly formatted for SQLite where I then include it into the `INSERT` `PreparedStatement` ... the
way I've done it here, the timestamp will always be the exact moment in time when a new record is created in any of
those tables. I only included it for this program as an intention to possibly use it in the future.

The other thing to point out is that most database engines DO NOT have a boolean datatype. MySQL has a data type
called `TINYINT` which only accepts values of 0 or 1 which makes that data type perfect for boolean data storage. SQLIte
only has INTEGER ... but regardless of the back end database engine, when we use the `PreparedStatement` interface to
write data into tables, we can use the methods of the data types that match what we are doing in our program and the
database engine will simply click along with us in harmony.

This does mean, however, that you can get sloppy in your table design ... like you could define every field as a TEXT
object (in SQLite) or a `VARCHAR` in MySQL ... then just define the data types though the JDBMS library ... and it would
work fine ... but you would get fired from a job for doing that because it does have slight potential for throwing
errors not to mention in the ***PERFORMANCE*** context, doing this in large data sets would MURDER the speed of
everything.

### Extra Credit (LOL)

I'm curious to see if you could massage the Table definitions in the `SQLite.getSchema()` method so that the tables
would model the image I created above and then adjust the methods in the `DB` class to align with the new table
definitions. I do believe I've given you enough knowledge just in this MD file to do just that ... especially knowing
how intelligent and logical you are ... this should not be a very difficult task for you.
