Spring Security dependency by default makes all the endpoints authorized or secured.
That means we will have to have a auth token while accessing all the endpoints.

There is a way to override this behaviour by getting hold of an object called HttpSecurity.
HttpSecurity lets us configure what are the endpoint paths and what are the access restrictions
to those paths.

We can get hold of the ServerHttpSecurity object by using WebSecurityConfigurerAdapter class.
We just have to extend this class to get access to the HttpSecurity object.
In the latest spring boot, this class is deprecated. So we will be using SecurityFilterChain class to 
get hold of the ServerHttpSecurity object.

We also have to @EnableWebFluxSecurity to configure these endpoints.

