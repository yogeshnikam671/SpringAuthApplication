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


