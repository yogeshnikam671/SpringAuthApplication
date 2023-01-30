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


### Authorization using JWT and integrating it with Spring Security

As mentioned in README.md, we used `jjwt` library to create and validate the JWT tokens.
Now, apart from the registration/login endpoints, the other endpoints are expected to send the JWT token
returned by the registration/login endpoints in the `Authorization` header of the request.

We have to configure the spring security web filter chain to validate these tokens on such requests.
To do this, we will take help of filters and `addFilterAt` of the `ServerHttpSecurity` chain.
We will add the filter at `SecurityWebFiltersOrder.AUTHENTICATION`.

To create the filter for authorization, we made use of `AuthenticationWebFilter`.

The `AuthenticationWebFilter` requires two things: 
1. `ReactiveAuthenticationManager`
2. `ServerAuthenticationConverter`

We know what a `ReactiveAuthenticationManager` is, let's understand what is a `ServerAuthenticationConverter` before moving ahead
with `AuthenticationWebFilter`

`ServerAuthenticationConverter` -> 

The ServerAuthenticationConverter is basically used to convert a `ServerWebExchange` into an`Authentication` object. 
We have to override the `convert` method of this interface to define how the ServerWebExchange can be converted into an Authentication object.

In our case `JwtServerAuthenticationConverter`, validates the JWT token received in the headers and then extracts the username from the token and
with the use of `UserDetailsService`, takes out the `UserDetails` from database and creates an Authentication object using the auth details saved in the database.

Note - Since, the Authentication object is created using the data stored in the database, the password is in Bcrypt encoded format and not plain text.
Because of this, the default `ReactiveAuthenticationManager` won't work, since the passwordEncoder set for that one is `BcryptPasswordEncoder`.

Hence, we created a new `NoOpReactiveAuthenticationManager` whose passwordEncoder is set to `NoOpPasswordEncoder` so that it would
work for bcrypt encoded passwords as well.

So this is what a `ServerAuthenticationConverter` is, and we got to know how we can use it to intercept the request and do some validations as well like
validating the JWT token.

Ok, so why does `AuthenticationWebFilter` need these two things ?
The `AuthenticationWebFilter` internally converts the `ServerWebExchange` into an `Authentication` object using the `ServerAuthenticationConverter`
and later authenticates it using the `ReactiveAuthenticationManager` provided.

This is how we can create the filter and add it in the filter chain to intercept and authorize all the requests using JWT token validation.


In the latest changes, we have got rid of the `NoOpReactiveAuthenticationManger` by creating our own custom
authenticationManager by extending it. It is named as `JwtAuthenticationManager`. This authentication manager basically sets a authentication object
in the security context without any username or password.

This is the valid behaviour that we want during JWT token validation since we do not want to authenticate the user again
but just validate the token.

With this, we need not use the deprecated `NoOpReactiveAuthenticationManager` and keep it clean.



