We will be using bcrypt algorithm the hash the passwords before
saving it in the database. Although there are other algorithms also present out there.
One of the algorithms which is considered to be the best is Argon2. It can also be used
to achieve the same goal.

### Bcrypt Algorithm Info -

Bcrypt is a hashing algorithm similar to SHA-1 and other hashing
algorithms out there.
When we hash a certain text, we get some random string.
This random string cannot be backtracked to the text that we hashed easily.
That's why we use hashing for sensitive information like passwords
so that they become difficult to hack.

The difference between other hashing algorithms and bcrypt it that
bcrypt is slower than other hashing algorithms.
Bcrypt was purposefully made slower to make the hacking of the sensitive
information difficult.

### How can a hacked hashed password cause trouble for the user ?

The hackers do an attack known as Dictionary Attack.
Hackers store a large number of random plain text values (passwords) and 
their bcrypt hashes in a lookup table (key-value dictionary). That means if the hacker hacks some hashed password and if that hashed password is present
in the lookup table (dictionary) then the hacker can have access to the actual plain text password.
That's how compromised hashed password can cause trouble.

To tackle this, we first started adding salt (random characters) to the password before hashing it. So that it becomes difficult for the hacker to backtrack
such hashed string to the plain text format. But even this won't stop the hacker from hacking the passwords as the hacker could create another dictionary where 
the random text and its salted hash is present as key value pair.

### Ok, so how making a hashing algorithm slower help us avoid such kind of attack ?

The key thing that helps the hacker find the plain text password is the lookup table/dictionary.
To create a huge dictionary, the hacker would have to hash a lot of random plain text strings and then hash all of them.
Although it is not very difficult with the use of technology.

But if the algorithm used for hashing itself is slower or resource sensitive, it would take a lot of time for the hacker to 
create such a dictionary easily. Thus making the passwords that are hashed using such slow algorithm more secure.

### Structure of a bcrypt hash -
#### *Bcrypt has in-built salt addition functionality*
Refer - https://stackoverflow.com/questions/6832445/how-can-bcrypt-have-built-in-salts

Pasting the important bit of answer here :

Stored in the database, a bcrypt "hash" might look something like this:

$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa

This is actually three fields, delimited by "$":

2a identifies the bcrypt algorithm version that was used.

10 is the cost factor, 2^10 iterations of the key derivation function are used (which is not enough, by the way. I'd recommend a cost of 12 or more.)

vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa is the salt and the cipher text, 
concatenated and encoded in a modified Base-64. 

The first 22 characters decode to a 16-byte value for the salt. 

The remaining characters are cipher text to be compared for authentication.
