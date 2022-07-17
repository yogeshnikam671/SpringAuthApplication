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


### Authentication using Reactive Authentication Manager

We first need to create a bean `reactiveAuthenticationManager`. We will use
`UserDetailsRepositoryReactiveAuthenticationManager` class to create one.
We can set the password encoder and then return the object which is of type reactiveAuthenticationManager.


Now the `UserDetailsRepositoryReactiveAuthenticationManager` class expects a `ReactiveUserDetailsService`.
So we need to create one by extending it.
We have to override the `loadByUsername` class in `ReactiveuserDetailsService` to specify how we can extract the particular user.

Thus, we have our `ReactiveAuthenticationManager` bean. Now we can make use of the in-built `authenticate` method of this manager
to authenticate a username and password by passing the same as a token to the above method. (Refer to `UserAuthService -> authenticateUsingAuthenticationManager` )

If you compare the old way of verifying the user and the one using authentication manager,
we can see that authenticationManager internally takes care of validating the given password with the hashed one and handling
the error scenarios if any.



